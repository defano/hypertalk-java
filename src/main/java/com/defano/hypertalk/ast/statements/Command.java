package com.defano.hypertalk.ast.statements;

import com.defano.hypertalk.ast.preemptions.Preemption;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.interpreter.MessageCompletionObserver;
import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.concurrent.CountDownLatch;

/**
 * Represents a HyperTalk command statement. HyperTalk commands are somewhat unusual in that their execution causes
 * a message to be sent the current card message hierarchy, and if the card, background, or stack traps the message,
 * then the command does not execute.
 *
 * This base class provides logic for sending the command's name to the current card hierarchy and invoking the
 * subclass's implementation of the command ({@link Statement#onExecute(ExecutionContext)}) if and only if the command was not trapped by the
 * card.
 */
public abstract class Command extends Statement implements MessageCompletionObserver {

    private final String messageName;
    private boolean trapped = false;
    private CountDownLatch cdl = new CountDownLatch(1);

    protected Command(ParserRuleContext context, String messageName) {
        super(context);
        this.messageName = messageName;
    }

    @Override
    public final void execute(ExecutionContext context) throws HtException, Preemption {
        cdl = new CountDownLatch(1);

        // Send command message to current card
        context.getCurrentCard().getPartModel().receiveMessage(context, messageName, getEvaluatedMessageArguments(context), this);

        // Wait for command handler to finish executing
        try {
            cdl.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Do not execute this command if handler trapped the message
        if (!trapped) {
            try {
                handleBreakpoints(context);
                onExecute(context);
            } catch (HtException e) {
                rethrowContextualizedException(context, e);
            }
        }
    }

    /**
     * Gets the arguments to be passed to the command handler. Override in subclasses for commands whose associated
     * message accepts arguments.
     *
     * @return The argument list
     * @throws HtSemanticException Thrown if an error occurs evaluating arguments
     * @param context The execution context.
     */
    protected ListExp getEvaluatedMessageArguments(ExecutionContext context) throws HtException {
        return new ListExp(null);
    }

    /** {@inheritDoc */
    @Override
    public void onMessagePassed(String message, boolean wasTrapped, HtException err) {
        this.trapped = wasTrapped;
        cdl.countDown();
    }
}
