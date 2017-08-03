/*
 * RangeUtils
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:11 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypertalk.utils;

import com.defano.hypertalk.ast.common.*;
import com.defano.hypertalk.exception.HtSemanticException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangeUtils {

    /**
     * Gets the range of characters identified by the given CompositeChunk.
     *
     * @param value The value whose CompositeChunk should be ranged
     * @param c     The chunk specifier whose character range should be calculated
     * @return The range of characters in value identified by c.
     * @throws HtSemanticException If an expression in the composite chunk cannot be correctly evaluated.
     */
    public static Range getRange(String value, CompositeChunk c) throws HtSemanticException {
        return getRange(value, c, new Range(0, value.length()));
    }

    /**
     * Gets the range of characters identified by a set of chunks.
     *
     * @param value     The value whose chunk should be ranged.
     * @param chunkType The type of chunk; character, item, word, line or range thereof.
     * @param start     The first requested chunk, inclusive, counting from 1.
     * @param end       The last requested chunk, inclusive, counting from 1.
     * @return The range of characters identified.
     */
    public static Range getRange(String value, ChunkType chunkType, int start, int end) {
        Range startRange = getRange(value, chunkType, start);
        Range endRange = getRange(value, chunkType, end);
        return new Range(startRange.start, endRange.end);
    }

    /**
     * Gets the range of characters identified by the given chunk.
     *
     * @param value     The value whose chunk should be ranged.
     * @param chunkType The type of chunk; character, item, word, line or range thereof.
     * @param count     The first requested chunk, inclusive, counting from 1.
     * @return
     */
    public static Range getRange(String value, ChunkType chunkType, int count) {
        Pattern pattern = ChunkUtils.getRegexForChunkType(chunkType);
        Matcher matcher = pattern.matcher(value);

        if (count == Ordinal.LAST.intValue()) {
            advanceToMatch(matcher, ChunkUtils.getMatchCount(matcher) - 1);
        } else if (count == Ordinal.MIDDLE.intValue()) {
            advanceToMatch(matcher, ChunkUtils.getMatchCount(matcher) / 2);
        } else {
            advanceToMatch(matcher, count - 1);
        }

        try {
            return new Range(matcher.start(), matcher.end());
        } catch (IllegalStateException e) {
            return new Range(value.length(), value.length());
        }
    }


    private static Range getRange(String value, CompositeChunk c, Range in) throws HtSemanticException {
        Range s = getRange(value, (Chunk) c, in);

        value = new Value(value).getChunk(new Chunk(c.type, c.start, c.end)).stringValue();
        s = getRange(value, c.chunkOf, s);

        if (c.chunkOf instanceof CompositeChunk) {
            Chunk next = ((CompositeChunk) c.chunkOf).chunkOf;
            value = new Value(value).getChunk(new Chunk(c.chunkOf.type, c.chunkOf.start, c.chunkOf.end)).stringValue();

            if (next instanceof CompositeChunk) {
                return getRange(value, (CompositeChunk) next, s);
            } else {
                return getRange(value, next, s);
            }
        } else {
            return s;
        }
    }


    private static Range getRange(String value, Chunk c, Range in) throws HtSemanticException {
        Range range = getRange(value, c);
        return new Range(in.start + range.start, in.start + range.start + (range.end - range.start));
    }

    private static Range getRange(String value, Chunk c) throws HtSemanticException {
        Value startVal = null;
        Value endVal = null;

        if (c.start != null)
            startVal = c.start.evaluate();
        if (c.end != null)
            endVal = c.end.evaluate();

        if (!startVal.isNatural() && !startVal.equals(Ordinal.MIDDLE.value()))
            throw new HtSemanticException("Chunk specifier requires natural integer value, got '" + startVal + "' instead");
        if (endVal != null && !endVal.isNatural() && !endVal.equals(Ordinal.MIDDLE.value()))
            throw new HtSemanticException("Chunk specifier requires natural integer value, got '" + endVal + "' instead");

        if (endVal != null)
            return getRange(value, c.type, startVal.integerValue(), endVal.integerValue());
        else
            return getRange(value, c.type, startVal.integerValue());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void advanceToMatch(Matcher matcher, int index) {
        while (index-- >= 0) {
            matcher.find();
        }
    }

}
