package com.defano.wyldcard.parts;

import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.ast.model.*;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.awt.KeyboardManager;
import com.defano.wyldcard.runtime.HyperCardProperties;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.interpreter.Interpreter;
import com.defano.wyldcard.runtime.interpreter.MessageCompletionObserver;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Represents an object that can receive HyperTalk messages.
 */
public interface Messagable {

    /**
     * Gets the script associated with this part.
     *
     * @return The script
     * @param context The execution context.
     */
    Script getScript(ExecutionContext context);

    /**
     * Gets a part specifier that uniquely identifies this part in the stack. This part will be bound to the 'me'
     * keyword in the script that receives messages.
     *
     * @return The part specifier for the 'me' keyword.
     * @param context The execution context.
     */
    PartSpecifier getMe(ExecutionContext context);

    /**
     * Sends a message (i.e., 'mouseUp') to this part's message passing hierarchy.
     *
     * @param context The execution context
     * @param message The message to be passed.
     */
    default void receiveMessage(ExecutionContext context, String message) {
        receiveMessage(context, message, new ListExp(null), (command, trapped, err) -> {
            if (err != null) {
                WyldCard.getInstance().showErrorDialog(err);
            }
        });
    }

    /**
     * Sends a message with bound arguments (i.e., 'doMenu') to this part's message passing hierarchy.
     *
     * @param context The execution context
     * @param message   The message to be passed
     * @param arguments The arguments to the message
     */
    default void receiveMessage(ExecutionContext context, String message, ListExp arguments) {
        receiveMessage(context, message, arguments, (command, trapped, err) -> {
            if (err != null) {
                WyldCard.getInstance().showErrorDialog(err);
            }
        });
    }

    /**
     * Sends a message with arguments (i.e., 'doMenu theMenu, theItem') to this part's message passing hierarchy.
     *
     * @param context The execution context
     * @param message      The name of the command; cannot be null.
     * @param arguments    The arguments to pass to this command; cannot be null.
     * @param onCompletion A callback that will fire as soon as the command has been executed in script; cannot be null.
*                     Note that this callback will not fire if the script terminates as a result of an error or
     */
    default void receiveMessage(ExecutionContext context, String message, ListExp arguments, MessageCompletionObserver onCompletion) {

        // No messages are sent cmd-option is down; some messages not sent when 'lockMessages' is true
        if (KeyboardManager.getInstance().isCommandOptionDown() ||
                (SystemMessage.isLockable(message)) && HyperCardProperties.getInstance().getKnownProperty(context, HyperCardProperties.PROP_LOCKMESSAGES).booleanValue())
        {
            onCompletion.onMessagePassed(message, false, null);
            return;
        }

        // Attempt to invoke command handler in this part and listen for completion
        Interpreter.asyncExecuteHandler(context, getMe(context), getScript(context), message, arguments, (me, script, handler, trappedMessage) -> {
            // Did this part trap this command?
            if (trappedMessage) {
                onCompletion.onMessagePassed(message, true, null);
            } else {
                // Get next recipient in message passing order; null if no other parts receive message
                Messagable nextRecipient = getNextMessageRecipient(context, getMe(context).getType());
                if (nextRecipient == null) {
                    onCompletion.onMessagePassed(message, false, null);
                } else {
                    nextRecipient.receiveMessage(context, message, arguments, onCompletion);
                }
            }
        });
    }

    /**
     * Sends a message to this part, and if the part (or any part in the message passing hierarchy) traps the command,
     * then the given key event is consumed ({@link InputEvent#consume()}.
     * <p>
     * In order to prevent Swing from acting on the event naturally, this method consumes the given KeyEvent and
     * re-dispatches a copy of it if this part (or any part in its message passing hierarchy) doesn't trap the message.
     * <p>
     * In order to prevent the re-dispatched event from producing a recursive call back to this method, the
     * {@link DeferredKeyEventComponent#setPendingRedispatch(boolean)} is invoked with 'true' initially, then invoked
     * with 'false' after the message has been completely received.
     *
     * @param context   The execution context
     * @param command   The name of the command
     * @param arguments The arguments to pass to this command
     * @param e         The input event to consume if the command is trapped by the part (or fails to invoke 'pass') within
     */
    default void receiveAndDeferKeyEvent(ExecutionContext context, String command, ListExp arguments, KeyEvent e, DeferredKeyEventComponent c) {
        InputEvent eventCopy = new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
        e.consume();

        c.setPendingRedispatch(true);
        receiveMessage(context, command, arguments, (command1, wasTrapped, error) -> {
            if (!wasTrapped) {
                c.dispatchEvent(eventCopy);
            }

            c.setPendingRedispatch(false);
        });
    }

    /**
     * Invokes a function defined in the part's script, blocking until the function completes.
     *
     *
     * @param context The execution context
     * @param functionName The name of the function to execute.
     * @param arguments    The arguments to the function.
     * @return The value returned by the function upon completion.
     * @throws HtSemanticException Thrown if a syntax or semantic error occurs attempting to execute the function.
     */
    default Value invokeFunction(ExecutionContext context, String functionName, Expression arguments) throws HtException {
        UserFunction function = getScript(context).getFunction(functionName);
        Messagable target = this;

        while (function == null) {
            // Get next part is message passing hierarchy
            target = getNextMessageRecipient(context, target.getMe(context).getType());

            // No more scripts to search; error!
            if (target == null) {
                throw new HtSemanticException("No such function " + functionName + ".");
            }

            // Look for function in this script
            function = target.getScript(context).getFunction(functionName);
        }

        return Interpreter.blockingExecuteFunction(context, target.getMe(context), function, arguments);
    }

    /**
     * Gets the next part in the message passing order.
     *
     * @param context The execution context
     * @return The next messagable part in the message passing order, or null, if we've reached the last object in the
     * hierarchy.
     */
    default Messagable getNextMessageRecipient(ExecutionContext context, PartType type) {

        switch (type) {
            case BACKGROUND:
                return WyldCard.getInstance().getActiveStack().getStackModel();
            case MESSAGE_BOX:
                return context.getCurrentCard().getCardModel();
            case CARD:
                return context.getCurrentCard().getCardModel().getBackgroundModel();
            case STACK:
                return null;
            case FIELD:
            case BUTTON:
                if (getMe(context).getOwner() == Owner.BACKGROUND) {
                    return context.getCurrentCard().getCardModel().getBackgroundModel();
                } else {
                    return context.getCurrentCard().getCardModel();
                }
        }

        throw new IllegalArgumentException("Bug! Don't know what the next message recipient is for: " + getMe(context).getOwner());
    }
}
