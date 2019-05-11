package com.defano.hypertalk.ast.model.chunk;

import com.defano.hypertalk.ast.expression.Expression;

public class CompositeChunk extends Chunk {

    public final Chunk chunkOf;

    public CompositeChunk(ChunkType type, Expression start, Expression end, Chunk chunkOf) {
        super(type, start, end);
        this.chunkOf = chunkOf;
    }

    public ChunkType getMutatedChunkType () {
        return getMutatedChunkType(this);
    }

    public static ChunkType getMutatedChunkType (Chunk c) {
        if (c instanceof CompositeChunk) {
            return getMutatedChunkType(((CompositeChunk) c).chunkOf);
        } else {
            return c.type;
        }
    }
}
