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
package org.netling.ssh.connection;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.netling.concurrent.Future;
import org.netling.concurrent.FutureUtils;
import org.netling.ssh.AbstractService;
import org.netling.ssh.common.DisconnectReason;
import org.netling.ssh.common.ErrorNotifiable;
import org.netling.ssh.common.Message;
import org.netling.ssh.common.SSHException;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.connection.channel.Channel;
import org.netling.ssh.connection.channel.OpenFailException;
import org.netling.ssh.connection.channel.OpenFailException.Reason;
import org.netling.ssh.connection.channel.forwarded.ForwardedChannelOpener;
import org.netling.ssh.transport.Transport;
import org.netling.ssh.transport.TransportException;

/** {@link Connection} implementation. */
public class ConnectionImpl
        extends AbstractService
        implements Connection {

    public static final class GlobalRequestResult
            extends Future<SSHPacket, ConnectionException> {
        public GlobalRequestResult(String name) {
            super(name, ConnectionException.chainer);
        }
    }

    private final Object internalSynchronizer = new Object();

    private final AtomicInteger nextID = new AtomicInteger();

    private final Map<Integer, Channel> channels = new ConcurrentHashMap<Integer, Channel>();

    private final Map<String, ForwardedChannelOpener> openers = new ConcurrentHashMap<String, ForwardedChannelOpener>();

    private final Queue<GlobalRequestResult> globalReqFutures = new LinkedList<GlobalRequestResult>();

    private int windowSize = 2048 * 1024;
    private int maxPacketSize = 32 * 1024;

    /**
     * Create with an associated {@link Transport}.
     *
     * @param trans transport layer
     */
    public ConnectionImpl(Transport trans) {
        super("ssh-connection", trans);
    }

    @Override
    public void attach(Channel chan) {
        log.info("Attaching `{}` channel (#{})", chan.getType(), chan.getID());
        channels.put(chan.getID(), chan);
    }

    @Override
    public Channel get(int id) {
        return channels.get(id);
    }

    @Override
    public ForwardedChannelOpener get(String chanType) {
        return openers.get(chanType);
    }

    @Override
    public void forget(Channel chan) {
        log.info("Forgetting `{}` channel (#{})", chan.getType(), chan.getID());
        channels.remove(chan.getID());
        synchronized (internalSynchronizer) {
            if (channels.isEmpty())
                internalSynchronizer.notifyAll();
        }
    }

    @Override
    public void forget(ForwardedChannelOpener opener) {
        log.info("Forgetting opener for `{}` channels: {}", opener.getChannelType(), opener);
        openers.remove(opener.getChannelType());
    }

    @Override
    public void attach(ForwardedChannelOpener opener) {
        log.info("Attaching opener for `{}` channels: {}", opener.getChannelType(), opener);
        openers.put(opener.getChannelType(), opener);
    }

    private Channel getChannel(SSHPacket buffer)
            throws ConnectionException {
        int recipient = buffer.readInt();
        Channel channel = get(recipient);
        if (channel != null)
            return channel;
        else {
            buffer.rpos(buffer.rpos() - 5);
            throw new ConnectionException(DisconnectReason.PROTOCOL_ERROR, "Received " + buffer.readMessageID()
                    + " on unknown channel #" + recipient);
        }
    }

    @Override
    public void handle(Message msg, SSHPacket buf)
            throws SSHException {
        if (msg.in(91, 100))
            getChannel(buf).handle(msg, buf);

        else if (msg.in(80, 90))
            switch (msg) {
                case REQUEST_SUCCESS:
                    gotGlobalReqResponse(buf);
                    break;
                case REQUEST_FAILURE:
                    gotGlobalReqResponse(null);
                    break;
                case CHANNEL_OPEN:
                    gotChannelOpen(buf);
                    break;
                default:
                    super.handle(msg, buf);
            }

        else
            super.handle(msg, buf);
    }

    @Override
    public void notifyError(SSHException error) {
        super.notifyError(error);

        synchronized (globalReqFutures) {
            FutureUtils.alertAll(error, globalReqFutures);
            globalReqFutures.clear();
        }

        ErrorNotifiable.Util.alertAll(error, channels.values());
        channels.clear();
    }

    @Override
    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    @Override
    public Transport getTransport() {
        return trans;
    }

    @Override
    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    @Override
    public int getWindowSize() {
        return windowSize;
    }

    @Override
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public void join()
            throws InterruptedException {
        synchronized (internalSynchronizer) {
            while (!channels.isEmpty())
                internalSynchronizer.wait();
        }
    }

    @Override
    public int nextID() {
        return nextID.getAndIncrement();
    }

    @Override
    public GlobalRequestResult sendGlobalRequest(String name, boolean wantReply, byte[] specifics)
            throws TransportException {
        synchronized (globalReqFutures) {
            log.info("Making global request for `{}`", name);
            trans.write(new SSHPacket(Message.GLOBAL_REQUEST).putString(name)
                    .putBoolean(wantReply).putRawBytes(specifics));

            GlobalRequestResult future = null;
            if (wantReply) {
                future = new GlobalRequestResult("global req for " + name);
                globalReqFutures.add(future);
            }
            return future;
        }
    }

    private void gotGlobalReqResponse(SSHPacket response)
            throws ConnectionException {
        synchronized (globalReqFutures) {
            final GlobalRequestResult gr = globalReqFutures.poll();
            if (gr == null)
                throw new ConnectionException(DisconnectReason.PROTOCOL_ERROR,
                        "Got a global request response when none was requested");
            else if (response == null)
                gr.error(new ConnectionException("Global request [" + gr + "] failed"));
            else
                gr.set(response);
        }
    }

    private void gotChannelOpen(SSHPacket buf)
            throws ConnectionException, TransportException {
        final String type = buf.readString();
        log.debug("Received CHANNEL_OPEN for `{}` channel", type);
        if (openers.containsKey(type))
            openers.get(type).handleOpen(buf);
        else {
            log.warn("No opener found for `{}` CHANNEL_OPEN request -- rejecting", type);
            sendOpenFailure(buf.readInt(), OpenFailException.Reason.UNKNOWN_CHANNEL_TYPE, "");
        }
    }

    @Override
    public void sendOpenFailure(int recipient, Reason reason, String message)
            throws TransportException {
        trans.write(new SSHPacket(Message.CHANNEL_OPEN_FAILURE)
                .putInt(recipient)
                .putInt(reason.getCode())
                .putString(message));
    }

    @Override
    public void notifyDisconnect()
            throws SSHException {
        super.notifyDisconnect();
        final ConnectionException ex = new ConnectionException("Disconnected.");
        FutureUtils.alertAll(ex, globalReqFutures);
        ErrorNotifiable.Util.alertAll(ex, new HashSet<Channel>(channels.values()));
    }

}
