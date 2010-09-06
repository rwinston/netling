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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * FTP over SSL processing. If desired, the JVM property -Djavax.net.debug=all can be used to
 * see wire-level SSL details.
 *
 * @version $Id: FTPSClient.java 962836 2010-07-10 12:48:22Z sebb $
 * 
 */
public class FTPSClient extends FTPClient {  
	/**
	 * Parameters for the <code>PROT</code> command 
	 * @author rory
	 */
	public enum Prot {
		CLEAR("C"),
		SAFE("S"),
		CONFIDENTIAL("E"),
		PRIVATE("P");

		private final String code;

		Prot(String code) {
			this.code = code;
		}

		String code() { return code; }
	}

	/** Default protocol name */
	private static final String DEFAULT_PROTOCOL = "TLS";

	/** The security mode. (True - Implicit Mode / False - Explicit Mode) */
	private final boolean isImplicit;
	/** The use SSL/TLS protocol. */
	private final String protocol;
	/** The AUTH Command value */
	private String auth = DEFAULT_PROTOCOL;
	/** The context object. */
	private SSLContext context;
	/** For RFC 4217 behaviour */
	private boolean autoClose = false;
	/** The socket object. */
	private Socket plainSocket;
	/** SSL-wrapped socket */
	private SSLSocket ssock;
	/** The established socket flag. */
	private boolean isCreation = true;
	/** The use client mode flag. */
	private boolean isClientMode = true;
	/** The need client auth flag. */
	private boolean isNeedClientAuth = false;
	/** The want client auth flag. */
	private boolean isWantClientAuth = false;
	/** The cipher suites */
	private String[] suites = null;
	/** The protocol versions */
	private String[] protocols = null;

	/** The FTPS {@link TrustManager} implementation. */
	private TrustManager trustManager = new FTPSTrustManager();

	/** The {@link KeyManager} */
	private KeyManager keyManager;

	/**
	 * Constructor for FTPSClient.
	 * @throws NoSuchAlgorithmException A requested cryptographic algorithm
	 * is not available in the environment.
	 */
	public FTPSClient() throws NoSuchAlgorithmException {
		this.protocol = DEFAULT_PROTOCOL;
		this.isImplicit = false;
	}

	/**
	 * Constructor for FTPSClient.
	 * @param isImplicit The secutiry mode(Implicit/Explicit).
	 * @throws NoSuchAlgorithmException A requested cryptographic algorithm
	 * is not available in the environment.
	 */
	public FTPSClient(boolean isImplicit) throws NoSuchAlgorithmException {
		this.protocol = DEFAULT_PROTOCOL;
		this.isImplicit = isImplicit;
	}

	/**
	 * Constructor for FTPSClient.
	 * @param protocol the protocol
	 * @throws NoSuchAlgorithmException A requested cryptographic algorithm
	 * is not available in the environment.
	 */
	public FTPSClient(String protocol) throws NoSuchAlgorithmException {
		this.protocol = protocol;
		this.isImplicit = false;
	}

	/**
	 * Constructor for FTPSClient.
	 * @param protocol the protocol
	 * @param isImplicit The secutiry mode(Implicit/Explicit).
	 * @throws NoSuchAlgorithmException A requested cryptographic algorithm
	 * is not available in the environment.
	 */
	public FTPSClient(String protocol, boolean isImplicit)
	throws NoSuchAlgorithmException {
		this.protocol = protocol;
		this.isImplicit = isImplicit;
	}

	/**
	 * Constructor for FTPSClient.
	 * @param isImplicit The secutiry mode(Implicit/Explicit).
	 * @param context A pre-configured SSL Context
	 */
	public FTPSClient(boolean isImplicit, SSLContext context) {
		this.isImplicit = isImplicit;
		this.context = context;
		this.protocol = DEFAULT_PROTOCOL;
	}

	/**
	 * Constructor for FTPSClient.
	 * @param context A pre-configured SSL Context
	 */
	public FTPSClient(SSLContext context) {
		this(false, context);
	}


	/**
	 * Set AUTH command use value.
	 * This processing is done before connected processing.
	 * @param auth AUTH command use value.
	 */
	public void setAuthValue(String auth) {
		this.auth = auth;
	}

