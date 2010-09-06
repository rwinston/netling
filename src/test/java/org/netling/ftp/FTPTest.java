package org.netling.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;
import org.netling.SocketClient;


public class FTPTest {
	FTP ftp = new FTP();
	
	BufferedWriter writer;
	StringWriter output;
	BufferedReader reader;
	
	
	@Before
	public void setUp() {
		output = new StringWriter();	
		writer = new BufferedWriter(output);
		ftp.setControlOutput(writer);	
	}
	
	@Test
	public void testStrictTest() {
		String line = "200 OK";
		assertFalse(ftp.strictCheck(line, "200"));
		
		line = "This is a test line";
		assertTrue(ftp.strictCheck(line, "200"));
	}
	
	@Test
	public void testLenientTest() {
		String line = "abc";
		assertTrue(ftp.lenientCheck(line));
		
		line = "abc-1234";
		assertTrue(ftp.lenientCheck(line));
		
		line = "426-blahblah";
		assertTrue(ftp.lenientCheck(line));
		
		line = "200 OK";
		assertFalse(ftp.lenientCheck(line));
	}
	
	@Test
	public void testUser() throws IOException {
		String response = "331 User OK";
		setupResponse(response);
	
		ftp.user("rory");
		assertEquals(output.toString(), FTPCommand.USER.command() + " rory" + SocketClient.NETASCII_EOL);
		assertEquals(ftp.getReplyCode(), 331);
		assertTrue(FTPReply.isPositiveIntermediate(ftp.getReplyCode()));
		assertEquals(ftp.getReplyString(), response + SocketClient.NETASCII_EOL);
	}
	
	@Test
	public void testPass() throws IOException {
		String response = "230 Logged In Ok";
		setupResponse(response);
		
		ftp.pass("mypassword");
		assertEquals(output.toString(), FTPCommand.PASS + " mypassword" + SocketClient.NETASCII_EOL);
		assertEquals(ftp.getReplyCode(), 230);
		assertTrue(FTPReply.isPositiveCompletion(ftp.getReplyCode()));
		assertEquals(ftp.getReplyString(), response + SocketClient.NETASCII_EOL);
		
	}

	private void setupResponse(String response) {
		reader = new BufferedReader(new StringReader(response));
		ftp.setControlInput(reader);
	}
	
}
