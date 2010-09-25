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

import org.netling.util.Base64;
import org.netling.ssh.common.Buffer;
import org.netling.ssh.common.KeyType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.PublicKey;

/**
 * Represents a Putty (.ppk) keyfile. 
 * 
 *
 * @see PKCS8KeyFile
 */
public class PuTTYKeyFile
        extends PKCS8KeyFile {

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

    private PublicKey pubKey;

    @Override
    public PublicKey getPublic()
            throws IOException {
        return pubKey != null ? pubKey : super.getPublic();
    }

    @Override
    public void init(File location) {
   
        if (location.exists())
            try {
                final BufferedReader br = new BufferedReader(new FileReader(location));
                try {
                    final String header = br.readLine();
                    final String encryption = br.readLine();
                    br.readLine();	// comment
                    final String pubKeyHeader = br.readLine();
                    
                    int lines = Integer.valueOf(pubKeyHeader.split(":")[1].trim());
                    StringBuilder keyBuf = new StringBuilder();
                    for (int i = 0; i < lines; ++i) {
                    	keyBuf.append(br.readLine());
                    }
                    
                    String keyType = header.split(" ")[1].trim();
                    
                    type = KeyType.fromString(keyType);
                    pubKey = new Buffer.PlainBuffer(Base64.decode(keyBuf.toString())).readPublicKey();         
                } finally {
                    br.close();
                }
            } catch (IOException e) {
                // let super provide both public & private key
                log.warn("Error reading public key file: {}", e.toString());
            }
        super.init(location);
    }

}