	/**
	 * Return AUTH command use value.
	 * @return AUTH command use value.
	 */
	public String getAuthValue() {
		return this.auth;
	}


	/**
	 * Because there are so many connect() methods,
	 * the connectAction() method is provided as a means of performing
	 * some action immediately after establishing a connection,
	 * rather than reimplementing all of the connect() methods.
	 * @throws IOException If it throw by connectAction.
	 * @see org.netling.SocketClient#_connectAction()
	 */
	@Override
	protected void onConnect() throws IOException {
		// Implicit mode.
		if (isImplicit) sslNegotiation();
		super.onConnect();
		// Explicit mode.
		if (!isImplicit) {
			execAUTH();
			sslNegotiation();
		}
	}

	/**
	 * AUTH command.
	 * @throws SSLException If it server reply code not equal "234" and "334".
	 * @throws IOException If an I/O error occurs while either sending
	 * the command.
	 */
	private void execAUTH() throws SSLException, IOException {
		int replyCode = sendCommand(FTPSCommand.AUTH.command(), auth);
		if (FTPReply.SECURITY_MECHANISM_IS_OK.code() == replyCode) {
			// replyCode = 334
			// I carry out an ADAT command.
		} else if (FTPReply.SECURITY_DATA_EXCHANGE_COMPLETE.code() != replyCode) {
			throw new SSLException(getReplyString());
		}
	}

	/**
	 * Performs a lazy init of the SSL context
	 * @throws IOException
	 */
	private void initSslContext() throws IOException {
		if(context == null) {
			try  {
				context = SSLContext.getInstance(protocol);
				context.init(new KeyManager[] { getKeyManager() } , new TrustManager[] { getTrustManager() } , null);
			} catch (KeyManagementException e) {
				IOException ioe = new IOException("Could not initialize SSL context");
				ioe.initCause(e);
				throw ioe;
			} catch (NoSuchAlgorithmException e) {
				IOException ioe = new IOException("Could not initialize SSL context");
				ioe.initCause(e);
				throw ioe;
			}
		}
	}

	/**
	 * SSL/TLS negotiation. Acquires an SSL socket of a control
	 * connection and carries out handshake processing.
	 * @throws IOException If server negotiation fails
	 */
	private void sslNegotiation() throws IOException {
		plainSocket = socket;
		initSslContext();

		SSLSocketFactory ssf = context.getSocketFactory();
		String ip = socket.getInetAddress().getHostAddress();
		int port = socket.getPort();
		ssock =
			(SSLSocket) ssf.createSocket(socket, ip, port, true);
		ssock.setEnableSessionCreation(isCreation);
		ssock.setUseClientMode(isClientMode);
		// server mode
		if (!isClientMode) {
			ssock.setNeedClientAuth(isNeedClientAuth);
			ssock.setWantClientAuth(isWantClientAuth);
		}

		if (protocols != null) ssock.setEnabledProtocols(protocols);
		if (suites != null) ssock.setEnabledCipherSuites(suites);
		ssock.startHandshake();

		this.socket = ssock;
		controlInput = new BufferedReader(new InputStreamReader(
				ssock .getInputStream(), getControlEncoding()));
		controlOutput = new BufferedWriter(new OutputStreamWriter(
				ssock.getOutputStream(), getControlEncoding()));
	}

	/**
	 * Get the {@link KeyManager} instance.
	 * @return The {@link KeyManager} instance
	 */
	private KeyManager getKeyManager() {
		return keyManager;
	}

	/**
	 * Set a {@link KeyManager} to use
	 *
	 * @param keyManager The KeyManager implementation to set.
	 */
	public void setKeyManager(KeyManager keyManager) {
		this.keyManager = keyManager;
	}

	/**
	 * Controls whether new a SSL session may be established by this socket.
	 * @param isCreation The established socket flag.
	 */
	public void setEnabledSessionCreation(boolean isCreation) {
		this.isCreation = isCreation;
	}

	/**
	 * Returns true if new SSL sessions may be established by this socket.
	 * When the underlying {@link Socket} instance is not SSL-enabled (i.e. an
	 * instance of {@link SSLSocket} with {@link SSLSocket}{@link #getEnableSessionCreation()}) enabled,
	 * this returns False.
	 * @return true - Indicates that sessions may be created;
	 * this is the default.
	 * false - indicates that an existing session must be resumed.
	 */
	public boolean getEnableSessionCreation() {
		if (socket instanceof SSLSocket)
			return ((SSLSocket)socket).getEnableSessionCreation();
		return false;
	}

