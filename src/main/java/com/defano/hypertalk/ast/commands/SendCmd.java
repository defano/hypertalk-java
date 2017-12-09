package com.defano.hypertalk.ast.commands;

import com.defano.hypercard.runtime.Interpreter;
import com.defano.hypercard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.common.Script;
import com.defano.hypertalk.ast.containers.PartContainerExp;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;

public class SendCmd extends Command {

    public final Expression part;
    public final Expression message;

    public SendCmd(ParserRuleContext context, Expression part, Expression message) {
        super(context, "send");

        this.part = part;
        this.message = message;
    }

    public void onExecute() throws HtException {
        PartContainerExp factor = part.factor(PartContainerExp.class, new HtSemanticException("Cannot send a message to that."));
        ExecutionContext.getContext().pushMe(factor.evaluateAsSpecifier());

        MessageCmd messageCmd = interpretMessage(message.evaluate().stringValue());
        if (messageCmd == null) {
            ExecutionContext.getContext().sendMessage(factor.evaluateAsSpecifier(), message.evaluate().stringValue(), new ArrayList<>());
        } else {
            messageCmd.onExecute();
        }

        ExecutionContext.getContext().popMe();
    }

    private MessageCmd interpretMessage(String message) {
        try {
            Script compiled = Interpreter.compileScriptlet(message);
            return (MessageCmd) compiled.getStatements().list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

}
