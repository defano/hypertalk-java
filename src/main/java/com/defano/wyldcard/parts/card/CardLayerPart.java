package com.defano.wyldcard.parts.card;

import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.context.ToolsContext;
import com.defano.wyldcard.parts.Part;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.hypertalk.ast.model.Value;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Represents a part that exists as part of a layer of the card (that is, a button or a field).
 */
public interface CardLayerPart extends Part {

    /**
     * Gets the Swing component associated with this part.
     * @return The Java Swing component.
     */
    JComponent getComponent();

    /**
     * Gets the card on which this part exists.
     * @return The part's parent card.
     */
    CardPart getCard();

    /**
     * Determines the layer of the card on which this part exists.
     * @return The layer of the card the part is on or null if indeterminate
     */
    default CardLayer getCardLayer() {
        CardPart card = getCard();
        return card == null ? null : card.getCardLayer(getComponent());
    }

    /**
     * Determines the currently active part layer, either {@link CardLayer#BACKGROUND_PARTS} or
     * {@link CardLayer#CARD_PARTS} depending on whether the user is presently editing the background.
     *
     * @return The part layer currently being edited.
     */
    static CardLayer getActivePartLayer() {
        return ToolsContext.getInstance().isEditingBackground() ? CardLayer.BACKGROUND_PARTS : CardLayer.CARD_PARTS;
    }

    /**
     * Sets the z-position of this part relative to other parts on the card.
     * @param context The execution context.
     * @param newPosition The z-order position of this part
     */
    default void setDisplayOrder(ExecutionContext context, int newPosition) {
        CardPart card = getCard();
        ArrayList<PartModel> parts = new ArrayList<>(card.getCardModel().getPartsInDisplayOrder(context));

        if (newPosition < 0) {
            newPosition = 0;
        } else if (newPosition > parts.size() - 1) {
            newPosition = parts.size() - 1;
        }

        parts.remove(getPartModel());
        parts.add(newPosition, getPartModel());

        for (int index = 0; index < parts.size(); index++) {
            PartModel thisPart = parts.get(index);
            thisPart.setKnownProperty(context, CardLayerPartModel.PROP_ZORDER, new Value(index), true);
        }

        card.onDisplayOrderChanged(context);
    }
}
