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


package org.netling;

import java.io.PrintWriter;
import org.netling.ProtocolCommandEvent;
import org.netling.ProtocolCommandListener;

/***
 * This is a support class for some of the example programs.  It is
 * a sample implementation of the ProtocolCommandListener interface
 * which just prints out to a specified stream all command/reply traffic.
 * <p>
 *
 * 
 ***/

public class PrintCommandListener implements ProtocolCommandListener
{
    private final PrintWriter writer;

    public PrintCommandListener(PrintWriter writer)
    {
        this.writer = writer;
    }

    public void protocolCommandSent(ProtocolCommandEvent event)
    {
        writer.print(event.getMessage());
        writer.flush();
    }

    public void protocolReplyReceived(ProtocolCommandEvent event)
    {
        writer.print(event.getMessage());
        writer.flush();
    }
}

