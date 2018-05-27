package com.defano.wyldcard.parts.card;

import com.defano.wyldcard.parts.util.MouseEventDispatcher;
import com.defano.jmonet.canvas.JMonetCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * An extension of JLayeredPane that provides routines for addressing card layers. See {@link CardLayer}.
 */
public abstract class CardLayeredPane extends JLayeredPane {

    private JMonetScrollPane foregroundCanvas;
    private JMonetScrollPane backgroundCanvas;
    private MouseEventDispatcher mouseEventDispatcher;

    public CardLayeredPane() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                foregroundCanvas.setBounds(0, 0, getWidth(), getHeight());
                backgroundCanvas.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    /**
     * Returns the layer of the card on which the given component exists.
     *
     * @param component The component whose layer should be determined.
     * @return The card layer of the given component.
     * @throws IllegalArgumentException if the component does not exist on this pane
     */
    public CardLayer getCardLayer(Component component) {
        int layer = getLayer(component);
        if (layer == DEFAULT_LAYER) {
            throw new IllegalArgumentException("Component does not exist on this card.");
        }

        return CardLayer.fromPaneLayer(layer);
    }

    /**
     * Adds a component to the card pane on the specified card layer.
     *
     * @param component The component to be added.
     * @param layer The layer on which the component should be added.
     */
    public void addToCardLayer(Component component, CardLayer layer) {
        if (layer == CardLayer.CARD_GRAPHICS || layer == CardLayer.BACKGROUND_GRAPHICS) {
            throw new IllegalArgumentException("Cannot add components to the graphic layer: " + layer);
        }

        setLayer(component, layer.paneLayer);
        add(component);
    }

    protected void setBackgroundCanvas(JMonetCanvas canvas) {
        if (backgroundCanvas != null) {
            remove(backgroundCanvas);
        }

        this.backgroundCanvas = new JMonetScrollPane(canvas);
        setLayer(backgroundCanvas, CardLayer.BACKGROUND_GRAPHICS.paneLayer);
        add(backgroundCanvas);
    }

    protected void setForegroundCanvas(JMonetCanvas canvas) {
        if (foregroundCanvas != null) {
            remove(foregroundCanvas);
        }

        this.foregroundCanvas = new JMonetScrollPane(canvas);

        // Pass mouse events to parts obscured behind the canvas.
        mouseEventDispatcher = MouseEventDispatcher.bindTo(this.foregroundCanvas.getCanvas(), () -> getComponentsInCardLayer(CardLayer.BACKGROUND_PARTS));

        setLayer(foregroundCanvas, CardLayer.CARD_GRAPHICS.paneLayer);
        add(foregroundCanvas);
    }

    public void setBackgroundImageVisible(boolean visible) {
        if (visible) {
            setBackgroundCanvas(backgroundCanvas.getCanvas());
        } else {
            remove(backgroundCanvas);
        }

        this.invalidate();
        this.repaint();
    }

    public void setCardImageVisible(boolean visible) {
        if (visible) {
            setForegroundCanvas(foregroundCanvas.getCanvas());
        } else {
            remove(foregroundCanvas);
        }

        this.invalidate();
        this.repaint();
    }

    public JMonetCanvas getBackgroundCanvas() {
        return backgroundCanvas.getCanvas();
    }

    public JScrollPane getForegroundCanvasScrollPane() {
        return foregroundCanvas;
    }

    public JMonetCanvas getForegroundCanvas() {
        return foregroundCanvas.getCanvas();
    }

    private Component[] getComponentsInCardLayer(CardLayer layer) {
        return getComponentsInLayer(layer.paneLayer);
    }

    public void dispose() {
        removeAll();

        mouseEventDispatcher.unbind();
        foregroundCanvas = null;
        backgroundCanvas = null;
    }

    private class JMonetScrollPane extends JScrollPane {

        private final JMonetCanvas canvas;

        public JMonetScrollPane(JMonetCanvas canvas) {
            this.canvas = canvas;

            setViewportView(canvas);
            getViewport().setOpaque(false);
            setBorder(null);
            setOpaque(false);
        }

        public JMonetCanvas getCanvas() {
            return canvas;
        }
    }

}
