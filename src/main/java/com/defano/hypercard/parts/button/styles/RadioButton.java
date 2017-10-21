package com.defano.hypercard.parts.button.styles;

import com.defano.hypercard.fonts.FontFactory;
import com.defano.hypercard.parts.button.ButtonPart;
import com.defano.hypercard.parts.ToolEditablePart;
import com.defano.hypercard.parts.button.ButtonComponent;
import com.defano.hypercard.parts.button.SharedHilight;
import com.defano.hypercard.parts.button.ButtonModel;
import com.defano.hypercard.fonts.FontUtils;
import com.defano.hypercard.parts.model.PropertiesModel;
import com.defano.hypertalk.ast.common.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RadioButton extends JRadioButton implements SharedHilight, ButtonComponent, ActionListener {

    private final ToolEditablePart toolEditablePart;

    public RadioButton(ToolEditablePart toolEditablePart) {
        this.toolEditablePart = toolEditablePart;

        super.addActionListener(this);
        super.addMouseListener(toolEditablePart);
        super.addKeyListener(toolEditablePart);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        toolEditablePart.drawSelectionRectangle(g);
    }

    @Override
    public void onPropertyChanged(PropertiesModel model, String property, Value oldValue, Value newValue) {
        switch (property) {
            case ButtonModel.PROP_NAME:
            case ButtonModel.PROP_SHOWNAME:
                boolean showName = toolEditablePart.getPartModel().getKnownProperty(ButtonModel.PROP_SHOWNAME).booleanValue();
                RadioButton.super.setText(showName ? toolEditablePart.getPartModel().getKnownProperty(ButtonModel.PROP_NAME).stringValue() : "");
                break;

            case ButtonModel.PROP_HILITE:
                RadioButton.super.setSelected(newValue.booleanValue());
                break;

            case ButtonModel.PROP_ENABLED:
                RadioButton.super.setEnabled(newValue.booleanValue());

            case ButtonModel.PROP_TEXTSIZE:
                setFont(FontFactory.byNameStyleSize(getFont().getFamily(), getFont().getStyle(), newValue.integerValue()));
                break;

            case ButtonModel.PROP_TEXTFONT:
                setFont(FontFactory.byNameStyleSize(newValue.stringValue(), getFont().getStyle(), getFont().getSize()));
                break;

            case ButtonModel.PROP_TEXTSTYLE:
                setFont(FontFactory.byNameStyleSize(getFont().getFamily(), FontUtils.getFontStyleForValue(newValue), getFont().getSize()));
                break;

            case ButtonModel.PROP_TEXTALIGN:
                setHorizontalAlignment(FontUtils.getAlignmentForValue(newValue));
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Swing automatically changes the selection state of the component for us; we need to un-do this change when
        // not in auto hilite mode.
        if (!isAutoHilited()) {
            setSelected(!isSelected());
        } else {
            setSharedHilite((ButtonPart) toolEditablePart, isSelected());
        }
    }

    private boolean isAutoHilited() {
        return toolEditablePart.getPartModel().getKnownProperty(ButtonModel.PROP_AUTOHILIGHT).booleanValue();
    }

}
