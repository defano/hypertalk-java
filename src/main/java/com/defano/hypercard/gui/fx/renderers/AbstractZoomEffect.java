package com.defano.hypercard.gui.fx.renderers;

import com.defano.hypercard.gui.fx.AnimatedVisualEffect;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractZoomEffect extends AnimatedVisualEffect {

    public enum ZoomShape {
        CIRCLE, RECTANGLE
    }

    public enum ZoomDirection {
        ZOOM_IN, ZOOM_OUT
    }

    private final ZoomDirection direction;
    private final ZoomShape shape;

    protected AbstractZoomEffect(ZoomShape shape, ZoomDirection direction) {
        this.direction = direction;
        this.shape = shape;
    }

    @Override
    public BufferedImage render(BufferedImage from, BufferedImage to, float progress) {
        int diagonal = (int) (Math.sqrt(Math.pow(from.getHeight(), 2) + Math.pow(from.getWidth(), 2)));

        if (direction == ZoomDirection.ZOOM_IN) {
            return renderZoom(to, from, (int)((diagonal / 2) * (1.0f - progress)));
        } else {
            return renderZoom(from, to, (int)((diagonal / 2) * progress));
        }
    }

    private BufferedImage renderZoom(BufferedImage a, BufferedImage b, int radius) {
        BufferedImage frame = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frame.createGraphics();

        // Cut the Iris out of the from image (leave a transparent hole) and draw it onto the frame
        g.drawImage(a, 0, 0, null);
        maskZoomRegion(g, a.getWidth(), a.getHeight(), radius, AlphaComposite.getInstance(AlphaComposite.DST_OUT));

        // Make a copy of the to image so we can cut an iris out of it
        BufferedImage iris = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = iris.createGraphics();
        ig.drawImage(b, 0, 0, null);

        // Cut out the iris from the to image (leaving an opaque hole on a transparent background)
        maskZoomRegion(ig, b.getWidth(), b.getHeight(), radius, AlphaComposite.getInstance(AlphaComposite.DST_IN));
        ig.dispose();

        // Draw the cut-out iris onto the frame
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.drawImage(iris, 0, 0, null);

        return frame;
    }


    /**
     * Punches out the ZoomShape of a specified radius in the given graphics context.
     *
     * @param g The graphics context to modify
     * @param width The width of the frame
     * @param height The height of the frame
     * @param radius The radius of the iris to create
     * @param mode The Porter-Duff mode to apply
     */
    protected void maskZoomRegion(Graphics g, int width, int height, int radius, AlphaComposite mode) {

        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics mg = mask.createGraphics();
        drawZoomShape(mg, width, height, radius);
        mg.dispose();

        ((Graphics2D)g).setComposite(mode);
        g.drawImage(mask, 0, 0, null);
    }

    private void drawZoomShape(Graphics g, int w, int h, int radius) {
        switch (shape) {
            case CIRCLE:
                g.fillOval((w / 2) - radius, (h / 2) - radius, radius * 2, radius * 2);
                break;
            case RECTANGLE:
                g.fillRect((w / 2) - radius, (h / 2) - radius, radius * 2, radius * 2);
                break;
        }
    }

}
