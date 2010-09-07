/*
 * Copyright 2010 Shikhar Bhushan
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
import org.netling.ssh.userauth.password.AccountResource;
import org.netling.ssh.userauth.password.PasswordFinder;

/** Implements the {@code password} authentication method. Password-change request handling is not currently supported. */
public class AuthPassword
        extends AbstractAuthMethod {

    private final PasswordFinder pwdf;

    public AuthPassword(PasswordFinder pwdf) {
        super("password");
        this.pwdf = pwdf;
    }

    @Override
    public SSHPacket buildReq()
            throws UserAuthException {
        final AccountResource accountResource = makeAccountResource();
        log.info("Requesting password for {}", accountResource);
        return super.buildReq() // the generic stuff
                .putBoolean(false) // no, we are not responding to a CHANGEREQ
                .putSensitiveString(pwdf.reqPassword(accountResource));
    }

    @Override
    public void handle(Message cmd, SSHPacket buf)
            throws UserAuthException, TransportException {
        if (cmd == Message.USERAUTH_60)
            throw new UserAuthException("Password change request received; unsupported operation");
        else
            super.handle(cmd, buf);
    }

    /**
     * Returns {@code true} if the associated {@link PasswordFinder} tells that we should retry with a new password that
     * it will supply.
     */
    @Override
    public boolean shouldRetry() {
        return pwdf.shouldRetry(makeAccountResource());
    }

}
