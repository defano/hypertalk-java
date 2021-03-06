package com.defano.wyldcard.stackreader.block;

import com.defano.wyldcard.stackreader.HyperCardStack;
import com.defano.wyldcard.stackreader.misc.ImportException;
import com.defano.wyldcard.stackreader.misc.StackInputStream;
import com.defano.wyldcard.stackreader.record.StyleRecord;

import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("unused")
public class StyleTableBlock extends Block {

    private int styleCount;
    private int nextStyleId;
    private StyleRecord[] styles;

    public StyleTableBlock(HyperCardStack stack, BlockType blockType, int blockSize, int blockId, byte[] blockData) {
        super(stack, blockType, blockSize, blockId, blockData);
    }

    public int getStyleCount() {
        return styleCount;
    }

    public int getNextStyleId() {
        return nextStyleId;
    }

    public StyleRecord[] getStyles() {
        return styles;
    }

    public StyleRecord getStyle(int styleId) {
        return Arrays.stream(getStyles())
                .filter(s -> s.getStyleId() == styleId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No style with id " + styleId));
    }

    @Override
    public void unpack() throws ImportException, IOException {

        try (StackInputStream sis = new StackInputStream(getBlockData())) {

            styleCount = sis.readInt();
            nextStyleId = sis.readInt();
            styles = new StyleRecord[styleCount];

            for (int styleIdx = 0; styleIdx < styleCount; styleIdx++) {
                byte[] styleRecord = sis.readBytes(24);
                styles[styleIdx] = StyleRecord.deserialize(this, styleRecord);
            }

        }
    }
}
