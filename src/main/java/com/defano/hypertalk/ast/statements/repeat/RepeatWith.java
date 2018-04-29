package com.defano.hypertalk.ast.statements.repeat;

public class RepeatWith extends RepeatSpecifier {

    public final String symbol;
    public final RepeatRange range;
    
    public RepeatWith (String symbol, RepeatRange range) {
        this.symbol = symbol;
        this.range = range;
    }
}
