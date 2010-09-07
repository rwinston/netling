package org.netling.xfer;

import org.netling.io.StreamCopier;

public interface TransferListener
        extends StreamCopier.Listener {

    void startedDir(String name);

    void startedFile(String name, long size);

    void finishedFile();

    void finishedDir();

}
