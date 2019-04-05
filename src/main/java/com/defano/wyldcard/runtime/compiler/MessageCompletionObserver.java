package com.defano.wyldcard.runtime.compiler;

import com.defano.hypertalk.exception.HtException;
import com.defano.wyldcard.message.Message;

/**
 * A handler for HyperCard command script execution completion.
 */
public interface MessageCompletionObserver {

    /**
     * Invoked after a message has been sent to a part (and its message passing hierarchy, if the part did not provide
     * a handler for the message).
     *  @param message The command that was passed.
     * @param wasTrapped True if any part in the message passing hierarchy trapped the command, false otherwise.
     * @param error Non-null if an error occurred while handling the message
     */
    void onMessagePassed(Message message, boolean wasTrapped, HtException error);
}
