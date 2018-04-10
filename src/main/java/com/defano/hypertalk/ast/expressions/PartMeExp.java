package com.defano.hypertalk.ast.expressions;

import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.expressions.containers.PartExp;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

public class PartMeExp extends PartExp {

    public PartMeExp(ParserRuleContext context) {
        super(context);
    }

    public PartSpecifier evaluateAsSpecifier(ExecutionContext context)
    throws HtSemanticException
    {
        return context.getStackFrame().getMe();
    }
}
