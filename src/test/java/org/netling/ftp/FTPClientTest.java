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
 *
 * This file may incorporate work covered by the following copyright and
 * permission notice:
 *
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing,
 *      software distributed under the License is distributed on an
 *      "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *      KIND, either express or implied.  See the License for the
 *      specific language governing permissions and limitations
 *      under the License.
 */
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
