/**
 * Value.java
 * @author matt.defano@gmail.com
 * 
 * Representation of value in HyperTalk; all values are stored internally
 * as Strings and converted to integers, floats or booleans as required by
 * the expression. 
 * 
 * The Value object is immutable; once created it cannot change value. 
 */

package hypertalk.ast.common;

import java.awt.*;
import java.util.List;
import java.util.Vector;

import hypertalk.ast.containers.Preposition;
import hypertalk.exception.HtSemanticException;
import hypertalk.utils.ChunkUtils;

public class Value {

    private final String value;

    // Cache for known value types
    private Long longValue;
    private Double floatValue;
    private Boolean booleanValue;
    
    public Value () {
        this("");
    }

    public Value (Point p) {
        this(p.x + "," + p.y);
    }

    public Value (Object v) {
        this(String.valueOf(v));
    }
    
    public Value (long v) {
        this(String.valueOf(v));
    }
    
    public Value (double f) {
        this(String.valueOf(f));
    }
    
    public Value (boolean v) {
        this(String.valueOf(v));
    }

    public Value (int x, int y) {
        this(String.valueOf(x) + "," + String.valueOf(y));
    }

    public Value (int x1, int y1, int x2, int y2) {
        this(String.valueOf(x1) + "," + String.valueOf(y1) + "," + String.valueOf(x2) + "," + String.valueOf(y2));
    }

    public Value (String value) {
        this.value = value;

        // Special case: empty string is a valid int and float
        if (value.trim().equals("")) {
            longValue = 0L;
            floatValue = 0.0;
            booleanValue = null;
        }

        else {
            try {
                longValue = Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                longValue = null;
            }

            try {
                floatValue = Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                floatValue = null;
            }

            if (value.trim().equalsIgnoreCase("true") || value.trim().equalsIgnoreCase("false")) {
                booleanValue = Boolean.parseBoolean(value);
            } else {
                booleanValue = null;
            }
        }
    }
    
    public boolean isInteger () {
        return longValue != null;
    }

    public boolean isFloat () {
        return floatValue != null;
    }    
    
    public boolean isNatural () {
        return isInteger() && longValue() >= 0;
    }    

    public boolean isBoolean () {
        return booleanValue != null;
    }    
    
    public boolean isNumber () {
        return isFloat();
    }

    public boolean isPoint () {
        List<Value> listValue = listValue();

        return listValue.size() == 2 &&
                new Value(listValue.get(0)).isInteger() &&
                new Value(listValue.get(1)).isInteger();
    }

    public boolean isRect () {
        List<Value> listValue = listValue();

        return listValue.size() == 4 &&
                new Value(listValue.get(0)).isInteger() &&
                new Value(listValue.get(1)).isInteger() &&
                new Value(listValue.get(2)).isInteger() &&
                new Value(listValue.get(3)).isInteger();
    }

    public String stringValue () {
        return value;
    }    

    public int integerValue() {
        if (longValue != null) {
            return longValue.intValue();
        } else {
            return 0;
        }
    }

    public long longValue() {
        if (longValue != null)
            return longValue;
        else
            return 0;
    }
        
    public double doubleValue() {
        if (floatValue != null)
            return floatValue;
        else
            return 0.0f;
    }
        
    public boolean booleanValue () {
        if (booleanValue != null)
            return booleanValue;
        else
            return false;
    }
            
    public List<Value> listValue () {
        List<Value> list = new Vector<>();
        
        for (String item : value.split(","))
            list.add(new Value(item));
            
        return list;
    }

    public Value listItemAt(int index) {
        if (listValue().size() > index) {
            return new Value(listValue().get(index));
        } else {
            return new Value();
        }
    }

    public int itemCount () {
        return ChunkUtils.getCount(ChunkType.ITEM, value);
    }
    
    public int wordCount () {
        return ChunkUtils.getCount(ChunkType.WORD, value);
    }
    
    public int charCount () {
        return ChunkUtils.getCount(ChunkType.CHAR, value);
    }
    
    public int lineCount () {
        return ChunkUtils.getCount(ChunkType.LINE, value);
    }
    
