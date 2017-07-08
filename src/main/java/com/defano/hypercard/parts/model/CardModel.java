/*
 * CardModel
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.parts.model;

import com.defano.hypercard.Serializer;
import com.defano.hypercard.parts.Part;
import com.defano.hypercard.parts.PartException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CardModel {

    private int backgroundId = 0;
    private Collection<ButtonModel> buttonModels;
    private Collection<FieldModel> fieldModels;
    private byte[] cardImage;

    private CardModel (int backgroundId) {
        this.buttonModels = new ArrayList<>();
        this.fieldModels = new ArrayList<>();
        this.backgroundId = backgroundId;
    }

    public static CardModel emptyCardModel (int backgroundId) {
        return new CardModel(backgroundId);
    }

    public Collection<AbstractPartModel> getPartModels() {
        List<AbstractPartModel> partModels = new ArrayList<>();
        partModels.addAll(buttonModels);
        partModels.addAll(fieldModels);
        return partModels;
    }

    public void removePart (Part part) {
        switch (part.getType()) {
            case BUTTON:
                buttonModels.remove(part.getPartModel());
                break;
            case FIELD:
                fieldModels.remove(part.getPartModel());
                break;
        }
    }

    public void addPart (Part part) throws PartException {
        switch (part.getType()) {
            case BUTTON:
                buttonModels.add((ButtonModel) part.getPartModel());
                break;
            case FIELD:
                fieldModels.add((FieldModel) part.getPartModel());
                break;
            default:
                throw new PartException("Bug! Unsupported part type: " + part.getType());
        }
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    public void setCardImage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            this.cardImage = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to save the card image.", e);
        }
    }

    public BufferedImage getCardImage() {
        if (cardImage == null || cardImage.length == 0) {
            return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        } else {
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(cardImage);
                return ImageIO.read(stream);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading the card image. The stack may be corrupted.", e);
            }
        }
    }

    public CardModel copyOf() {
        return Serializer.copy(this);
    }
}
