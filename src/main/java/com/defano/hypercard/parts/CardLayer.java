package com.defano.hypercard.parts;

import com.defano.hypertalk.ast.common.PartLayer;

public enum CardLayer {
    BACKGROUND_GRAPHICS(1, "Background"),
    BACKGROUND_PARTS(2, "Background"),
    CARD_GRAPHICS(3, "Card"),
    CARD_PARTS(4, "Card");

    public final int paneLayer;
    public final String friendlyName;

    CardLayer(int paneLayer, String friendlyName) {
        this.paneLayer = paneLayer;
        this.friendlyName = friendlyName;
    }

    public PartLayer asPartLayer() {
        return (this == BACKGROUND_GRAPHICS || this == BACKGROUND_PARTS) ? PartLayer.BACKGROUND : PartLayer.CARD;
    }

    public static CardLayer fromPaneLayer(int layer) {
        for (CardLayer thisLayer : CardLayer.values()) {
            if (thisLayer.paneLayer == layer) {
                return thisLayer;
            }
        }

        throw new IllegalArgumentException("No such layer: " + layer);
    }
}
