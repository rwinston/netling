package org.netling.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.netling.MalformedServerReplyException;


public class FTPClientTest {
	
	/**
	 * Dummy client class that saves calling methods on an unconnected socket
	 * @author rory
	 *
	 */
	class DummyFTPClient extends FTPClient {
		@Override
		public InetAddress getRemoteAddress()  {
			InetAddress addr = null;
			try {
				addr = InetAddress.getByAddress("localhost", new byte[]{127,0,0,1});
			} catch (UnknownHostException e) {
				fail("Error creating dummy IP address:" + e.getMessage());
			}
			return addr;
		}
	}
	
	FTPClient client = new DummyFTPClient();
	
	@Test
	public void testParsePassiveModeReply() {
		String reply = "227 Entering Passive Mode (64,4,30,33,59,205)";
		try {
			client.parsePassiveModeReply(reply);
		} catch (MalformedServerReplyException e) {
			fail("PASV reply [" + reply + "] should parse correctly");
		}
		assertEquals("64.4.30.33", client.getPassiveHost());
		assertEquals(59<<8|205, client.getPassivePort());
	}
	
	@Test
	public void testExtendedPassiveModeReply() {
		String reply = "229 Entering Extended Passive Mode (|||13747|)";
		try {
			client.parseExtendedPassiveModeReply(reply);
		} catch (MalformedServerReplyException e) {
			fail("EPSV reply [" + reply + "] should parse correctly");
		}
	}

}
