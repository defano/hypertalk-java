package com.defano.hypertalk.ast.functions;

import com.defano.hypercard.runtime.context.HyperCardProperties;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.exception.HtException;
import org.antlr.v4.runtime.ParserRuleContext;

public class PropertyDelegatedFunc extends Expression {

    private final String propertyName;

    public PropertyDelegatedFunc(ParserRuleContext context, String propertyName) {
        super(context);
        this.propertyName = propertyName;
    }

    @Override
    protected Value onEvaluate() throws HtException {
        return HyperCardProperties.getInstance().getProperty(propertyName);
    }
}
