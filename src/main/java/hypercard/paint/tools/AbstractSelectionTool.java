package hypercard.paint.tools;

import hypercard.paint.Transform;
import hypercard.paint.canvas.Canvas;
import hypercard.paint.model.ImmutableProvider;
import hypercard.paint.model.PaintToolType;
import hypercard.paint.model.Provider;
import hypercard.paint.utils.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSelectionTool extends AbstractPaintTool implements KeyListener {

    private Provider<BufferedImage> selectedImage = new Provider<>();
    private Point initialPoint, lastPoint;
    private boolean isMovingSelection = false;
    private boolean dirty = false;

    private int antsPhase;
    private ScheduledExecutorService antsAnimator = Executors.newSingleThreadScheduledExecutor();
    private Future antsAnimation;

    public abstract void resetSelection();

    public abstract void setSelectionBounds(Rectangle bounds);

    public abstract void defineSelectionBounds(Point initialPoint, Point currentPoint, boolean constrain);

    public abstract void completeSelectionBounds(Point finalPoint);

    public abstract Shape getSelectionOutline();

    public abstract void adjustSelectionBounds(int xDelta, int yDelta);

    public AbstractSelectionTool(PaintToolType type) {
        super(type);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (hasSelectionBounds() && getSelectionOutline().contains(e.getPoint())) {
            setToolCursor(Cursor.getDefaultCursor());
        } else {
            setToolCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        isMovingSelection = getSelectionOutline() != null && getSelectionOutline().contains(e.getPoint());

        // User clicked inside selection bounds; start moving selection
        if (isMovingSelection) {
            lastPoint = e.getPoint();
        }

        // User clicked outside current selection bounds
        else {
            if (isDirty()) {
                finishSelection();
            } else {
                clearSelection();
            }

            initialPoint = e.getPoint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        // User is moving an existing selection
        if (isMovingSelection) {
            setDirty();
            adjustSelectionBounds(e.getX() - lastPoint.x, e.getY() - lastPoint.y);
            drawSelection();
            lastPoint = e.getPoint();
        }

        // User is defining a new selection rectangle
        else {
            defineSelectionBounds(initialPoint, MathUtils.pointWithinBounds(e.getPoint(), getCanvas().getBounds()), e.isShiftDown());

            getCanvas().clearScratch();
            drawSelectionOutline();
            getCanvas().repaintCanvas();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // User released mouse after defining a selection
        if (!hasSelection() && hasSelectionBounds()) {
            completeSelectionBounds(e.getPoint());
            getSelectionFromCanvas();
        }
    }

    @Override
    public void activate(Canvas canvas) {
        super.activate(canvas);

        getCanvas().addKeyListener(this);
        getCanvas().addObserver(this);

        antsAnimation = antsAnimator.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                antsPhase = antsPhase + 1 % 5;
                if (hasSelection()) {
                    drawSelection();
                }
            });

        }, 0, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        // Need to remove selection frame when tool is no longer active
        finishSelection();
        getCanvas().removeKeyListener(this);
        getCanvas().removeObserver(this);

        if (antsAnimation != null) {
            antsAnimation.cancel(false);
        }
    }

    public void rotateLeft() {
        transformSelection(Transform.rotateLeft(selectedImage.get().getWidth(), selectedImage.get().getHeight()));
    }

    public void rotateRight() {
        transformSelection(Transform.rotateRight(selectedImage.get().getWidth(), selectedImage.get().getHeight()));
    }

    public void flipHorizontal() {
        transformSelection(Transform.flipHorizontalTransform(selectedImage.get().getWidth()));
    }

    public void flipVerical() {
        transformSelection(Transform.flipVerticalTransform(selectedImage.get().getHeight()));
    }

    public void transformSelection(AffineTransform transform) {
        if (hasSelection()) {
            setDirty();

            // Get the original location of the selection
            Point originalLocation = getSelectionLocation();

            // Transform the selected image
            selectedImage.set(Transform.transform(selectedImage.get(), transform));

            // Relocate the image to its original location
            Rectangle newBounds = selectedImage.get().getRaster().getBounds();
            newBounds.setLocation(originalLocation);
            setSelectionBounds(newBounds);

            drawSelection();
        }
    }

    public ImmutableProvider<BufferedImage> getSelectedImageProvider() {
        return ImmutableProvider.from(selectedImage);
    }

    public BufferedImage getSelectedImage() {
        return selectedImage.get();
    }

    protected void setSelectedImage(BufferedImage selectedImage) {
        this.selectedImage.set(selectedImage);
        drawSelection();
    }

    /**
     * Gets the stroke used to paint the selection outline (typically, a dashed, "marching ants" stroke). May be
     * overridden by a subclass to change the tool of the selection.
     * @return
     */
    protected Stroke getMarchingAnts() {
        return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, new float[]{5.0f}, antsPhase);
    }

    /**
     * Clears the current selection frame (removes any "marching ants" from the canvas), but does not "erase" the image
     * selected.
     */
    public void clearSelection() {
        selectedImage.set(null);
        dirty = false;
        resetSelection();

        getCanvas().clearScratch();
        getCanvas().repaintCanvas();
    }

    /**
     * Determines if the user has an active selection.
     * <p>
     * Differs from {@link #hasSelectionBounds()} in that when a user is dragging the selection rectangle, a selection
     * boundary will exist but a selection will not. The selection is not made until the user releases the mouse.
     *
     * @return True is a selection exists, false otherwise.
     */
    public boolean hasSelection() {
        return hasSelectionBounds() && selectedImage.get() != null;
    }

    /**
     * Determines if the user has an active selection boundary (i.e., a rectangle of marching ants)
     * <p>
     * Differs from {@link #hasSelectionBounds()} in that when a user is dragging the selection rectangle, a selection
     * boundary will exist but a selection will not. The selection is not made until the user releases the mouse.
     *
     * @return True if a selection boundary exists, false otherwise.
     */
    public boolean hasSelectionBounds() {
        return getSelectionOutline() != null && getSelectionOutline().getBounds().width > 0 && getSelectionOutline().getBounds().height > 0;
    }

    /**
     * Make the canvas image bounded by the given selection rectangle the current selected image.
     */
    private void getSelectionFromCanvas() {
        getCanvas().clearScratch();

        Shape selectionBounds = getSelectionOutline();
        BufferedImage maskedSelection = maskSelection(getCanvas().getCanvasImage(), selectionBounds);
        BufferedImage trimmedSelection = maskedSelection.getSubimage(selectionBounds.getBounds().x, selectionBounds.getBounds().y, selectionBounds.getBounds().width, selectionBounds.getBounds().height);

        selectedImage.set(trimmedSelection);
        drawSelection();
    }

    /**
     * Determines the location (top-left x,y coordinate) of the selection outline.
     * @return
     */
    private Point getSelectionLocation() {
        if (!hasSelection()) {
            return null;
        }

        return getSelectionOutline().getBounds().getLocation();
    }

    /**
     * Removes the image bounded by the selection outline from the canvas by filling bounded pixels with
     * fully transparent pixels.
     */
    private void eraseSelectionFromCanvas() {
        getCanvas().clearScratch();

        // Clear image underneath selection
        Graphics2D scratch = (Graphics2D) getCanvas().getScratchGraphics();
        scratch.setColor(Color.WHITE);
        scratch.fill(getSelectionOutline());
        scratch.dispose();

        getCanvas().commit(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 1.0f));
        drawSelection();
    }

    /**
     * Drops the selected image onto the canvas (committing the change) and clears the selection outline. This has the
     * effect of completing a select-and-move operation.
     */
    protected void finishSelection() {

        if (hasSelection()) {
            getCanvas().clearScratch();

            Graphics2D g2d = (Graphics2D) getCanvas().getScratchGraphics();
            g2d.drawImage(selectedImage.get(), getSelectedImageLocation().x, getSelectedImageLocation().y, null);
            g2d.dispose();

            getCanvas().commit();
            clearSelection();
        }
    }

    /**
     * Draws the provided image and selection frame ("marching ants") onto the scratch buffer at the given location.
     */
    protected void drawSelection() {
        getCanvas().clearScratch();

        Graphics2D g = (Graphics2D) getCanvas().getScratchGraphics();
        g.drawImage(selectedImage.get(), getSelectedImageLocation().x, getSelectedImageLocation().y, null);
        g.dispose();

        drawSelectionOutline();

        getCanvas().repaintCanvas();
    }

    /**
     * Returns the location (top-left x,y coordinates) on the canvas where the selected image should be drawn.
     * Typically, this is the location of the selection shape.
     *
     * However, for tools that mutate the selection shape (i.e., {@link RotateTool}), this location may need to be
     * adjusted to account for changes to the selection shape's bounds.
     *
     * @return The x,y coordinate where the selected image should be drawn on the canvas.
     */
    protected Point getSelectedImageLocation() {
        return new Point(getSelectionOutline().getBounds().x, getSelectionOutline().getBounds().y);
    }

    /**
     * Renders the selection outline (marching ants) on the canvas.
     */
    protected void drawSelectionOutline() {
        Graphics2D g = (Graphics2D) getCanvas().getScratchGraphics();

        g.setStroke(getMarchingAnts());
        g.setColor(Color.BLACK);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.XOR));
        g.draw(getSelectionOutline());
        g.dispose();
    }

    /**
     * Marks the selection as having been mutated (either by transformation or movement).
     */
    protected void setDirty() {

        // First time we attempt to modify the selection, clear it from the canvas (so that we don't duplicate it)
        if (!dirty) {
            eraseSelectionFromCanvas();
        }

        dirty = true;
    }

    /**
     * Determines if the current selection has been changed or moved in any way since the selection outline was
     * defined.
     *
     * @return True if the selection was changed, false otherrwise.
     */
    protected boolean isDirty() {
        return dirty;
    }

    /**
     * Creates a new image in which every pixel not within the given shape has been changed to fully transparent.
     *
     * @param image The image to mask
     * @param shape The shape bounding the subimage to keep
     * @return
     */
    private BufferedImage maskSelection(BufferedImage image, Shape shape) {
        BufferedImage subimage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int clearPixel = new Color(0, 0, 0, 0).getRGB();

        for (int y = 0; y < image.getRaster().getHeight(); y++) {
            for (int x = 0; x < image.getRaster().getWidth(); x++) {
                if (x > image.getWidth() || y > image.getHeight()) continue;

                if (shape.contains(x, y)) {
                    subimage.setRGB(x, y, image.getRGB(x, y));
                } else {
                    subimage.setRGB(x, y, clearPixel);
                }
            }
        }

        return subimage;
    }

    @Override
    public void onCommit(Canvas canvas, BufferedImage committedElement, BufferedImage canvasImage) {
        if (hasSelection() && committedElement == null) {
            clearSelection();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (hasSelection()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_BACK_SPACE:
                    setDirty();
                    clearSelection();
                    break;

                case KeyEvent.VK_ESCAPE:
                    finishSelection();
                    break;

                case KeyEvent.VK_LEFT:
                    setDirty();
                    adjustSelectionBounds(-1, 0);
                    drawSelection();
                    break;

                case KeyEvent.VK_RIGHT:
                    setDirty();
                    adjustSelectionBounds(1, 0);
                    drawSelection();
                    break;

                case KeyEvent.VK_UP:
                    setDirty();
                    adjustSelectionBounds(0, -1);
                    drawSelection();
                    break;

                case KeyEvent.VK_DOWN:
                    setDirty();
                    adjustSelectionBounds(0, 1);
                    drawSelection();
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Nothing to do
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Nothing to do
    }
}
