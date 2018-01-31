package com.defano.hypercard.parts.button.styles;

import com.defano.hypercard.border.RoundRectBorder;
import com.defano.hypercard.parts.ToolEditablePart;
import com.defano.hypercard.parts.button.ButtonComponent;

import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ClassicButton extends AbstractLabelButton implements ButtonComponent {

    private final static int ARC_DIAMETER = 6;      // Rounded corner diameter

    public ClassicButton(ToolEditablePart toolEditablePart) {
        super(toolEditablePart);
        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(getButtonBorder());
    }

    @Override
    protected void paintHilite(boolean isHilited, Graphics2D g) {

        g.setColor(getBackground());
        g.fill(new RoundRectangle2D.Double(
                getInsets().left,
                getInsets().top,
                getWidth() - getInsets().left - getInsets().right,
                getHeight() - getInsets().top - getInsets().bottom,
                ARC_DIAMETER,
                ARC_DIAMETER
        ));

        if (isHilited) {
            g.setColor(Color.BLACK);
            g.fill(new RoundRectangle2D.Double(
                    getInsets().left,
                    getInsets().top,
                    getWidth() - getInsets().left - getInsets().right,
                    getHeight() - getInsets().top - getInsets().bottom,
                    ARC_DIAMETER,
                    ARC_DIAMETER
            ));
        }
    }

    protected int getButtonCornerDiameter() {
        return ARC_DIAMETER;
    }

    protected Border getButtonBorder() {
        return new RoundRectBorder(ARC_DIAMETER);
    }
}
