/*
 * StatRepeat
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * RepeatStatement.java
 *
 * @author matt.defano@gmail.com
 * <p>
 * Encapsulation of a repeat statement
 */

package com.defano.hypertalk.ast.statements;

import com.defano.hypercard.context.ExecutionContext;
import com.defano.hypercard.gui.util.KeyboardManager;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.ast.constructs.RepeatCount;
import com.defano.hypertalk.ast.constructs.RepeatDuration;
import com.defano.hypertalk.ast.constructs.RepeatForever;
import com.defano.hypertalk.ast.constructs.RepeatRange;
import com.defano.hypertalk.ast.constructs.RepeatSpecifier;
import com.defano.hypertalk.ast.constructs.RepeatWith;
import com.defano.hypertalk.exception.HtSemanticException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class RepeatStatement extends Statement {

    public final RepeatSpecifier range;
    public final StatementList statements;

    public RepeatStatement(RepeatSpecifier range, StatementList statements) {
        this.range = range;
        this.statements = statements;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void execute() throws HtException {
        if (range instanceof RepeatForever) {
            while (true) {
                statements.execute();
                rest();
            }
        } else if (range instanceof RepeatCount) {
            RepeatCount count = (RepeatCount) range;
            Value countValue = count.count.evaluate();

            if (!countValue.isNatural())
                throw new HtSemanticException("Repeat range must be a natural number, got '" + countValue + "' instead.");

            int countIndex = countValue.integerValue();
            while (countIndex-- > 0) {
                statements.execute();
                rest();
            }
        } else if (range instanceof RepeatDuration) {
            RepeatDuration duration = (RepeatDuration) range;

            // While loop
            if (duration.polarity == RepeatDuration.POLARITY_WHILE) {
                while (duration.condition.evaluate().booleanValue()) {
                    statements.execute();
                    rest();
                }
            }

            // Until loop
            if (duration.polarity == RepeatDuration.POLARITY_UNTIL) {
                while (!duration.condition.evaluate().booleanValue()) {
                    statements.execute();
                    rest();
                }
            }
        } else if (range instanceof RepeatWith) {
            RepeatWith with = (RepeatWith) range;
            String symbol = with.symbol;
            RepeatRange range = with.range;

            Value fromValue = range.from.evaluate();
            Value toValue = range.to.evaluate();

            if (!fromValue.isInteger())
                throw new HtSemanticException("Start of repeat range is not an integer value: '" + fromValue + "'");
            if (!toValue.isInteger())
                throw new HtSemanticException("End of repeat range is not an integer value: '" + toValue + "'");

            int from = fromValue.integerValue();
            int to = toValue.integerValue();

            if (range.polarity == RepeatRange.POLARITY_UPTO) {

                if (from > to)
                    throw new HtSemanticException("Start of repeat range is greater than end: " + from + " > " + to);

                for (int index = from; index <= to; index++) {
                    ExecutionContext.getContext().set(symbol, new Value(index));
                    statements.execute();
                    rest();
                }
            }

            else if (range.polarity == RepeatRange.POLARITY_DOWNTO) {
                if (to > from)
                    throw new HtSemanticException("End of repeat range is less than start: " + to + " > " + from);

                for (int index = from; index >= to; index--) {
                    ExecutionContext.getContext().set(symbol, new Value(index));
                    statements.execute();
                    rest();
                }
            }
        } else
            throw new RuntimeException("Unknown repeat type");
    }

    private void rest() throws HtException {

        if (KeyboardManager.isBreakSequence) {
            throw new HtSemanticException("Script aborted.");
        } else {
            try {
                // Flush the Swing UI event queue
                SwingUtilities.invokeAndWait(() -> {});
            } catch (InterruptedException | InvocationTargetException e) {
                // Nothing to do
            }
        }
    }
}
