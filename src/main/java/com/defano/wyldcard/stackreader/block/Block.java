package com.defano.wyldcard.stackreader.block;

import com.defano.wyldcard.stackreader.HyperCardStack;
import com.defano.wyldcard.stackreader.decoder.MacRomanDecoder;
import com.defano.wyldcard.stackreader.decoder.PatternDecoder;
import com.defano.wyldcard.stackreader.decoder.VersionDecoder;
import com.defano.wyldcard.stackreader.decoder.WOBAImageDecoder;
import com.defano.wyldcard.stackreader.misc.ImportException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a "block" structure, an array of which comprise the HyperCard stack file format.
 */
@SuppressWarnings("unused")
public abstract class Block implements PatternDecoder, VersionDecoder, WOBAImageDecoder, MacRomanDecoder, Serializable {

    // Ignore when serializing; creates cycle in object graph
    private final transient HyperCardStack stack;

    private final BlockType blockType;
    private final int blockSize;
    private final int blockId;
    private final byte[] blockData;

    public Block(HyperCardStack stack, BlockType blockType, int blockSize, int blockId, byte[] blockData) {
        if (stack == null) {
            throw new IllegalArgumentException("Stack value cannot be null; each block must belong to a parent stack object.");
        }

        this.stack = stack;
        this.blockType = blockType;
        this.blockSize = blockSize;
        this.blockId = blockId;
        this.blockData = blockData;
    }

    /**
     * Unpacks (de-serializes) the data returned by {@link #getBlockData()} into block-specific Java fields. None
     * of the getter methods on the block (except for the block size, id, type and raw data) will return valid data
     * before this method has been called.
     *
     * @throws ImportException Thrown if a fatal error occurs unpacking the data, typically caused by malformed or
     *                         unexpected values in the data.
     */
    public abstract void unpack() throws ImportException, IOException;

    /**
     * Gets the HyperCard stack object to which this block belongs.
     *
     * @return The parent stack object.
     */
    public HyperCardStack getStack() {
        return stack;
    }

    /**
     * The enums of block. See {@link BlockType} for details.
     *
     * @return The block enums.
     */
    public BlockType getBlockType() {
        return blockType;
    }

    /**
     * Block size including size, enums and ID fields.
     *
     * @return The total block size, in bytes.
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * The ID of this block; a unique value used to identify this block from others of the same enums.
     *
     * @return The block id
     */
    public int getBlockId() {
        return blockId;
    }

    /**
     * Gets the data associated with the block, consisting of all of the bytes directly following the block header (the
     * block type, length and id). The size of the block data is equal to {@link #getBlockSize()} - 16 bytes (accounting
     * for the type, size and id fields).
     *
     * @return The block data
     */
    public byte[] getBlockData() {
        return blockData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
