package com.defano.hypertalk.ast.statements.commands;

import com.defano.wyldcard.awt.MouseManager;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import org.antlr.v4.runtime.ParserRuleContext;

import java.awt.*;

public class DragCmd extends Command {

    private final Expression from;
    private final Expression to;
    private final Expression modifierKeys;

    public DragCmd(ParserRuleContext context, Expression from, Expression to) {
        super(context, "drag");

        this.from = from;
        this.to = to;
        this.modifierKeys = null;
    }

    public DragCmd(ParserRuleContext context, Expression from, Expression to, Expression modifierKeys) {
        super(context, "drag");

        this.from = from;
        this.to = to;
        this.modifierKeys = modifierKeys;
    }

    @Override
    public void onExecute(ExecutionContext context) throws HtException {
        boolean withShift = false;
        boolean withOption = false;
        boolean withCommand = false;

        if (modifierKeys != null) {
            for (Value thisModifier : modifierKeys.evaluate(context).getItems(context)) {
                withShift = thisModifier.equals(new Value("shiftKey")) || withShift;
                withOption = thisModifier.equals(new Value("optionKey")) || withOption;
                withCommand = (thisModifier.equals(new Value("commandKey")) || thisModifier.contains(new Value("cmdKey"))) || withCommand;
            }
        }

        Value from = this.from.evaluate(context);
        Value to = this.to.evaluate(context);

        if (!from.isPoint(context)) {
            throw new HtSemanticException(from.stringValue() + " is not a valid location.");
        }

        if (!to.isPoint(context)) {
            throw new HtSemanticException(to.stringValue() + " is not a valid location.");
        }

        int x1 = from.getItems(context).get(0).integerValue();
        int y1 = from.getItems(context).get(1).integerValue();
        int x2 = to.getItems(context).get(0).integerValue();
        int y2 = to.getItems(context).get(1).integerValue();

        MouseManager.getInstance().dragFrom(new Point(x1, y1), new Point(x2, y2), withShift, withOption, withCommand);
    }
}
