/*
* Copyright 2010 netling project <http://netling.org>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.netling.ftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 * 
 * Implementation of org.netling.SocketFactory
 *
 * 
 */
public class FTPSSocketFactory extends SocketFactory {

    private final SSLContext context;
    
    public FTPSSocketFactory(SSLContext context) {
        this.context = context;
    }
    
    @Override
    public Socket createSocket(String address, int port) throws UnknownHostException, IOException {
        return this.context.getSocketFactory().createSocket(address, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return this.context.getSocketFactory().createSocket(address, port);
    }

    @Override
    public Socket createSocket(String address, int port, InetAddress localAddress, int localPort) throws UnknownHostException, IOException {
        return this.context.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.context.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

    /** @deprecated use {@link FTPSServerSocketFactory#createServerSocket(int) instead} */
    @Deprecated
    public ServerSocket createServerSocket(int port) throws IOException {
        return this.init(this.context.getServerSocketFactory().createServerSocket(port));
    }

    /** @deprecated  use {@link FTPSServerSocketFactory#createServerSocket(int, int) instead} */
    @Deprecated
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog));
    }

    /** @deprecated  use {@link FTPSServerSocketFactory#createServerSocket(int, int, InetAddress) instead} */
    @Deprecated
    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog, ifAddress));
    }
        
    /** @deprecated  use {@link FTPSServerSocketFactory#init(ServerSocket)} */
    @Deprecated
    public ServerSocket init(ServerSocket socket) throws IOException {
        ((SSLServerSocket) socket).setUseClientMode(true);
        return socket;
    }
}
