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


package org.netling.ftp.parser;

import java.text.ParseException;

import org.netling.ftp.FTPClientConfig;
import org.netling.ftp.FTPFile;

/**
 * @version $Id: OS400FTPEntryParser.java 658518 2008-05-21 01:04:30Z sebb $
 */

public class OS400FTPEntryParser extends ConfigurableFTPFileEntryParserImpl
{
    private static final String DEFAULT_DATE_FORMAT 
        = "yy/MM/dd HH:mm:ss"; //01/11/09 12:30:24
    


    private static final String REGEX =
        "(\\S+)\\s+"                // user
        + "(\\d+)\\s+"              // size
        + "(\\S+)\\s+(\\S+)\\s+"    // date stuff 
        + "(\\*\\S+)\\s+"               // *STMF/*DIR
        + "(\\S+/?)\\s*";               // filename

    
    /**
     * The default constructor for a OS400FTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public OS400FTPEntryParser()
    {
        this(null);
    }

    /**
     * This constructor allows the creation of an OS400FTPEntryParser object 
     * with something other than the default configuration.
     *
     * @param config The {@link FTPClientConfig configuration} object used to 
     * configure this parser.
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     * 
     */
    public OS400FTPEntryParser(FTPClientConfig config)
    {
        super(REGEX);
        configure(config);
    }


    public FTPFile parseFTPEntry(String entry)
    {

        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        FTPFile.Type type;

        if (matches(entry))
        {
            String usr = group(1);
            String filesize = group(2);
            String datestr = group(3)+" "+group(4);
            String typeStr = group(5);
            String name = group(6);
            
            try
            {
                file.setTimestamp(super.parseTimestamp(datestr));
            }
            catch (ParseException e)
            {
                // intentionally do nothing
            }

            if (typeStr.equalsIgnoreCase("*STMF"))
            {
                type = FTPFile.Type.FILE;
            }
            else if (typeStr.equalsIgnoreCase("*DIR"))
            {
                type = FTPFile.Type.DIRECTORY;
            }
            else
            {
                type = FTPFile.Type.UNKNOWN;
            }

            file.setType(type);

            file.setUser(usr);

            try
            {
                file.setSize(Long.parseLong(filesize));
            }
            catch (NumberFormatException e)
            {
                // intentionally do nothing
            }

            if (name.endsWith("/"))
            {
                name = name.substring(0, name.length() - 1);
            }
            int pos = name.lastIndexOf('/');
            if (pos > -1)
            {
                name = name.substring(pos + 1);
            }

            file.setName(name);

            return file;
        }
        return null;
    }

    /**
     * Defines a default configuration to be used when this class is
     * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
     * parameter being specified.
     * @return the default configuration for this parser.
     */
    @Override
    protected FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig(
                FTPClientConfig.SYST_OS400,
                DEFAULT_DATE_FORMAT,
                null, null, null, null);
    }

}
