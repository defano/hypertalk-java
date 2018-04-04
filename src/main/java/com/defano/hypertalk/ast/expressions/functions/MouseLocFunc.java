package com.defano.hypertalk.ast.expressions.functions;

import com.defano.wyldcard.awt.MouseManager;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import org.antlr.v4.runtime.ParserRuleContext;

public class MouseLocFunc extends Expression {

    public MouseLocFunc(ParserRuleContext context) {
        super(context);
    }
    
    public Value onEvaluate(ExecutionContext context) {
        return new Value(MouseManager.getInstance().getMouseLoc());
    }
}
