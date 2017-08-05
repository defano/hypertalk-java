/*
 * FunctionExecutionTask
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.runtime;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.context.ExecutionContext;
import com.defano.hypertalk.ast.common.NamedBlock;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.containers.PartSpecifier;
import com.defano.hypertalk.ast.common.ExpressionList;
import com.defano.hypertalk.exception.HtSemanticException;

import java.util.List;
import java.util.concurrent.Callable;

public class FunctionExecutionTask implements Callable<Value> {

    private final NamedBlock function;
    private final ExpressionList arguments;
    private final PartSpecifier me;

    public FunctionExecutionTask (PartSpecifier me, NamedBlock function, ExpressionList arguments) {
        this.function = function;
        this.arguments = arguments;
        this.me = me;

        if (function.parameters.list.size() != arguments.getArgumentCount())
            HyperCard.getInstance().showErrorDialog(new HtSemanticException("Function '" + function.name + "' expects " + function.parameters.list.size() + " arguments, but got " + arguments.getArgumentCount() + "."));
    }

    @Override
    public Value call() throws Exception {

        // Arguments passed to function must be evaluated in the context of the caller (i.e., before we push a new stack frame)
        List<Value> evaluatedArguments = arguments.evaluate();

        ExecutionContext.getContext().pushContext();
        ExecutionContext.getContext().setMe(me);

        try {
            // Bind argument values to parameter variables in this context
            for (int index = 0; index < function.parameters.list.size(); index++) {
                String theParam = function.parameters.list.get(index);
                Value theArg = evaluatedArguments.get(index);

                ExecutionContext.getContext().set(theParam, theArg);
            }
            
            function.statements.execute();
        
        } catch (HtSemanticException e) {
            HyperCard.getInstance().showErrorDialog(e);
        }

        Value returnValue = ExecutionContext.getContext().getReturnValue();
        ExecutionContext.getContext().popContext();
        
        return returnValue;
    }
}