    public Value getChunk (Chunk c) throws HtSemanticException {

        Value startVal = null;
        Value endVal = null;
        
        int startIdx = 0;
        int endIdx = 0;
        
        if (c.start != null)
            startVal = c.start.evaluate();
        if (c.end != null)
            endVal = c.end.evaluate();
                        
        if (!startVal.isNatural() && !startVal.equals(Ordinal.MIDDLE.value()))
            throw new HtSemanticException("Chunk specifier requires natural integer value, got '" + startVal + "' instead");
        if (endVal != null && !endVal.isNatural() && !endVal.equals(Ordinal.MIDDLE.value()))
            throw new HtSemanticException("Chunk specifier requires natural integer value, got '" + endVal + "' instead");

        if (startVal != null)
            startIdx = startVal.integerValue();
        if (endVal != null)
            endIdx = endVal.integerValue();

        Value chunkValue = new Value(ChunkUtils.getChunk(c.type, value, startIdx, endIdx));

        // If a composite chunk; evaluate right hand of the expression first
        if (c instanceof CompositeChunk) {
            return chunkValue.getChunk(((CompositeChunk) c).chunkOf);
        } else {
            return chunkValue;
        }
    }

    public static Value setChunk (Value mutable, Preposition p, Chunk c, Object mutator) throws HtSemanticException {

        if (c instanceof CompositeChunk) {
            return new Value(ChunkUtils.putCompositeChunk((CompositeChunk) c, p, mutable.stringValue(), String.valueOf(mutator)));
        }

        String mutatorString = mutator.toString();
        String mutableString = mutable.toString();

        Value startVal = null;
        Value endVal = null;

        int startIdx = 0;
        int endIdx = 0;
        
        if (c.start != null)
            startVal = c.start.evaluate();
        if (c.end != null)
            endVal = c.end.evaluate();
        
        if (!startVal.isNatural() && !startVal.equals(Ordinal.MIDDLE.value()))
            throw new HtSemanticException("Chunk specifier requires natural integer value, got '" + startVal + "' instead");
        if (endVal != null && !endVal.isNatural() && !endVal.equals(Ordinal.MIDDLE.value()))
            throw new HtSemanticException("Chunk specifier requires natural integer value, got '" + endVal + "' instead");
        
        if (startVal != null)
            startIdx = startVal.integerValue();
        if (endVal != null)
            endIdx = endVal.integerValue();

        return new Value(ChunkUtils.putChunk(c.type, p, mutableString, startIdx, endIdx, mutatorString));
    }
    
    public static Value setValue (Value mutable, Preposition p, Value mutator) {
        
        switch (p) {
        case BEFORE:    return new Value(mutator.toString() + mutable.toString());
        case INTO:        return new Value(mutator.toString());
        case AFTER:        return new Value(mutable.toString() + mutator.toString());
        default: throw new RuntimeException("Value.setValue()| Unhandeled preposition");
        }
    }
    
    public boolean isEmpty () {
        return value.equals("");
    }

    public Value lessThan (Object val) {
        Value v = new Value(val);
        if (isFloat() && v.isFloat())
            return new Value(doubleValue() < v.doubleValue());
        else
            return new Value(toString().compareTo(v.toString()) < 0);
    }

    public Value greaterThan (Object val) {
        Value v = new Value(val);
        if (isFloat() && v.isFloat())
            return new Value(doubleValue() > v.doubleValue());
        else
            return new Value(toString().compareTo(v.toString()) > 0);
    }
    
    public Value greaterThanOrEqualTo (Object val) {
        Value v = new Value(val);
        if (isFloat() && v.isFloat())
            return new Value (doubleValue() >= v.doubleValue());
        else
            return new Value(toString().compareTo(v.toString()) >= 0);
    }
    
    public Value lessThanOrEqualTo (Object val) {
        Value v = new Value(val);
        if (isFloat() && v.isFloat())
            return new Value(doubleValue() <= v.doubleValue());
        else
            return new Value(toString().compareTo(v.toString()) <= 0);
    }
    
    public Value multiply (Object val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isNumber())
            throw new HtSemanticException(value + " cannot be multiplied because it is not a number");
        if (!v.isNumber())
            throw new HtSemanticException(value + " cannot be multiplied by the text expression: " + v);

