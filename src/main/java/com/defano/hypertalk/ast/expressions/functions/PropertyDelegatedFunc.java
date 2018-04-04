package com.defano.hypertalk.ast.expressions.functions;

import com.defano.wyldcard.runtime.HyperCardProperties;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.exception.HtException;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import org.antlr.v4.runtime.ParserRuleContext;

public class PropertyDelegatedFunc extends Expression {

    private final String propertyName;

    public PropertyDelegatedFunc(ParserRuleContext context, String propertyName) {
        super(context);
        this.propertyName = propertyName;
    }

    @Override
    protected Value onEvaluate(ExecutionContext context) throws HtException {
        return HyperCardProperties.getInstance().getProperty(context, propertyName);
    }
}
