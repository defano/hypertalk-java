/*
 * NoSuchPropertyException
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * NoSuchPropertyException.java
 * @author matt.defano@gmail.com
 * 
 * Exception thrown when getting or setting a property that does not exist
 * for the part in which it was requested.
 */

package com.defano.hypertalk.exception;

/**
 * Indicates an attempt to access a property that does not exist on the requested object.
 */
public class NoSuchPropertyException extends HtSemanticException {
    public NoSuchPropertyException(String message) {
        super(message);
    }
}
