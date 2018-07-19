package com.defano.wyldcard.patterns;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.runtime.context.ToolsContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WyldCardPatternFactory {

    public final static int PATTERN_WIDTH = 8;
    public final static int PATTERN_HEIGHT = 8;
    private final static WyldCardPatternFactory instance = new WyldCardPatternFactory();
    private final static int SPRITE_MATRIX_WIDTH = 17;
    private final static int SPRITE_MATRIX_HEIGHT = 12;

    private List<PatternInvalidatonObserver> observers = new ArrayList<>();
    private HashMap<Integer, TexturePaint> patternCache = new HashMap<>();

    private WyldCardPatternFactory() {
        invalidatePatternCache();
    }

    public static WyldCardPatternFactory getInstance() {
        return instance;
    }

    private static TexturePaint create(int id) {

        BufferedImage pattern = getPatternImage(id);
        for (int x = 0; x < pattern.getWidth(); x++) {
            for (int y = 0; y < pattern.getHeight(); y++) {
                if (pattern.getRGB(x, y) == 0xffffffff) {
                    pattern.setRGB(x, y, ToolsContext.getInstance().getBackgroundColor().getRGB());
                } else if (pattern.getRGB(x, y) == 0xff000000) {
                    pattern.setRGB(x, y, ToolsContext.getInstance().getForegroundColor().getRGB());
                }
            }
        }

        return new TexturePaint(pattern, new Rectangle(0, 0, pattern.getWidth(), pattern.getHeight()));
    }

    private static BufferedImage getPatternImage(int id) {

        if (WyldCard.getInstance().getFocusedStack() != null) {
            BufferedImage userPattern = WyldCard.getInstance().getFocusedStack().getStackModel().getUserPattern(id);
            if (userPattern != null) {
                return userPattern;
            }
        }

        try {
            int row = (id / 4);
            int column = (id % 4);

            int xOffset = 1 + (column * SPRITE_MATRIX_WIDTH) + column;
            int yOffset = 1 + (row * SPRITE_MATRIX_HEIGHT) + row;

            BufferedImage spriteSheet = ImageIO.read(WyldCardPatternFactory.class.getResource("/patterns/patterns.png"));
            return spriteSheet.getSubimage(xOffset, yOffset, PATTERN_WIDTH, PATTERN_HEIGHT);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read patterns.", e);
        }
    }

    public void invalidatePatternCache() {
        patternCache.clear();

        for (int index = 0; index < 40; index++) {
            patternCache.put(index, create(index));
        }

        fireObservers();
    }

    public TexturePaint getPattern(int id) {
        if (id > 39) {
            throw new IllegalArgumentException("No such pattern. Patterns are numbered 0 to 39.");
        }

        return patternCache.get(id);
    }

    public void addPatternInvalidationObserver(PatternInvalidatonObserver observer) {
        this.observers.add(observer);
    }

    private void fireObservers() {
        SwingUtilities.invokeLater(() -> {
            for (PatternInvalidatonObserver observer : observers) {
                observer.patternsInvalidated();
            }
        });
    }

}
