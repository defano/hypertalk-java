package com.defano.hypertalk.ast.common;

public class Convertible {

    public final ConvertibleDateFormat first;
    public final ConvertibleDateFormat second;

    public Convertible(ConvertibleDateFormat first) {
        this(first, null);
    }

    public Convertible(ConvertibleDateFormat first, ConvertibleDateFormat second) {
        this.first = first;
        this.second = second;
    }

}
