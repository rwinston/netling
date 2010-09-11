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

/***
 * FTPCommand stores a set of constants for FTP command codes.  To interpret
 * the meaning of the codes, familiarity with RFC 959 is assumed.
 * The mnemonic constant names are transcriptions from the code descriptions
 * of RFC 959.  For those who think in terms of the actual FTP commands,
 * a set of constants such as {@link #USER  USER } are provided
 * where the constant name is the same as the FTP command.
 ***/
public enum FTPCommand {
	USER("USER"),
    PASS("PASS"),
    ACCT("ACCT"),
    CWD("CWD"),
    CDUP("CDUP"),
    SMNT("SMNT"),
    REIN("REIN"),
    QUIT("QUIT"),
    PORT("PORT"),
    PASV("PASV"),
    TYPE("TYPE"),
    STRU("STRU"),
    MODE("MODE"),
    RETR("RETR"),
    STOR("STOR"),
    STOU("STOU"),
    APPE("APPE"),
    ALLO("ALLO"),
    REST("REST"),
    RNFR("RNFR"),
    RNTO("RNTO"),
    ABOR("ABOR"),
    DELE("DELE"),
    RMD("RMD"),
    MKD("MKD"),
    PWD("PWD"),
    LIST("LIST"),
    NLST("NLST"),
    SITE("SITE"),
    SYST("SYST"),
    STAT("STAT"),
    HELP("HELP"),
    NOOP("NOOP"),   
    MDTM("MDTM"),
    FEAT("FEAT"),
    MFMT("MFMT"),
    EPSV("EPSV"),
    EPRT("EPRT");
 
	/** The underlying FTP protocol command */
	final String command;
	
	public String command() { return command; }
	
	FTPCommand(final String command) {
		this.command = command;
	}
	
	
    /**
     * Retrieve the FTP protocol command string corresponding to a specified
     * command code.
     * <p>
     * @param command The command code.
     * @return The FTP protcol command string corresponding to a specified
     *         command code.
     */
    public static final String getCommand(final FTPCommand command) {
        return command.command();
    }
}
