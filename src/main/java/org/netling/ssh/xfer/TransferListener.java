package org.netling.ssh.xfer;

import org.netling.ssh.common.StreamCopier;

public interface TransferListener
        extends StreamCopier.Listener {

    void startedDir(String name);

    void startedFile(String name, long size);

    void finishedFile();

    void finishedDir();

}
