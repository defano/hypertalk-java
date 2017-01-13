package hypercard.paint.tools;

import hypercard.paint.model.PaintToolType;
import hypercard.paint.utils.FlexQuadrilateral;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public abstract class AbstractTransformTool extends AbstractSelectionTool {

    private final static int HANDLE_SIZE = 8;

    private BufferedImage originalImage;
    private Rectangle selectionBounds;
    private FlexQuadrilateral transformBounds;

    private Rectangle topLeftHandle, topRightHandle, bottomRightHandle, bottomLeftHandle;
    private boolean dragTopLeft, dragTopRight, dragBottomRight, dragBottomLeft;

    public abstract void moveTopLeft(FlexQuadrilateral quadrilateral, Point newPosition);

    public abstract void moveTopRight(FlexQuadrilateral quadrilateral, Point newPosition);

    public abstract void moveBottomLeft(FlexQuadrilateral quadrilateral, Point newPosition);

    public abstract void moveBottomRight(FlexQuadrilateral quadrilateral, Point newPosition);

    public AbstractTransformTool(PaintToolType type) {
        super(type);
    }

    @Override
    public void mousePressed(MouseEvent e, int scaleX, int scaleY) {

        // User has already made selection; we'll handle the mouse press
        if (hasSelection()) {

            // User is clicking a drag handle
            dragTopLeft = topLeftHandle.contains(new Point(scaleX, scaleY));
            dragTopRight = topRightHandle.contains(new Point(scaleX, scaleY));
            dragBottomLeft = bottomLeftHandle.contains(new Point(scaleX, scaleY));
            dragBottomRight = bottomRightHandle.contains(new Point(scaleX, scaleY));

            // User is clicking outside the selection bounds; clear selection
            if (!getSelectionOutline().contains(new Point(scaleX, scaleY))) {
                finishSelection();
                clearSelection();
            }
        }

        // No selection; delegate to selection tool to create a selection
        else {
            super.mousePressed(e, scaleX, scaleY);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e, int scaleX, int scaleY) {

        // Selection exists, see if we're dragging a handle
        if (hasSelection()) {

            if (dragTopLeft || dragTopRight || dragBottomLeft || dragBottomRight) {
                setDirty();
            }

            if (dragTopLeft) {
                moveTopLeft(transformBounds, new Point(scaleX, scaleY));
            } else if (dragTopRight) {
                moveTopRight(transformBounds, new Point(scaleX, scaleY));
            } else if (dragBottomLeft) {
                moveBottomLeft(transformBounds, new Point(scaleX, scaleY));
            } else if (dragBottomRight) {
                moveBottomRight(transformBounds, new Point(scaleX, scaleY));
            }

            drawSelection();
        }

        // No selection, delegate to selection tool to define selection
        else {
            super.mouseDragged(e, scaleX, scaleY);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e, int scaleX, int scaleY) {

        // User is completing selection
        if (!hasSelection()) {
            super.mouseReleased(e, scaleX, scaleY);

            // Grab a copy of the selected image before we begin transforming it
            originalImage = getSelectedImage();
        }
    }

    @Override
    public void defineSelectionBounds(Point initialPoint, Point currentPoint, boolean constrain) {
        selectionBounds = new Rectangle(initialPoint);
        selectionBounds.add(currentPoint);

        int width = selectionBounds.width;
        int height = selectionBounds.height;

        if (constrain) {
            width = height = Math.max(width, height);
        }

        selectionBounds = new Rectangle(selectionBounds.x, selectionBounds.y, width, height);
    }

    @Override
    public void completeSelectionBounds(Point finalPoint) {
        transformBounds = new FlexQuadrilateral(selectionBounds);
    }

    @Override
    public void resetSelection() {
        selectionBounds = null;
        transformBounds = null;

        topLeftHandle = topRightHandle = bottomLeftHandle = bottomRightHandle = null;
    }

    @Override
    public void setSelectionBounds(Rectangle bounds) {
        throw new IllegalStateException("No implemented");
    }

    @Override
    public Shape getSelectionOutline() {
        return transformBounds != null ? transformBounds.getShape() : selectionBounds;
    }

    @Override
    public void adjustSelectionBounds(int xDelta, int yDelta) {
        selectionBounds.setLocation(selectionBounds.x + xDelta, selectionBounds.y + yDelta);
    }

    protected BufferedImage getOriginalImage() {
        return originalImage;
    }

    protected void drawSelectionOutline() {
        super.drawSelectionOutline();

        if (hasSelection()) {

            // Render drag handles on selection bounds
            Graphics2D g = (Graphics2D) getCanvas().getScratchGraphics();
            g.setPaint(Color.BLACK);

            topLeftHandle = new Rectangle(transformBounds.getTopLeft().x, transformBounds.getTopLeft().y, HANDLE_SIZE, HANDLE_SIZE);
            topRightHandle = new Rectangle(transformBounds.getTopRight().x - HANDLE_SIZE, transformBounds.getTopRight().y, HANDLE_SIZE, HANDLE_SIZE);
            bottomRightHandle = new Rectangle(transformBounds.getBottomRight().x - HANDLE_SIZE, transformBounds.getBottomRight().y - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
            bottomLeftHandle = new Rectangle(transformBounds.getBottomLeft().x, transformBounds.getBottomLeft().y - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);

            g.fill(topLeftHandle);
            g.fill(topRightHandle);
            g.fill(bottomRightHandle);
            g.fill(bottomLeftHandle);

            g.dispose();
        }
    }

}
