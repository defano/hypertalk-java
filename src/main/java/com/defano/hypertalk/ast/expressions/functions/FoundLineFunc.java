package com.defano.hypertalk.ast.expressions.functions;

import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.search.SearchManager;
import com.google.inject.Inject;
import org.antlr.v4.runtime.ParserRuleContext;

public class FoundLineFunc extends Expression {

    @Inject
    private SearchManager searchManager;

    public FoundLineFunc(ParserRuleContext context) {
        super(context);
    }

    @Override
    protected Value onEvaluate(ExecutionContext context) {
        return searchManager.getFoundLine();
    }
}