package com.defano.hypertalk.ast.expressions.containers;

import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.model.Preposition;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.exception.HtException;
import org.antlr.v4.runtime.ParserRuleContext;

public class VariableExp extends ContainerExp {

    private final String symbol;

    public VariableExp(ParserRuleContext context, String symbol) {
        super(context);
        this.symbol = symbol;
    }

    @Override
    public Value onEvaluate(ExecutionContext context) throws HtException {
        Value value = context.getVariable(symbol);
        return chunkOf(context, value, getChunk());
    }

    @Override
    public void putValue(ExecutionContext context, Value value, Preposition preposition) throws HtException {
        context.setVariable(symbol, preposition, getChunk(), value);
    }

}
