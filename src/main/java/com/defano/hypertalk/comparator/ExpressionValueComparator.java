package com.defano.hypertalk.comparator;

import com.defano.hypercard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.exception.HtSemanticException;

import java.util.Comparator;

public class ExpressionValueComparator implements Comparator<Value> {

    private final Expression expression;
    private final SortStyle sortStyle;

    public ExpressionValueComparator(Expression expression, SortStyle sortStyle) {
        this.expression = expression;
        this.sortStyle = sortStyle;
    }

    @Override
    public int compare(Value o1, Value o2) {
        try {
            ExecutionContext.getContext().set("each", o1);
            Value o1Evaluated = expression.evaluate();

            ExecutionContext.getContext().set("each", o2);
            Value o2Evaluated = expression.evaluate();

            return o1Evaluated.compareTo(o2Evaluated, sortStyle);
        } catch (HtSemanticException e) {
            // TODO: How best to handle semantic error during comparison?
            return 0;
        }
    }
}
