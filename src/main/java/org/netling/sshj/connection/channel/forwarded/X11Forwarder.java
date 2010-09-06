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
package org.netling.sshj.connection.channel.forwarded;

import org.netling.sshj.common.SSHPacket;
import org.netling.sshj.connection.Connection;
import org.netling.sshj.connection.ConnectionException;
import org.netling.sshj.transport.TransportException;

/**
 * Handles forwarded {@code x11} channels. The actual request to forward X11 should be made from the specific {@link
 * org.netling.sshj.connection.channel.direct.Session Session}.
 */
public class X11Forwarder
        extends AbstractForwardedChannelOpener {

    /** An {@code x11} forwarded channel. */
    public static class X11Channel
            extends AbstractForwardedChannel {

        public static final String TYPE = "x11";

        public X11Channel(Connection conn, int recipient, int remoteWinSize, int remoteMaxPacketSize, String origIP,
                          int origPort) {
            super(conn, TYPE, recipient, remoteWinSize, remoteMaxPacketSize, origIP, origPort);
        }

    }

    private final ConnectListener listener;

    /**
     * @param conn     connection layer
     * @param listener listener which will be delegated {@link X11Channel}'s to next
     */
    public X11Forwarder(Connection conn, ConnectListener listener) {
        super(X11Channel.TYPE, conn);
        this.listener = listener;
    }

    /** Internal API */
    @Override
    public void handleOpen(SSHPacket buf)
            throws ConnectionException, TransportException {
        callListener(listener, new X11Channel(conn,
                                              buf.readInt(),
                                              buf.readInt(), buf.readInt(),
                                              buf.readString(), buf.readInt()));
    }

    /** Stop handling {@code x11} channel open requests. De-registers itself with connection layer. */
    public void stop() {
        conn.forget(this);
    }

}