	/**
	 * Configures the socket to require client authentication.
	 * @param isNeedClientAuth The need client auth flag.
	 */
	public void setNeedClientAuth(boolean isNeedClientAuth) {
		this.isNeedClientAuth = isNeedClientAuth;
	}

	/**
	 * Returns true if the socket will require client authentication.
	 * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns false.
	 * @return true - If the server mode socket should request
	 * that the client authenticate itself.
	 */
	public boolean getNeedClientAuth() {
		if (socket instanceof SSLSocket)
			return ((SSLSocket)socket).getNeedClientAuth();
		return false;
	}

	/**
	 * Configures the socket to request client authentication,
	 * but only if such a request is appropriate to the cipher
	 * suite negotiated.
	 * @param isWantClientAuth The want client auth flag.
	 */
	public void setWantClientAuth(boolean isWantClientAuth) {
		this.isWantClientAuth = isWantClientAuth;
	}

	/**
	 * Returns true if the socket will request client authentication.
	 * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns false.
	 * @return true - If the server mode socket should request
	 * that the client authenticate itself.
	 */
	public boolean getWantClientAuth() {
		if (socket instanceof SSLSocket)
			return ((SSLSocket)socket).getWantClientAuth();
		return false;
	}

	/**
	 * Configures the socket to use client (or server) mode in its first
	 * handshake.
	 * @param isClientMode The use client mode flag.
	 */
	public void setUseClientMode(boolean isClientMode) {
		this.isClientMode = isClientMode;
	}

	/**
	 * Returns true if the socket is set to use client mode
	 * in its first handshake.
	 * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns false.
	 * @return true - If the socket should start its first handshake
	 * in "client" mode.
	 */
	public boolean getUseClientMode() {
		if (socket instanceof SSLSocket)
			return ((SSLSocket)socket).getUseClientMode();
		return false;
	}

	/**
	 * Controls which particular cipher suites are enabled for use on this
	 * connection. Called before server negotiation.
	 * @param cipherSuites The cipher suites.
	 */
	public void setEnabledCipherSuites(String[] cipherSuites) {
		suites = new String[cipherSuites.length];
		System.arraycopy(cipherSuites, 0, suites, 0, cipherSuites.length);
	}

	/**
	 * Returns the names of the cipher suites which could be enabled
	 * for use on this connection.
	 * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns null.
	 * @return An array of cipher suite names, or <code>null</code>
	 */
	public String[] getEnabledCipherSuites() {
		if (socket instanceof SSLSocket)
			return ((SSLSocket)socket).getEnabledCipherSuites();
		return null;
	}

	/**
	 * Controls which particular protocol versions are enabled for use on this
	 * connection. I perform setting before a server negotiation.
	 * @param protocolVersions The protocol versions.
	 */
	public void setEnabledProtocols(String[] protocolVersions) {
		protocols = new String[protocolVersions.length];
		System.arraycopy(protocolVersions, 0, protocols, 0, protocolVersions.length);
	}

	/**
	 * Returns the names of the protocol versions which are currently
	 * enabled for use on this connection.
	 * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns null.
	 * @return An array of protocols, or <code>null</code>
	 */
	public String[] getEnabledProtocols() {
		if (socket instanceof SSLSocket)
			return ((SSLSocket)socket).getEnabledProtocols();
		return null;
	}

	/**
	 * PBSZ command. pbsz value: 0 to (2^32)-1 decimal integer.
	 * @param pbsz Protection Buffer Size.
	 * @throws SSLException If the server reply code does not equal "200".
	 * @throws IOException If an I/O error occurs while sending
	 * the command.
	 */
	public void execPBSZ(long pbsz) throws SSLException, IOException {
		if (pbsz < 0 || 4294967295L < pbsz)
			throw new IllegalArgumentException();
		if (FTPReply.COMMAND_OK.code() != sendCommand(FTPSCommand.PBSZ.command(), String.valueOf(pbsz)))
			throw new SSLException(getReplyString());
	}

