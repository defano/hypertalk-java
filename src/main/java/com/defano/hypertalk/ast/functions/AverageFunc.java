/*
 * ExpAverageFun
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:12 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * AverageFunc.java
 * @author matt.defano@gmail.com
 * 
 * Implementation for the built-in function "average"
 */

package com.defano.hypertalk.ast.functions;

import com.defano.hypertalk.ast.common.ExpressionList;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class AverageFunc extends ArgListFunction {

    public AverageFunc(ParserRuleContext context, ExpressionList argumentList) {
        super(context, argumentList);
    }

    public AverageFunc(ParserRuleContext context, Expression expression) {
        super(context, expression);
    }

    public Value onEvaluate() throws HtException {
        float sum = 0;
        List<Value> list = evaluateArgumentList();

        if (list.size() == 0) {
            return new Value(0);
        }
        
        for (Value item : list) {

            if (!item.isNumber()) {
                throw new HtSemanticException("All arguments to average must be numbers.");
            }
                
            sum += item.doubleValue();
        }
        
        return new Value(sum/list.size());
    }
}
