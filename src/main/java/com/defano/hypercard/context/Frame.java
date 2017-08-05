/*
 * Frame
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * Frame.java
 * @author matt.defano@gmail.com
 * 
 * Maintains the current local context; analagous to the current stack frame.
 */

package com.defano.hypercard.context;

import com.defano.hypertalk.ast.common.PassedCommand;
import com.defano.hypertalk.ast.common.Value;

import java.util.List;

public class Frame {

    public final SymbolTable symbols;
    private final List<String> globalsInScope;
    private PassedCommand passedCommand;
    private Value returnValue;
    
    public Frame(SymbolTable symbols, List<String> globalsInScope, Value returnValue) {
        this.symbols = symbols;
        this.globalsInScope = globalsInScope;
        this.returnValue = returnValue;
        
        // "it" is implemented as a global variable that's always in scope
        globalsInScope.add("it");
    }
    
    public void globalInScope (String symbol) {
        globalsInScope.add(symbol);
    }
    
    public boolean isGlobalInScope (String symbol) {
        return globalsInScope.contains(symbol);
    }
    
    public void setReturnValue (Value returnValue) {
        this.returnValue = returnValue;
    }
    
    public Value getReturnValue () {
        return returnValue;
    }

    public PassedCommand getPassedCommand() {
        return passedCommand;
    }

    public void setPassedCommand(PassedCommand passedCommand) {
        this.passedCommand = passedCommand;
    }
}
