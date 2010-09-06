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

/***
 * FTPReply stores a set of constants for FTP reply codes.  To interpret
 * the meaning of the codes, familiarity with RFC 959 is assumed.
 * The mnemonic constant names are transcriptions from the code descriptions
 * of RFC 959.  For those who think in terms of the actual reply code values,
 * a set of CODE_NUM constants are provided where NUM is the numerical value
 * of the code.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @author Rory Winston
 ***/

public enum FTPReply
{
	RESTART_MARKER(110),
	SERVICE_NOT_READY(120),
	DATA_CONNECTION_ALREADY_OPEN(125),
	FILE_STATUS_OK(150),
	COMMAND_OK(200),
	COMMAND_IS_SUPERFLUOUS(202),
	SYSTEM_STATUS(211),
	DIRECTORY_STATUS(212),
	FILE_STATUS(213),
	HELP_MESSAGE(214),
	NAME_SYSTEM_TYPE(215),
	SERVICE_READY(220),
	SERVICE_CLOSING_CONTROL_CONNECTION(221),
	DATA_CONNECTION_OPEN(225),
	CLOSING_DATA_CONNECTION(226),
	ENTERING_PASSIVE_MODE(227),
	ENTERING_EPSV_MODE(229),
	USER_LOGGED_IN(230),
	FILE_ACTION_OK(250),
	PATHNAME_CREATED(257),
	NEED_PASSWORD(331),
	NEED_ACCOUNT(332),
	FILE_ACTION_PENDING(350),
	SERVICE_NOT_AVAILABLE(421),
	CANNOT_OPEN_DATA_CONNECTION(425),
	TRANSFER_ABORTED(426),
	FILE_ACTION_NOT_TAKEN(450),
	ACTION_ABORTED(451),
	INSUFFICIENT_STORAGE(452),
	UNRECOGNIZED_COMMAND(500),
	SYNTAX_ERROR_IN_ARGUMENTS(501),
	COMMAND_NOT_IMPLEMENTED(502),
	BAD_COMMAND_SEQUENCE(503),
	COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER(504),
	NOT_LOGGED_IN(530),
	NEED_ACCOUNT_FOR_STORING_FILES(532),
	FILE_UNAVAILABLE(550),
	PAGE_TYPE_UNKNOWN(551),
	STORAGE_ALLOCATION_EXCEEDED(552),
	FILE_NAME_NOT_ALLOWED(553),

	// FTPS return codes
	SECURITY_DATA_EXCHANGE_COMPLETE(234),
	SECURITY_DATA_EXCHANGE_SUCCESSFULLY(235),
	SECURITY_MECHANISM_IS_OK(334),
	SECURITY_DATA_IS_ACCEPTABLE(335),
	UNAVAILABLE_RESOURCE(431),
	BAD_TLS_NEGOTIATION_OR_DATA_ENCRYPTION_REQUIRED(522),
	DENIED_FOR_POLICY_REASONS(533),
	REQUEST_DENIED(534),
	FAILED_SECURITY_CHECK(535),
	REQUESTED_PROT_LEVEL_NOT_SUPPORTED(536),

	// IPv6 error codes
	// Note this is also used as an FTPS error code reply
	EXTENDED_PORT_FAILURE(522);


	private int code;

	FTPReply(int code) { this.code = code; }

	public int code() { return code; }

	/***
	 * Determine if a reply code is a positive preliminary response.  All
	 * codes beginning with a 1 are positive preliminary responses.
	 * Postitive preliminary responses are used to indicate tentative success.
	 * No further commands can be issued to the FTP server after a positive
	 * preliminary response until a follow up response is received from the
	 * server.
	 * <p>
	 * @param reply  The reply code to test.
	 * @return True if a reply code is a postive preliminary response, false
	 *         if not.
	 ***/
	public static boolean isPositivePreliminary(int reply)
	{
		return (reply >= 100 && reply < 200);
	}

	/***
	 * Determine if a reply code is a positive completion response.  All
	 * codes beginning with a 2 are positive completion responses.
	 * The FTP server will send a positive completion response on the final
	 * successful completion of a command.
	 * <p>
	 * @param reply  The reply code to test.
	 * @return True if a reply code is a postive completion response, false
	 *         if not.
	 ***/
	public static boolean isPositiveCompletion(int reply)
	{
		return (reply >= 200 && reply < 300);
	}

	/***
	 * Determine if a reply code is a positive intermediate response.  All
	 * codes beginning with a 3 are positive intermediate responses.
	 * The FTP server will send a positive intermediate response on the
	 * successful completion of one part of a multi-part sequence of
	 * commands.  For example, after a successful USER command, a positive
	 * intermediate response will be sent to indicate that the server is
	 * ready for the PASS command.
	 * <p>
	 * @param reply  The reply code to test.
	 * @return True if a reply code is a postive intermediate response, false
	 *         if not.
	 ***/
	public static boolean isPositiveIntermediate(int reply)
	{
		return (reply >= 300 && reply < 400);
	}

	/***
	 * Determine if a reply code is a negative transient response.  All
	 * codes beginning with a 4 are negative transient responses.
	 * The FTP server will send a negative transient response on the
	 * failure of a command that can be reattempted with success.
	 * <p>
	 * @param reply  The reply code to test.
	 * @return True if a reply code is a negative transient response, false
	 *         if not.
	 ***/
	public static boolean isNegativeTransient(int reply)
	{
		return (reply >= 400 && reply < 500);
	}

	/***
	 * Determine if a reply code is a negative permanent response.  All
	 * codes beginning with a 5 are negative permanent responses.
	 * The FTP server will send a negative permanent response on the
	 * failure of a command that cannot be reattempted with success.
	 * <p>
	 * @param reply  The reply code to test.
	 * @return True if a reply code is a negative permanent response, false
	 *         if not.
	 ***/
	public static boolean isNegativePermanent(int reply)
	{
		return (reply >= 500 && reply < 600);
	}

}
