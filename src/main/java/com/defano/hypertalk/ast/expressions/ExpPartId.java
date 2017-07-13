/*
 * ExpPartId
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:12 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * ExpPartId.java
 * @author matt.defano@gmail.com
 * 
 * Encapsulation of an id-based part specification, for example: "button id 12"
 */

package com.defano.hypertalk.ast.expressions;

import com.defano.hypercard.context.GlobalContext;
import com.defano.hypercard.parts.PartException;
import com.defano.hypertalk.ast.common.PartLayer;
import com.defano.hypertalk.ast.common.PartType;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.containers.PartIdSpecifier;
import com.defano.hypertalk.ast.containers.PartSpecifier;
import com.defano.hypertalk.exception.HtSemanticException;

public class ExpPartId extends ExpPart {

    public final PartLayer layer;
    public final PartType type;
    public final Expression id;
    
    public ExpPartId (PartLayer layer, PartType type, Expression id) {
        this.layer = layer;
        this.type = type;
        this.id = id;
    }
    
    public Value evaluate () throws HtSemanticException {
        try {
            return GlobalContext.getContext().get(evaluateAsSpecifier()).getValue();
        } catch (PartException e) {
            throw new HtSemanticException("Can't get that part.");
        }
    }
    
    public PartSpecifier evaluateAsSpecifier () 
    throws HtSemanticException
    {        
        return new PartIdSpecifier(layer, type, id.evaluate().integerValue());
    }
    
}
