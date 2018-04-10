package com.defano.hypertalk.ast.statements.commands;

import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.interpreter.Interpreter;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.ast.expressions.containers.PartExp;
import com.defano.hypertalk.ast.model.Script;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

public class SendCmd extends Command {

    public final Expression part;
    public final Expression message;

    public SendCmd(ParserRuleContext context, Expression part, Expression message) {
        super(context, "send");

        this.part = part;
        this.message = message;
    }

    public void onExecute(ExecutionContext context) throws HtException {
        PartExp factor = part.factor(context, PartExp.class, new HtSemanticException("Cannot send a message to that."));

        MessageCmd messageCmd = interpretMessage(message.evaluate(context).stringValue());
        if (messageCmd == null) {
            context.sendMessage(factor.evaluateAsSpecifier(context), message.evaluate(context).stringValue(), new ListExp(null));
        } else {
            messageCmd.onExecute(context);
        }
    }

    private MessageCmd interpretMessage(String message) {
        try {
            Script compiled = Interpreter.blockingCompileScriptlet(message);
            return (MessageCmd) compiled.getStatements().list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

}
