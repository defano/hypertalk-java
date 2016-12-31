package hypercard.paint.tools;

import java.awt.*;

public class RoundRectangleTool extends AbstractShapeTool {

    public RoundRectangleTool() {
        super(PaintToolType.ROUND_RECTANGLE);
    }

    @Override
    public void drawBounds(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        if (getFill() != null) {
            g2d.setPaint(getFill());
            g2d.fillRoundRect(x, y, width, height, 10, 10);
        }
    }
}
