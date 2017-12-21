package com.defano.hypercard.parts.button.styles;

import com.defano.hypercard.border.RoundRectBorder;
import com.defano.hypercard.parts.ToolEditablePart;
import com.defano.hypercard.parts.button.ButtonComponent;

import java.awt.*;

public class ClassicButton extends AbstractLabelButton implements ButtonComponent {

    private final static int ARC_DIAMETER = 6;      // Rounded corner diameter

    public ClassicButton(ToolEditablePart toolEditablePart) {
        super(toolEditablePart);
        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(new RoundRectBorder(ARC_DIAMETER));
    }

    @Override
    protected void paintHilite(boolean isHilited, Graphics2D g) {
        if (isHilited) {
            g.setColor(Color.BLACK);
            g.fillRoundRect(
                    getInsets().left - 1,
                    getInsets().top - 1,
                    getWidth() - getInsets().left - getInsets().right + 2,
                    getHeight() - getInsets().top - getInsets().bottom + 2,
                    ARC_DIAMETER,
                    ARC_DIAMETER
            );
        } else {
            g.setColor(getBackground());
            g.fillRoundRect(
                    getInsets().left,
                    getInsets().top,
                    getWidth() - getInsets().left - getInsets().right,
                    getHeight() - getInsets().top - getInsets().bottom,
                    ARC_DIAMETER,
                    ARC_DIAMETER
            );
        }
    }

}