	/**
	 * PROT command.</br>
	 * C - Clear</br>
	 * S - Safe(SSL protocol only)</br>
	 * E - Confidential(SSL protocol only)</br>
	 * P - Private
	 * @param prot Data Channel Protection Level.
	 * @throws SSLException If the server reply code does not equal "200".
	 * @throws IOException If an I/O error occurs while sending
	 * the command.
	 */
	public void execPROT(Prot prot) throws SSLException, IOException {
		if (prot == null) prot = Prot.CLEAR;

		if (FTPReply.COMMAND_OK.code() != sendCommand(
				FTPSCommand.PROT.command(), prot.code()))
			throw new SSLException(getReplyString());
		if (prot == Prot.CLEAR) {
			setSocketFactory(null);
			setServerSocketFactory(null);
		} else {
			setSocketFactory(new FTPSSocketFactory(context));
			setServerSocketFactory(new FTPSServerSocketFactory(context));
			initSslContext();
		}
	}

	/**
	 * Send an FTP command.
	 * The CCC (Clear Command Channel) command causes the underlying {@link SSLSocket} instance  to be assigned
	 * to a plain {@link Socket} instances
	 * @param command The FTP command.
	 * @return server reply.
	 * @throws IOException If an I/O error occurs while sending
	 * the command.
	 * @see org.netling.ftp.FTP#sendCommand(java.lang.String)
	 */
	@Override
	public int sendCommand(String command, String args) throws IOException {
		int repCode = super.sendCommand(command, args);
		/* If CCC is issued, restore socket i/o streams to unsecured versions */
		if (FTPSCommand.CCC.command().equals(command)) {
			if (FTPReply.COMMAND_OK.code() == repCode) {
				if (autoClose)
					socket.close();
				socket = plainSocket;
				controlInput = new BufferedReader(
						new InputStreamReader(
								socket .getInputStream(), getControlEncoding()));
				controlOutput = new BufferedWriter(
						new OutputStreamWriter(
								socket.getOutputStream(), getControlEncoding()));
				setSocketFactory(null);
			} else {
				throw new SSLException(getReplyString());
			}
		}
		return repCode;
	}

	/**
	 * Returns a socket of the data connection.
	 * Wrapped as an {@link SSLSocket}, which carries out handshake processing.
	 * @param command The textual representation of the FTP command to send.
	 * @param arg The arguments to the FTP command.
	 * If this parameter is set to null, then the command is sent with
	 * no arguments.
	 * @return corresponding to the established data connection.
	 * Null is returned if an FTP protocol error is reported at any point
	 * during the establishment and initialization of the connection.
	 * @throws IOException If there is any problem with the connection.
	 * @see FTPClient#openDataConnection(int, String)
	 */
	@Override
	protected Socket openDataConnection(FTPCommand command, String arg)
	throws IOException {
		Socket socket = super.openDataConnection(command, arg);
		if (socket != null && socket instanceof SSLSocket) {
			SSLSocket sslSocket = (SSLSocket)socket;

			sslSocket.setUseClientMode(isClientMode);
			sslSocket.setEnableSessionCreation(isCreation);

			// server mode
			if (!isClientMode) {
				sslSocket.setNeedClientAuth(isNeedClientAuth);
				sslSocket.setWantClientAuth(isWantClientAuth);
			}
			if (suites != null)
				sslSocket.setEnabledCipherSuites(suites);
			if (protocols != null)
				sslSocket.setEnabledProtocols(protocols);
			sslSocket.startHandshake();
		}

		return socket;
	}

	/**
	 * Get the currently configured {@link TrustManager}.
	 *
	 * @return A TrustManager instance.
	 */
	public TrustManager getTrustManager() {
		return trustManager;
	}

	/**
	 * Override the default {@link TrustManager} to use.
	 *
	 * @param trustManager The TrustManager implementation to set.
	 */
	public void setTrustManager(TrustManager trustManager) {
		this.trustManager = trustManager;
	}

	/**
	 * If set to <code>true</code>, closes the underlying plain socket
	 * when the SSL socket is closed on transmission of a <code>CCC</code> command. 
	 * For RFC 4217 compliance, this should
	 * be set to <code>false</code>.
	 * 
	 * @param autoClose Whether to close the underlying plain socket on a <code>CCC</code>
	 */
	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}
}

