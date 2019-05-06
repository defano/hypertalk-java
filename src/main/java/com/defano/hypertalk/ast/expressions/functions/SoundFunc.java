package com.defano.hypertalk.ast.expressions.functions;

import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.runtime.ExecutionContext;
import com.defano.wyldcard.sound.SoundManager;
import com.google.inject.Inject;
import org.antlr.v4.runtime.ParserRuleContext;

public class SoundFunc extends Expression {

    @Inject
    private SoundManager soundManager;

    public SoundFunc(ParserRuleContext context) {
        super(context);
    }

    @Override
    protected Value onEvaluate(ExecutionContext context) {
        return new Value(soundManager.getSound());
    }
}
