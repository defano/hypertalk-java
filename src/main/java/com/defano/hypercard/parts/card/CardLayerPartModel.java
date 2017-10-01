package com.defano.hypercard.parts.card;

import com.defano.hypercard.fonts.FontUtils;
import com.defano.hypercard.fonts.FontFactory;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypertalk.ast.common.Owner;
import com.defano.hypertalk.ast.common.PartType;
import com.defano.hypertalk.ast.common.Value;

import javax.swing.*;
import java.awt.*;

public abstract class CardLayerPartModel extends PartModel {

    public static final String PROP_ZORDER = "zorder";
    public static final String PROP_SELECTEDTEXT = "selectedtext";
    public static final String PROP_SELECTEDLINE = "selectedline";
    public static final String PROP_SELECTEDCHUNK = "selectedchunk";
    public static final String PROP_TEXTSIZE = "textsize";
    public static final String PROP_TEXTFONT = "textfont";
    public static final String PROP_TEXTSTYLE = "textstyle";
    public static final String PROP_TEXTALIGN = "textalign";
    public static final String PROP_ENABLED = "enabled";

    public CardLayerPartModel(PartType type, Owner owner) {
        super(type, owner);

        defineProperty(PROP_ZORDER, new Value(0), false);
        defineProperty(PROP_SELECTEDTEXT, new Value(""), true);
        defineProperty(PROP_SELECTEDLINE, new Value(""), true);
        defineProperty(PROP_SELECTEDCHUNK, new Value(""), true);
        defineProperty(PROP_TEXTSIZE, new Value(((Font) UIManager.get("Button.font")).getSize()), false);
        defineProperty(PROP_TEXTFONT, new Value(((Font)UIManager.get("Button.font")).getFamily()), false);
        defineProperty(PROP_TEXTSTYLE, new Value("plain"), false);
        defineProperty(PROP_TEXTALIGN, new Value("center"), false);
        defineProperty(PROP_ENABLED, new Value(true), false);

    }

    public Font getFont() {
        String family = getKnownProperty(PROP_TEXTFONT).stringValue();
        int style = FontUtils.getStyleForValue(getKnownProperty(PROP_TEXTSTYLE));
        int size = getKnownProperty(PROP_TEXTSIZE).integerValue();

        return FontFactory.byNameStyleSize(family, style, size);
    }

    public void setFont(Font font) {
        if (font != null) {
            setKnownProperty(PROP_TEXTSIZE, new Value(font.getSize()));
            setKnownProperty(PROP_TEXTFONT, new Value(font.getFamily()));
            setKnownProperty(PROP_TEXTSTYLE, FontUtils.getValueForStyle(font.getStyle()));
        }
    }

}
