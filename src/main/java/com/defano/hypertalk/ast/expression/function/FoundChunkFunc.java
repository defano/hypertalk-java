package com.defano.hypertalk.ast.expression.function;

import com.defano.hypertalk.ast.expression.Expression;
import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.runtime.ExecutionContext;
import com.defano.wyldcard.search.SearchManager;
import com.google.inject.Inject;
import org.antlr.v4.runtime.ParserRuleContext;

public class FoundChunkFunc extends Expression {

    @Inject
    private SearchManager searchManager;

    public FoundChunkFunc(ParserRuleContext context) {
        super(context);
    }

    @Override
    protected Value onEvaluate(ExecutionContext context) {
        return searchManager.getFoundChunk();
    }
}
