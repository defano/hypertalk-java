package com.defano.hypertalk.ast.expressions;

import com.defano.hypertalk.ast.model.Chunk;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.exception.HtException;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import org.antlr.v4.runtime.ParserRuleContext;

public class ChunkExp extends Expression {

    public final Chunk chunk;
    public final Expression expression;
    
    public ChunkExp(ParserRuleContext context, Chunk chunk, Expression expression) {
        super(context);
        this.chunk = chunk;
        this.expression = expression;
    }
    
    public Value onEvaluate(ExecutionContext context) throws HtException {
        return expression.onEvaluate(context).getChunk(context, chunk);
    }
}
