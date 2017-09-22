/*
 * StatGetCmd
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * GetCmd.java
 * @author matt.defano@gmail.com
 * 
 * Encapsulation of the "get" statement
 */

package com.defano.hypertalk.ast.commands;

import com.defano.hypercard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.containers.PartSpecifier;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtSemanticException;

public class GetCmd extends Command {

    public final Expression expression;
    public final PartSpecifier part;
    
    public GetCmd(Expression e) {
        super("get");

        expression = e;
        part = null;
    }
    
    public GetCmd(PartSpecifier ps) {
        super("get");

        expression = null;
        part = ps;
    }
    
    public void onExecute () throws HtSemanticException {
        if (expression != null) {
            ExecutionContext.getContext().setIt(expression.evaluate());
        }
    }
}