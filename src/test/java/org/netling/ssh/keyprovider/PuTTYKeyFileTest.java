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
package org.netling.ssh.keyprovider;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import org.junit.Before;
import org.junit.Test;
import org.netling.ssh.common.KeyType;
import org.netling.ssh.common.SecurityUtils;
import org.netling.ssh.userauth.keyprovider.FileKeyProvider;
import org.netling.ssh.userauth.keyprovider.PuTTYKeyFile;
import org.netling.ssh.util.KeyUtil;

public class PuTTYKeyFileTest {

    static final FileKeyProvider rsa = new PuTTYKeyFile();
    static final String modulus = "69650227815596402965041754202443924697588361387938672840471051524428347040623571500991857116961799154966200313268938455626243645190245251192190317203332304379093760943443597795064336595672780503265825538124280598191801419439302771902075590286048611895234776549628100141296823306176552853792696123466332512069";
    static final String exponent = "37";
    
    @Before
    public void setUp()
            throws UnsupportedEncodingException, GeneralSecurityException {
        if (!SecurityUtils.isBouncyCastleRegistered())
            throw new AssertionError("bouncy castle needed");
        rsa.init(new File("src/test/resources/test_key.ppk"));
    }

    @Test
    public void testKeys()
            throws IOException, GeneralSecurityException {

    	PublicKey expected = SecurityUtils.getKeyFactory("RSA").generatePublic(
    			new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent)));
    	assertEquals(expected, rsa.getPublic());
    }

    @Test
    public void testType()
            throws IOException {
        assertEquals(rsa.getType(), KeyType.RSA);
    }

}