        try {
            if (isInteger() && v.isInteger())
                return new Value(Math.multiplyExact(longValue(), v.longValue()));
            else
                return new Value(doubleValue() * v.doubleValue());
        } catch (ArithmeticException e) {
            throw new HtSemanticException("Overflow when trying to multiply " + stringValue() + " by " + v.stringValue());
        }
    }
    
    public Value divide (Object val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isNumber())
            throw new HtSemanticException(value + " cannot be divided because it is not a number");
        if (!v.isNumber())
            throw new HtSemanticException(value + " cannot be divided by the text expression: " + v);

        try {
            if (isInteger() && v.isInteger())
                return new Value(longValue() / v.longValue());
            else
                return new Value(doubleValue() / v.doubleValue());
        } catch (ArithmeticException e) {
            throw new HtSemanticException("Cannot divide " + stringValue() + " by zero");
        }
    }

    public Value add (Object val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isNumber())
            throw new HtSemanticException(value + " cannot be added because it is not a number");
        if (!v.isNumber())
            throw new HtSemanticException(value + " cannot be added to the text expression: " + v);

        try {
            if (isInteger() && v.isInteger())
                return new Value(Math.addExact(longValue(), v.longValue()));
            else
                return new Value(doubleValue() + v.doubleValue());
        } catch (ArithmeticException e) {
            throw new HtSemanticException("Overflow when trying to add " + stringValue() + " to " + v.stringValue());
        }
    }
    
    public Value subtract (Object val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isNumber())
            throw new HtSemanticException("'" + value + "' cannot be subtracted because it is not a number");
        if (!v.isNumber())
            throw new HtSemanticException("'" + value + "' cannot be subtracted by the text expression: " + v);

        try {
            if (isInteger() && v.isInteger())
                return new Value(Math.subtractExact(longValue(), v.longValue()));
            else
                return new Value(doubleValue() - v.doubleValue());
        } catch (ArithmeticException e) {
            throw new HtSemanticException("Overflow when trying to subtract " + v.stringValue() + " from " + stringValue());
        }
    }
    
    public Value exponentiate (Object val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isNumber())
            throw new HtSemanticException(value + " cannot be raised to a power because it is not a number");
        if (!v.isNumber())
            throw new HtSemanticException(value + " cannot be raised to the power of the text expression: " + v);

        return new Value(Math.pow(doubleValue(), v.doubleValue()));
    }

    public Value mod (Object val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isNumber())
            throw new HtSemanticException(value + " cannot be divided because it is not a number");
        if (!v.isNumber())
            throw new HtSemanticException(value + " cannot be divided by the text expression: " + v);
        
        if (isInteger() && v.isInteger())
            return new Value(longValue() % v.longValue());
        else
            return new Value(doubleValue() % v.doubleValue());
    }
    
    public Value not () throws HtSemanticException {
        if (!isBoolean())
            throw new HtSemanticException(value + " cannot be logically negated because it is not boolean");
        
        return new Value(!booleanValue());
    }

    public Value negate () throws HtSemanticException {
        if (isInteger())
            return new Value(longValue() * -1);
        else if (isFloat())
            return new Value(doubleValue() * -1);
        else {
            throw new HtSemanticException(value + " cannot be negated because it is not a number");
        }
    }

    public Value and (Value val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isBoolean())
            throw new HtSemanticException(value + " cannot be and'ed because it is not boolean");
        if (!v.isBoolean())
            throw new HtSemanticException(value + " cannot be and'ed with text value " + v);
        
        return new Value(booleanValue() && v.booleanValue());
    }
    
    public Value or (Value val) throws HtSemanticException {
        Value v = new Value(val);
        if (!isBoolean())
            throw new HtSemanticException(value + " cannot be or'ed because it is not boolean");
        if (!v.isBoolean())
            throw new HtSemanticException(value + " cannot be or'ed with text value " + v);
        
        return new Value(booleanValue() || v.booleanValue());
    }

    public Value concat (Value val) {
        return new Value(value + val.toString());
    }
    
    public boolean contains (Object v) {
        return value.contains(v.toString());
    }
        
    public String toString () {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value otherValue = (Value) o;

        if (isBoolean() && otherValue.isBoolean()) {
            return this.booleanValue() == otherValue.booleanValue();
        } else if (isInteger() && otherValue.isInteger()) {
            return this.integerValue() == otherValue.integerValue();
        } else if (isInteger() || isFloat()) {
            return this.doubleValue() == otherValue.doubleValue();
        } else {
            return this.stringValue().equalsIgnoreCase(otherValue.stringValue());
        }

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
