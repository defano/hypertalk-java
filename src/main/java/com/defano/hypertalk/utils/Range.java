package com.defano.hypertalk.utils;

import com.defano.hypertalk.ast.common.Chunk;
import com.defano.hypertalk.ast.common.ChunkType;
import com.defano.hypertalk.ast.expressions.LiteralExp;

public class Range {
    public final int start, end;

    public Range() {
        this(0, 0);
    }

    public Range(int start, int end) {
        this.start = start < end ? start : end;
        this.end = start < end ? end : start;
    }

    public int length() {
        return end - start;
    }

    public Chunk asChunk() {
        return new Chunk(ChunkType.CHARRANGE, new LiteralExp(null, start + 1), new LiteralExp(null, end));
    }

    public boolean isEmpty() {
        return length() < 1;
    }

    @Override
    public String toString() {
        return "Range{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
