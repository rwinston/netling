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
package org.netling.ssh.transport.kex;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.netling.ssh.common.Message;
import org.netling.ssh.common.SSHPacket;
import org.netling.ssh.transport.Transport;
import org.netling.ssh.transport.TransportException;
import org.netling.ssh.transport.digest.Digest;

/** Key exchange algorithm. */
public interface KeyExchange {

    /**
     * Initialize the key exchange algorithm.
     *
     * @param trans the transport
     * @param V_S   the server identification string
     * @param V_C   the client identification string
     * @param I_S   the server key init packet
     * @param I_C   the client key init packet
     *
     * @throws GeneralSecurityException
     * @throws TransportException       if there is an error sending a packet
     */
    void init(Transport trans, byte[] V_S, byte[] V_C, byte[] I_S, byte[] I_C)
            throws GeneralSecurityException, TransportException;

    /** @return the computed H parameter */
    byte[] getH();

    /** @return the computed K parameter */
    byte[] getK();

    /**
     * The message digest used by this key exchange algorithm.
     *
     * @return the message digest
     */
    Digest getHash();

    /** @return the host key determined from server's response packets */
    PublicKey getHostKey();

    /**
     * Process the next packet
     *
     * @param msg    message identifier
     * @param buffer the packet
     *
     * @return a boolean indicating if the processing is complete or if more packets are to be received
     *
     * @throws GeneralSecurityException
     * @throws TransportException       if there is an error sending a packet
     */
    boolean next(Message msg, SSHPacket buffer)
            throws GeneralSecurityException, TransportException;

}
