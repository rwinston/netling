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
 */
package org.netling.sftp;

import org.netling.concurrent.Future;

public class Request
        extends SFTPPacket<Request> {

    private final PacketType type;
    private final long reqID;
    private final Future<Response, SFTPException> responseFuture;

    public Request(PacketType type, long reqID) {
        super(type);
        this.type = type;
        this.reqID = reqID;
        responseFuture = new Future<Response, SFTPException>("sftp / " + reqID, SFTPException.chainer);
        putInt(reqID);
    }

    public long getRequestID() {
        return reqID;
    }

    public PacketType getType() {
        return type;
    }

    public Future<Response, SFTPException> getResponseFuture() {
        return responseFuture;
    }

    @Override
    public String toString() {
        return "Request{" + reqID + ";" + type + "}";
    }

}
