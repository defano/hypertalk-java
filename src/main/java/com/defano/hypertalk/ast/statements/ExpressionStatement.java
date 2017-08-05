/*
 * StatExp
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * ExpressionStatement.java
 * @author matt.defano@gmail.com
 * 
 * Encapsulation of an expression statement
 */

package com.defano.hypertalk.ast.statements;

import com.defano.hypercard.context.ExecutionContext;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.exception.HtSemanticException;

public class ExpressionStatement extends Statement {

    public final Expression expression;
    
    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }
    
    public void execute () throws HtSemanticException {
        Value v = expression.evaluate();
        ExecutionContext.getContext().setIt(v);
    }
}
