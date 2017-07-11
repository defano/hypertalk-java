/*
 * PartResizer
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.parts;

import com.defano.hypercard.gui.util.MouseManager;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypercard.gui.util.KeyboardManager;
import com.defano.hypertalk.ast.common.Value;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides the ability for the user to resize a part within the card panel of the stack window.
 */
public class PartResizer {

    public final static int QUADRANT_TOPRIGHT = 1;
    public final static int QUADRANT_TOPLEFT = 2;
    public final static int QUADRANT_BOTTOMLEFT = 3;
    public final static int QUADRANT_BOTTOMRIGHT = 4;

    private final static int SNAP_TO_GRID_SIZE = 10;
    private final static int RESIZER_REFRESH_MS = 10;
    private final static int MIN_WIDTH = 20;
    private final static int MIN_HEIGHT = 20;

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Part part;
    private Component within;
    private boolean done = false;
    private int fromQuadrant;
    private Rectangle originalBounds;
    
    private class ResizerTask implements Runnable {

        @Override
        public void run () {        
            Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouseLoc, within);
            Point partLoc = originalBounds.getLocation();

            int newWidth = KeyboardManager.isShiftDown ? ((mouseLoc.x / SNAP_TO_GRID_SIZE) * SNAP_TO_GRID_SIZE) - partLoc.x : mouseLoc.x - partLoc.x;
            int newHeight = KeyboardManager.isShiftDown ? ((mouseLoc.y / SNAP_TO_GRID_SIZE) * SNAP_TO_GRID_SIZE) - partLoc.y : mouseLoc.y - partLoc.y;

            try {
                if (newWidth >= MIN_WIDTH)
                    part.setProperty(PartModel.PROP_WIDTH, new Value(newWidth));

                if (newHeight >= MIN_HEIGHT)
                    part.setProperty(PartModel.PROP_HEIGHT, new Value(newHeight));

            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            
            if (!done) {
                executor.schedule(this, RESIZER_REFRESH_MS, TimeUnit.MILLISECONDS);
            }
        }
    }

    public PartResizer (Part part, Component within, int fromQuadrant) {
        this.part = part;
        this.within = within;
        this.fromQuadrant = fromQuadrant;
        this.originalBounds = new Rectangle(part.getRect());

        MouseManager.notifyOnMouseReleased(() -> done = true);
        executor.schedule(new ResizerTask(), 0, TimeUnit.MILLISECONDS);
    }
}
