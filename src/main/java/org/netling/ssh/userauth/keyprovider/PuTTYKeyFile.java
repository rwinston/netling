/*
 * Copyright 2010 netling project <http://netling.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netling.ssh.userauth.keyprovider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.netling.ssh.common.Buffer;
import org.netling.ssh.common.KeyType;
import org.netling.ssh.userauth.password.PasswordFinder;
import org.netling.ssh.userauth.password.PrivateKeyFileResource;
import org.netling.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source for the following documentation: putty 0.6.0 source / sshpubk.c
 * -----------------------------------------------------------------------
 *
 * PuTTY's own format for SSH-2 keys is as follows:
 *
 * The file is text. Lines are terminated by CRLF, although CR-only
 * and LF-only are tolerated on input.
 *
 * The first line says "PuTTY-User-Key-File-2: " plus the name of the
 * algorithm ("ssh-dss", "ssh-rsa" etc).
 *
 * The next line says "Encryption: " plus an encryption type.
 * Currently the only supported encryption types are "aes256-cbc"
 * and "none".
 *
 * The next line says "Comment: " plus the comment string.
 *
 * Next there is a line saying "Public-Lines: " plus a number N.
 * The following N lines contain a base64 encoding of the public
 * part of the key. This is encoded as the standard SSH-2 public key
 * blob (with no initial length): so for RSA, for example, it will
 * read
 *
 *    string "ssh-rsa"
 *    mpint  exponent
 *    mpint  modulus
 *
 * Next, there is a line saying "Private-Lines: " plus a number N,
 * and then N lines containing the (potentially encrypted) private
 * part of the key. For the key type "ssh-rsa", this will be
 * composed of
 *
 *    mpint  private_exponent
 *    mpint  p                  (the larger of the two primes)
 *    mpint  q                  (the smaller prime)
 *    mpint  iqmp               (the inverse of q modulo p)
 *    data   padding            (to reach a multiple of the cipher block size)
 *
 * And for "ssh-dss", it will be composed of
 *
 *    mpint  x                  (the private key parameter)
 *  [ string hash   20-byte hash of mpints p || q || g   only in old format ]
 *
 * Finally, there is a line saying "Private-MAC: " plus a hex
 * representation of a HMAC-SHA-1 of:
 *
 *    string  name of algorithm ("ssh-dss", "ssh-rsa")
 *    string  encryption type
 *    string  comment
 *    string  public-blob
 *    string  private-plaintext (the plaintext version of the
 *                               private part, including the final
 *                               padding)
 *
 * The key to the MAC is itself a SHA-1 hash of:
 *
 *    data    "putty-private-key-file-mac-key"
 *    data    passphrase
 *
 * (An empty passphrase is used for unencrypted keys.)
 *
 * If the key is encrypted, the encryption key is derived from the
 * passphrase by means of a succession of SHA-1 hashes. Each hash
 * is the hash of:
 *
 *    uint32  sequence-number
 *    data    passphrase
 *
 * where the sequence-number increases from zero. As many of these
 * hashes are used as necessary.
 *
 * For backwards compatibility with snapshots between 0.51 and
 * 0.52, we also support the older key file format, which begins
 * with "PuTTY-User-Key-File-1" (version number differs). In this
 * format the Private-MAC: field only covers the private-plaintext
 * field and nothing else (and without the 4-byte string length on
 * the front too). Moreover, the Private-MAC: field can be replaced
 * with a Private-Hash: field which is a plain SHA-1 hash instead of
 * an HMAC (this was generated for unencrypted keys).
 */


/**
 * Represents a Putty (.ppk) keyfile.
 *
 * @see PKCS8KeyFile
 */
public class PuTTYKeyFile
        implements FileKeyProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(PuTTYKeyFile.class);

    public static class Factory
            implements org.netling.ssh.common.Factory.Named<FileKeyProvider> {

        @Override
        public FileKeyProvider create() {
            return new PuTTYKeyFile();
        }

        @Override
        public String getName() {
            return "PuTTY";
        }

    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private File location;
    private PasswordFinder pwdf;
    protected PrivateKeyFileResource resource;

    private KeyType type;
    private PublicKey pubKey;
    private PrivateKey privKey;

    @Override
    public KeyType getType()
            throws IOException {
        if (type == null)
            readTypeAndPublicKey();
        return type;
    }

    @Override
    public PublicKey getPublic()
            throws IOException {
        if (pubKey == null)
            readTypeAndPublicKey();
        return pubKey;
    }

    @Override
    public PrivateKey getPrivate() {
        if (privKey == null)
            readPrivateKey();
        return privKey;
    }

    @Override
    public void init(File location) {
        init(location, null);
    }

    @Override
    public void init(File location, PasswordFinder pwdf) {
        this.location = location;
        this.pwdf = pwdf;
        resource = new PrivateKeyFileResource(location.getAbsoluteFile());
    }

    private void readTypeAndPublicKey()
            throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(location));
        try {
            final String header = br.readLine();
            final String encryption = br.readLine();
            br.readLine(); // comment
            final String pubKeyHeader = br.readLine();
            final int pubLines = getHeaderValue(pubKeyHeader);
            StringBuilder keyBuf = new StringBuilder();
            for (int i = 0; i < pubLines; ++i) {
                keyBuf.append(br.readLine());
            }
            String keyType = header.split(" ")[1].trim();
            type = KeyType.fromString(keyType);
            pubKey = new Buffer.PlainBuffer(Base64.decode(keyBuf.toString())).readPublicKey();
            
            // Read the private key too
            final String privKeyHeader = br.readLine();
            final int privLines = getHeaderValue(privKeyHeader);
            StringBuilder privKeyBuf = new StringBuilder();
            for (int i = 0; i < privLines; ++i) {
            	privKeyBuf.append(br.readLine());
            }
            System.out.println(header);
            System.out.println("Encryption=" + encryption);
            System.out.println(privKeyBuf.toString());
            Buffer privateKey = new Buffer.PlainBuffer(Base64.decode(privKeyBuf.toString()));
            BigInteger privExponent = privateKey.readMPInt();
            BigInteger p = privateKey.readMPInt();
            BigInteger q = privateKey.readMPInt();
            BigInteger iqmp = privateKey.readMPInt();
            System.out.println("p=" + p + ",q=" + q);
            
            final String mac = br.readLine().split(":")[1].trim();
            System.out.println(mac);
            
            
            
        } finally {
            br.close();
        }
    }
    
    int getHeaderValue(final String header) {
    	return Integer.valueOf(header.split(":")[1].trim());
    }

    private PrivateKey readPrivateKey() {
        throw new RuntimeException("TODO");
    }

}
