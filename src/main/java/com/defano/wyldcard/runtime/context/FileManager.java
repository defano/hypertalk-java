package com.defano.wyldcard.runtime.context;

import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;

public interface FileManager {
    /**
     * Opens the file identified by filename and returns a FileHandle object referring to it.
     *
     * Note that this method only "logically" opens the file; it does not necessarily open the file on the filesystem.
     * The FileHandle will open and close the file physically as needed as read/write operations are made.
     *
     * @param filename The filename (or file path) to the requested file. If the file doesn't exist, it will be
     *                 created when written to.
     * @return A FileHandle to the requested file. Never null.
     */
    DefaultFileManager.FileHandle open(String filename);

    /**
     * Closes the file handle associated with filename. Physically closes the file on the filesystem if the handle
     * was holding it open.
     *
     * @param filename The file name or (file path) of the file that should be closed.
     * @throws HtSemanticException Thrown if the file is not open or if an error occurs writing buffered data to the
     * file
     */
    void close(String filename) throws HtException;

    /**
     * Gets the FileHandle associated with the file identified by filename, or null if the file is not
     * open.
     *
     * @param filename The file name (or path) of the file whose FileHandle should be returned.
     * @return The open FileHandle associated with the requested file or null if the file is not open.
     */
    DefaultFileManager.FileHandle getFileHandle(String filename);

    /**
     * Closes all open file handles. Useful when closing a stack and intending to flush any open files. Does not report
     * errors that may occur when closing and writing files.
     */
    void closeAll();
}
