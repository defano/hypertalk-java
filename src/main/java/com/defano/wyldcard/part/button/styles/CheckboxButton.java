package com.defano.wyldcard.part.button.styles;

import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.border.PartBorderFactory;
import com.defano.wyldcard.font.FontUtils;
import com.defano.wyldcard.part.ToolEditablePart;
import com.defano.wyldcard.part.button.ButtonModel;
import com.defano.wyldcard.part.button.ButtonPart;
import com.defano.wyldcard.part.button.HyperCardButton;
import com.defano.wyldcard.part.button.SharedHilite;
import com.defano.wyldcard.property.PropertiesModel;
import com.defano.wyldcard.runtime.ExecutionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CheckboxButton extends JCheckBox implements SharedHilite, HyperCardButton, ActionListener {

    private final ToolEditablePart toolEditablePart;

    public CheckboxButton(ToolEditablePart toolEditablePart) {
        this.toolEditablePart = toolEditablePart;

        super.setBorder(PartBorderFactory.createEmptyBorder());
        super.setBorderPainted(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        toolEditablePart.drawSelectionRectangle(g);
    }

    @Override
    public ToolEditablePart getToolEditablePart() {
        return toolEditablePart;
    }

    @Override
    public void onStart() {
        super.addActionListener(this);
        super.addMouseListener(toolEditablePart);
        super.addKeyListener(toolEditablePart);
    }

    @Override
    public void onStop() {
        super.removeActionListener(this);
        super.removeMouseListener(toolEditablePart);
        super.addKeyListener(toolEditablePart);
    }

    @Override
    public void onPropertyChanged(ExecutionContext context, PropertiesModel model, String property, Value oldValue, Value newValue) {
        switch (property) {
            case ButtonModel.PROP_NAME:
            case ButtonModel.PROP_SHOWNAME:
                boolean showName = toolEditablePart.getPartModel().get(context, ButtonModel.PROP_SHOWNAME).booleanValue();
                String buttonName = toolEditablePart.getPartModel().get(context, ButtonModel.PROP_NAME).toString();
                setText(showName ? buttonName : "");
                break;

            case ButtonModel.PROP_HIGHLIGHT:
                CheckboxButton.super.setSelected(newValue.booleanValue());
                break;

            case ButtonModel.PROP_ENABLED:
                CheckboxButton.super.setEnabled(newValue.booleanValue());
                break;

            case ButtonModel.PROP_TEXTSIZE:
                setFont(FontUtils.getFontByNameStyleSize(getFont().getFamily(), getFont().getStyle(), newValue.integerValue()));
                break;

            case ButtonModel.PROP_TEXTFONT:
                setFont(FontUtils.getFontByNameStyleSize(newValue.toString(), getFont().getStyle(), getFont().getSize()));
                break;

            case ButtonModel.PROP_TEXTSTYLE:
                setFont(FontUtils.getFontByNameStyleSize(getFont().getFamily(), FontUtils.getFontStyleForValue(context, newValue), getFont().getSize()));
                break;

            case ButtonModel.PROP_TEXTALIGN:
                //noinspection MagicConstant
                setHorizontalAlignment(FontUtils.getAlignmentForValue(newValue));
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Swing automatically changes the selection state of the component for us; we need to un-do this change when not in auto-hilite mode.
        if (isAutoHilited()) {
            setSharedHilite(new ExecutionContext(), (ButtonPart) toolEditablePart, isSelected());
        } else {
            setSelected(!isSelected());
        }
    }

    private boolean isAutoHilited() {
        return toolEditablePart.getPartModel().get(new ExecutionContext(), ButtonModel.ALIAS_AUTOHILIGHT).booleanValue();
    }

}
