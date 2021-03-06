package com.defano.wyldcard.part.card;

import com.defano.wyldcard.part.util.MouseEventDispatcher;
import com.defano.jmonet.canvas.JMonetCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * An extension of JLayeredPane that provides routines for addressing card layers. See {@link CardDisplayLayer}.
 */
public abstract class CardLayeredPane extends JLayeredPane {

    private JMonetScrollPane foregroundCanvas;
    private JMonetScrollPane backgroundCanvas;
    private MouseEventDispatcher mouseEventDispatcher;

    public CardLayeredPane() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (foregroundCanvas != null) {
                    foregroundCanvas.setBounds(0, 0, getWidth(), getHeight());
                }

                if (backgroundCanvas != null) {
                    backgroundCanvas.setBounds(0, 0, getWidth(), getHeight());
                }
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
    public CardDisplayLayer getCardLayer(Component component) {
        int layer = getLayer(component);
        if (layer == DEFAULT_LAYER) {
            throw new IllegalArgumentException("Component does not exist on this card.");
        }

        return CardDisplayLayer.fromPaneLayer(layer);
    }

    /**
     * Adds a component to the card pane on the specified card layer.
     *
     * @param component The component to be added.
     * @param layer The layer on which the component should be added.
     */
    public void addToCardLayer(Component component, CardDisplayLayer layer) {
        if (layer == CardDisplayLayer.CARD_GRAPHICS || layer == CardDisplayLayer.BACKGROUND_GRAPHICS) {
            throw new IllegalArgumentException("Cannot add components to the graphic layer: " + layer);
        }

        setLayer(component, layer.getPaneLayer());
        add(component);
        revalidate();
    }

    protected void setBackgroundCanvas(JMonetScrollPane canvas) {
        if (backgroundCanvas != null) {
            remove(backgroundCanvas);
        }

        this.backgroundCanvas = canvas;
        setLayer(backgroundCanvas, CardDisplayLayer.BACKGROUND_GRAPHICS.getPaneLayer());
        add(backgroundCanvas);
        revalidate();
    }

    protected void setForegroundCanvas(JMonetScrollPane canvas) {
        if (foregroundCanvas != null) {
            remove(foregroundCanvas);
        }

        if (mouseEventDispatcher != null) {
            mouseEventDispatcher.unbind();
        }

        this.foregroundCanvas = canvas;

        // Pass mouse events to parts obscured behind the canvas.
        mouseEventDispatcher = MouseEventDispatcher.boundTo(this.foregroundCanvas.getCanvas(), () -> getComponentsInCardLayer(CardDisplayLayer.BACKGROUND_PARTS));

        setLayer(foregroundCanvas, CardDisplayLayer.CARD_GRAPHICS.getPaneLayer());
        add(foregroundCanvas);
        revalidate();
        repaint();
    }

    public void setBackgroundImageVisible(boolean visible) {
        if (visible) {
            setBackgroundCanvas(backgroundCanvas);
        } else {
            remove(backgroundCanvas);
        }

        revalidate();
        repaint();
    }

    public void setCardImageVisible(boolean visible) {
        if (visible) {
            setForegroundCanvas(foregroundCanvas);
        } else {
            remove(foregroundCanvas);
        }

        revalidate();
        repaint();
    }

    public JMonetCanvas getBackgroundCanvas() {
        return backgroundCanvas == null ? null : backgroundCanvas.getCanvas();
    }

    public JScrollPane getForegroundCanvasScrollPane() {
        return foregroundCanvas;
    }

    public JMonetCanvas getForegroundCanvas() {
        return foregroundCanvas == null ? null : foregroundCanvas.getCanvas();
    }

    private Component[] getComponentsInCardLayer(CardDisplayLayer layer) {
        return getComponentsInLayer(layer.getPaneLayer());
    }

    public void dispose() {
        removeAll();

        mouseEventDispatcher.unbind();

        foregroundCanvas.getCanvas().dispose();
        backgroundCanvas.getCanvas().dispose();

        foregroundCanvas = null;
        backgroundCanvas = null;
    }

}
