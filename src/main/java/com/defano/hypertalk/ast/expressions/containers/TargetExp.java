package com.defano.hypertalk.ast.expressions.containers;

import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import org.antlr.v4.runtime.ParserRuleContext;

public class TargetExp extends PartExp {

    public TargetExp(ParserRuleContext context) {
        super(context);
    }

    @Override
    public PartSpecifier evaluateAsSpecifier(ExecutionContext context) {
        return context.getTarget();
    }
}
