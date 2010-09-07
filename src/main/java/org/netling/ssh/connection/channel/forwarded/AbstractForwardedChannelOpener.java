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

package org.netling.ssh.connection.channel.forwarded;

import org.netling.io.Util;
import org.netling.ssh.connection.Connection;
import org.netling.ssh.connection.channel.Channel;
import org.netling.ssh.connection.channel.OpenFailException;
import org.netling.ssh.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** Base class for {@link ForwardedChannelOpener}'s. */
public abstract class AbstractForwardedChannelOpener
        implements ForwardedChannelOpener {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final String chanType;
    protected final Connection conn;

    protected AbstractForwardedChannelOpener(String chanType, Connection conn) {
        this.chanType = chanType;
        this.conn = conn;
    }

    @Override
    public String getChannelType() {
        return chanType;
    }

    /** Calls the listener with the new channel in a separate thread. */
    protected void callListener(final ConnectListener listener, final Channel.Forwarded chan) {
        new Thread() {

            {
                setName("ConnectListener");
            }

            @Override
            public void run() {
                try {
                    listener.gotConnect(chan);
                } catch (IOException logged) {
                    log.warn("In callback to {}: {}", listener, logged);
                    if (chan.isOpen())
                        Util.closeQuietly(chan);
                    else
                        try {
                            chan.reject(OpenFailException.Reason.CONNECT_FAILED, "");
                        } catch (TransportException cantdonthn) {
                            log.warn("Error rejecting {}: {}", chan, cantdonthn);
                        }
                }
            }

        }.start();
    }

}
