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

/**
 * FTPS-specific command
 */
public enum FTPSCommand {
	AUTH("AUTH"),
    ADAT("ADAT"),
    PBSZ("PBSZ"),
    PROT("PROT"),
    CCC("CCC");

	/** The underlying FTPS command */
	private final String command;
	
	FTPSCommand(final String command) {
		this.command = command;
	}
	
	public String command() { return command; }
	
    /**
     * Retrieve the FTPS command string corresponding to a specified
     * command code.
     * <p>
     * @param command The command code.
     * @return The FTPS command string corresponding to a specified 
     *  command code.
     */
    public static final String getCommand(FTPSCommand command) {
        return command.command();
    }
}
