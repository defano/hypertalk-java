/*
 * ExpMouseFun
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * MouseFunc.java
 * @author matt.defano@gmail.com
 * 
 * Implementation of the built-in function "the mouse"
 */

package com.defano.hypertalk.ast.functions;

import com.defano.hypercard.awt.MouseManager;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import org.antlr.v4.runtime.ParserRuleContext;

public class MouseFunc extends Expression {

    public MouseFunc(ParserRuleContext context) {
        super(context);
    }
    
    public Value onEvaluate() {
        return MouseManager.isMouseDown() ? new Value("down") : new Value("up");
    }
}
