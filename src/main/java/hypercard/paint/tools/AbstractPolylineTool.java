package hypercard.paint.tools;

import hypercard.paint.model.PaintToolType;
import hypercard.paint.utils.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPolylineTool extends AbstractPaintTool implements KeyListener {

    private List<Point> points = new ArrayList<>();
    private Point currentPoint = null;

    protected abstract void drawPolyline(Graphics2D g, Stroke stroke, Paint paint, int[] xPoints, int[] yPoints);
    protected abstract void drawPolygon(Graphics2D g, Stroke stroke, Paint strokePaint, int[] xPoints, int[] yPoints);
    protected abstract void fillPolygon(Graphics2D g, Paint fillPaint, int[] xPoints, int[] yPoints);

    public AbstractPolylineTool(PaintToolType type) {
        super(type);
    }

    @Override
    public void activate(hypercard.paint.canvas.Canvas canvas) {
        super.activate(canvas);
        getCanvas().addKeyListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        getCanvas().removeKeyListener(this);
    }


    @Override
    public void mouseMoved(MouseEvent e) {

        // Nothing to do if initial point is not yet established
        if (points.size() == 0) {
            return;
        }

        if (e.isShiftDown()) {
            Point lastPoint = points.get(points.size() - 1);
            currentPoint = MathUtils.snapLineToNearestAngle(lastPoint, e.getPoint(), 15);
            points.add(currentPoint);
        } else {
            currentPoint = e.getPoint();
            points.add(currentPoint);
        }

        int[] xs = points.stream().mapToInt(i->i.x).toArray();
        int[] ys = points.stream().mapToInt(i->i.y).toArray();

        getCanvas().clearScratch();

        Graphics2D g2d = (Graphics2D) getCanvas().getScratchGraphics();
        drawPolyline(g2d, getStroke(), getStrokePaint(), xs, ys);
        g2d.dispose();

        getCanvas().repaintCanvas();

        points.remove(points.size() - 1);
    }

    @Override
    public void mousePressed(MouseEvent e) {

        // User double-clicked; complete the polygon
        if (e.getClickCount() > 1 && points.size() > 1){
            points.add(currentPoint);
            commitPolygon();
        }

        // First click (creating initial point)
        else if (currentPoint == null) {
            points.add(e.getPoint());
        }

        // Single click with initial point established
        else {
            points.add(currentPoint);
        }
    }

    private void commitPolygon() {
        getCanvas().clearScratch();

        int[] xs = points.stream().mapToInt(i->i.x).toArray();
        int[] ys = points.stream().mapToInt(i->i.y).toArray();

        points.clear();
        currentPoint = null;

        Graphics2D g2d = (Graphics2D) getCanvas().getScratchGraphics();
        drawPolygon(g2d, getStroke(), getStrokePaint(), xs, ys);

        if (getFillPaint() != null) {
            fillPolygon(g2d, getFillPaint(), xs, ys);
        }

        g2d.dispose();

        getCanvas().commit();
    }

    private void commitPolyline() {
        getCanvas().clearScratch();

        int[] xs = points.stream().mapToInt(i->i.x).toArray();
        int[] ys = points.stream().mapToInt(i->i.y).toArray();

        points.clear();
        currentPoint = null;

        Graphics2D g2d = (Graphics2D) getCanvas().getScratchGraphics();
        drawPolyline(g2d, getStroke(), getStrokePaint(), xs, ys);
        g2d.dispose();

        getCanvas().commit();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Nothing to do
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

            // Ignore esc unless at least one point has been defined
            if (points.size() > 0) {
                points.add(currentPoint);
                commitPolyline();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Nothing to do
    }
}
