package com.defano.wyldcard.runtime.interpreter;

import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.preemptions.Preemption;
import com.defano.hypertalk.ast.statements.ExpressionStatement;
import com.defano.hypertalk.ast.statements.Statement;
import com.defano.hypertalk.ast.statements.StatementList;
import com.defano.hypertalk.ast.statements.commands.MessageCmd;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.runtime.context.ExecutionContext;

import java.util.concurrent.Callable;

public class MessageEvaluationTask implements Callable<String> {

    private final ExecutionContext context;
    private final String messageText;

    public MessageEvaluationTask(ExecutionContext staticContext, String messageText) {
        this.context = staticContext;
        this.messageText = messageText;
    }

    @Override
    public String call() throws HtException {
        StatementList statements = Interpreter.blockingCompileScriptlet(messageText).getStatements();

        if (context.getStackDepth() == 0) {
            context.pushStackFrame();
        }

        context.setTarget(WyldCard.getInstance().getFocusedCard().getCardModel().getPartSpecifier(context));

        try {
            statements.execute(context);
        } catch (Preemption e) {
            return null;            // Can't really exit from here; just return null
        }

        Statement lastStatement = statements.list.get(statements.list.size() - 1);

        // Special case: If last statement was an unknown literal (interpreted as a message), then return the variable-
        // evaluation of that literal
        if (lastStatement instanceof MessageCmd) {
            return context.getVariable(lastStatement.getToken().getText()).stringValue();
        }

        // When the last statement is an expression, return the result of evaluating the expression
        else if (lastStatement instanceof ExpressionStatement) {
            return context.getIt().stringValue();
        }

        // Special case: Command entered into the message provided a result; treat as error
        else if (!context.getResult().isEmpty()) {
            String resultError = context.getResult().stringValue();
            context.setResult(new Value());
            throw new HtSemanticException(resultError);
        }

        // No guesses.
        else {
            return null;
        }
    }

}
