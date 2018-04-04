package com.defano.hypertalk.ast.expressions.containers;

import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.window.WindowManager;
import com.defano.wyldcard.window.forms.MessageWindow;
import com.defano.hypertalk.ast.model.Preposition;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.ast.model.PartType;
import org.antlr.v4.runtime.ParserRuleContext;

import javax.swing.*;

public class MsgBoxExp extends ContainerExp {

    public MsgBoxExp(ParserRuleContext context) {
        super(context);
    }

    @Override
    public Value onEvaluate(ExecutionContext context) throws HtException {
        Value value = new Value(WindowManager.getInstance().getMessageWindow().getMsgBoxText());
        return chunkOf(context, value, getChunk());
    }

    @Override
    public void putValue(ExecutionContext context, Value value, Preposition preposition) throws HtException {
        Value destValue = new Value(WindowManager.getInstance().getMessageWindow().getMsgBoxText());

        // Operating on a chunk of the existing value
        if (getChunk() != null)
            destValue = Value.setChunk(context, destValue, preposition, getChunk(), value);
        else
            destValue = Value.setValue(destValue, preposition, value);

        WindowManager.getInstance().getMessageWindow().setMsgBoxText(destValue.stringValue());
        context.setIt(destValue);

        // If message is hidden, show it but don't focus it
        if (!WindowManager.getInstance().getMessageWindow().isVisible()) {
            SwingUtilities.invokeLater(() -> {
                MessageWindow message = WindowManager.getInstance().getMessageWindow();
                message.setFocusableWindowState(false);
                message.setVisible(true);
                message.setFocusableWindowState(true);
            });
        }
    }

    public PartType type() {
        return PartType.MESSAGE_BOX;
    }
}
