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
package org.netling.ssh.userauth.method;

import org.netling.ssh.common.Message;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.transport.TransportException;
import org.netling.ssh.userauth.UserAuthException;
import org.netling.ssh.userauth.keyprovider.KeyProvider;

/**
 * Implements the {@code "publickey"} SSH authentication method.
 * <p/>
 * Requesteing authentication with this method first sends a "feeler" request with just the public key, and if the
 * server responds with {@code SSH_MSG_USERAUTH_PK_OK} indicating that the key is acceptable, it proceeds to send a
 * request signed with the private key. Therefore, private keys are not requested from the associated {@link
 * KeyProvider} unless needed.
 */
public class AuthPublickey
        extends KeyedAuthMethod {

    /** Initialize this method with the provider for public and private key. */
    public AuthPublickey(KeyProvider kProv) {
        super("publickey", kProv);
    }

    /** Internal use. */
    @Override
    public void handle(Message cmd, SSHPacket buf)
            throws UserAuthException, TransportException {
        if (cmd == Message.USERAUTH_60)
            sendSignedReq();
        else
            super.handle(cmd, buf);
    }

    /**
     * Builds SSH_MSG_USERAUTH_REQUEST packet.
     *
     * @param signed whether the request packet will contain signature
     *
     * @return the {@link SSHPacket} containing the request packet
     *
     * @throws UserAuthException
     */
    private SSHPacket buildReq(boolean signed)
            throws UserAuthException {
        log.debug("Attempting authentication using {}", kProv);
        return putPubKey(super.buildReq().putBoolean(signed));
    }

    /**
     * Send SSH_MSG_USERAUTH_REQUEST containing the signature.
     *
     * @throws UserAuthException
     * @throws TransportException
     */
    private void sendSignedReq()
            throws UserAuthException, TransportException {
        log.debug("Key acceptable, sending signed request");
        params.getTransport().write(putSig(buildReq(true)));
    }

    /** Builds a feeler request (sans signature). */
    @Override
    protected SSHPacket buildReq()
            throws UserAuthException {
        return buildReq(false);
    }

}
