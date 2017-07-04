/*
 * ToolsContext
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.context;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.patterns.HyperCardPatternFactory;
import com.defano.jmonet.canvas.PaintCanvas;
import com.defano.jmonet.model.ImmutableProvider;
import com.defano.jmonet.model.PaintToolType;
import com.defano.jmonet.model.Provider;
import com.defano.jmonet.tools.base.AbstractBoundsTool;
import com.defano.jmonet.tools.base.AbstractSelectionTool;
import com.defano.jmonet.tools.base.PaintTool;
import com.defano.jmonet.tools.brushes.BasicBrush;
import com.defano.jmonet.tools.builder.PaintToolBuilder;
import com.defano.hypertalk.ast.common.Tool;

import java.awt.*;
import java.awt.image.BufferedImage;


public class ToolsContext {

    private final static ToolsContext instance = new ToolsContext();

    // Tool mode properties
    private Provider<ToolMode> toolModeProvider = new Provider<>(ToolMode.BROWSE);

    // Properties that the tools provide to us...
    private ImmutableProvider<BufferedImage> selectedImageProvider = new ImmutableProvider<>();

    // Properties that we provide the tools...
    private Provider<Boolean> shapesFilled = new Provider<>(false);
    private Provider<Boolean> isEditingBackground = new Provider<>(false);
    private Provider<Stroke> lineStrokeProvider = new Provider<>(new BasicStroke(2));
    private Provider<BasicBrush> eraserStrokeProvider = new Provider<>(BasicBrush.SQUARE_12X12);
    private Provider<BasicBrush> brushStrokeProvider = new Provider<>(BasicBrush.ROUND_12X12);
    private Provider<Paint> linePaintProvider = new Provider<>(Color.black);
    private Provider<Integer> fillPatternProvider = new Provider<>(0);
    private Provider<Integer> shapeSidesProvider = new Provider<>(5);
    private Provider<Font> fontProvider = new Provider<>(new Font("Ariel", Font.PLAIN, 24));
    private Provider<PaintTool> paintToolProvider = new Provider<>(PaintToolBuilder.create(PaintToolType.ARROW).build());
    private Provider<Boolean> drawMultiple = new Provider<>(false);
    private Provider<Boolean> drawCentered = new Provider<>(false);

    private Provider<Color> foregroundColorProvider = new Provider<>(Color.BLACK);
    private Provider<Color> backgroundColorProvider = new Provider<>(Color.WHITE);

    private PaintToolType lastToolType;

    private ToolsContext() {}

    public static ToolsContext getInstance() {
        return instance;
    }

    public void morphSelection(PaintToolType newToolType) {
        if (getPaintTool() instanceof AbstractSelectionTool && ((AbstractSelectionTool) getPaintTool()).hasSelectionBounds()) {
            AbstractSelectionTool selectionTool = (AbstractSelectionTool) getPaintTool();
            Shape selection = selectionTool.getSelectionOutline();

            PaintTool newTool = selectPaintTool(newToolType);
            if (newTool instanceof AbstractSelectionTool) {
                ((AbstractSelectionTool) newTool).createSelection(selection.getBounds());
            } else {
                throw new IllegalArgumentException("Morph tool type must be a selection tool.");
            }

        } else {
            selectPaintTool(newToolType);
        }
    }

    public Provider<Stroke> getLineStrokeProvider() {
        return lineStrokeProvider;
    }

    public Provider<Paint> getLinePaintProvider() {
        return linePaintProvider;
    }

    public void setLinePaint(Paint p) {
        linePaintProvider.set(p);
    }

    public void reactivateTool(PaintCanvas canvas) {
        paintToolProvider.get().deactivate();
        paintToolProvider.get().activate(canvas);
    }

    public Provider<PaintTool> getPaintToolProvider() {
        return paintToolProvider;
    }

    public void setToolMode(ToolMode mode) {
        if (mode != ToolMode.PAINT) {
            selectPaintTool(PaintToolType.ARROW);
        }

        toolModeProvider.set(mode);
    }

    public Provider<ToolMode> getToolModeProvider() {
        return toolModeProvider;
    }

    public ToolMode getToolMode() {
        return toolModeProvider.get();
    }

    public PaintTool getPaintTool() {
        return paintToolProvider.get();
    }

    public PaintTool selectPaintTool(PaintToolType selectedToolType) {

        lastToolType = paintToolProvider.get().getToolType();
        paintToolProvider.get().deactivate();

        PaintTool selectedTool = PaintToolBuilder.create(selectedToolType)
                .withStrokeProvider(getStrokeProviderForTool(selectedToolType))
                .withStrokePaintProvider(linePaintProvider)
                .withFillPaintProvider(Provider.derivedFrom(fillPatternProvider, t -> isShapesFilled() || !selectedToolType.isShapeTool() ? HyperCardPatternFactory.create(t) : (Paint) null))
                .withFontProvider(fontProvider)
                .withShapeSidesProvider(shapeSidesProvider)
                .makeActiveOnCanvas(HyperCard.getInstance().getCard().getCanvas())
                .build();

        if (selectedTool instanceof AbstractSelectionTool) {
            selectedImageProvider.setSource(((AbstractSelectionTool) selectedTool).getSelectedImageProvider());
        }

        if (selectedTool instanceof AbstractBoundsTool) {
            ((AbstractBoundsTool)selectedTool).setDrawMultiple(drawMultiple);
            ((AbstractBoundsTool)selectedTool).setDrawCentered(drawCentered);
        }

        if (selectedToolType != PaintToolType.ARROW) {
            setToolMode(ToolMode.PAINT);
        }

        paintToolProvider.set(selectedTool);
        return selectedTool;
    }

    public void setForegroundColor(Color color) {
        foregroundColorProvider.set(color);
    }

    public void setBackgroundColor(Color color) {
        backgroundColorProvider.set(color);
    }

    public Color getForegroundColor() {
        return foregroundColorProvider.get();
    }

    public Color getBackgroundColor() {
        return backgroundColorProvider.get();
    }

    public Provider<Color> getBackgroundColorProvider() {
        return backgroundColorProvider;
    }

    public Provider<Color> getForegroundColorProvider() {
        return foregroundColorProvider;
    }

    public void setSelectedBrush(BasicBrush brush) {
        brushStrokeProvider.set(brush);
    }

    public Provider<BasicBrush> getSelectedBrushProvider() {
        return brushStrokeProvider;
    }

    public void toggleDrawCentered() {
        drawCentered.set(!drawCentered.get());
    }

    public Provider<Boolean> getDrawCenteredProvider() {
        return drawCentered;
    }

    public void toggleDrawMultiple() {
        drawMultiple.set(!drawMultiple.get());
    }

    public Provider<Boolean> getDrawMultipleProvider() {
        return drawMultiple;
    }

    public ImmutableProvider<BufferedImage> getSelectedImageProvider() {
        return selectedImageProvider;
    }

    public void setShapeSides(int shapeSides) {
        shapeSidesProvider.set(shapeSides);
    }

    public Provider<Integer> getShapeSidesProvider() {
        return shapeSidesProvider;
    }

    public Provider<Integer> getFillPatternProvider() {
        return fillPatternProvider;
    }

    public void toggleMagnifier() {
        if (getPaintTool().getToolType() == PaintToolType.MAGNIFIER) {
            HyperCard.getInstance().getCard().getCanvas().setScale(1.0);
            selectPaintTool(lastToolType);
        } else if (HyperCard.getInstance().getCard().getCanvas().getScale() != 1.0) {
            HyperCard.getInstance().getCard().getCanvas().setScale(1.0);
        }
        else {
            selectPaintTool(PaintToolType.MAGNIFIER);
        }
    }

    public void setFontSize(int size) {
        String currentFamily = fontProvider.get().getFamily();
        int currentStyle = fontProvider.get().getStyle();

        fontProvider.set(new Font(currentFamily, currentStyle, size));
    }

    public void setFontStyle(int style) {
        String currentFamily = fontProvider.get().getFamily();
        int currentSize = fontProvider.get().getSize();

        fontProvider.set(new Font(currentFamily, style, currentSize));
    }

    public void setFontFamily(String fontName) {
        int currentSize = fontProvider.get().getSize();
        int currentStyle = fontProvider.get().getStyle();

        fontProvider.set(new Font(fontName, currentStyle, currentSize));
    }

    public Provider<Font> getFontProvider() {
        return fontProvider;
    }

    public void setLineWidth(int width) {
        lineStrokeProvider.set(new BasicStroke(width));
    }

    public void setPattern(int patternId) {
            fillPatternProvider.set(patternId);
    }

    public boolean isEditingBackground() {
        return isEditingBackground.get();
    }

    public ImmutableProvider<Boolean> isEditingBackgroundProvider() {
        return ImmutableProvider.from(isEditingBackground);
    }

    public void toggleIsEditingBackground() {
        getPaintTool().deactivate();
        isEditingBackground.set(!isEditingBackground.get());
        reactivateTool(HyperCard.getInstance().getCard().getCanvas());
    }

    public void setIsEditingBackground(boolean isEditingBackground) {
        getPaintTool().deactivate();
        this.isEditingBackground.set(isEditingBackground);
        reactivateTool(HyperCard.getInstance().getCard().getCanvas());
    }

    public boolean isShapesFilled() {
        return shapesFilled.get();
    }

    public void toggleShapesFilled() {
        shapesFilled.set(!shapesFilled.get());
        selectPaintTool(paintToolProvider.get().getToolType());
    }

    public Provider<Boolean> getShapesFilledProvider() {
        return shapesFilled;
    }

    public void setSelectedTool (Tool tool) {
        switch (tool) {
            case BROWSE:
                setToolMode(ToolMode.BROWSE);
                break;
            case OVAL:
                selectPaintTool(PaintToolType.OVAL);
                break;
            case BRUSH:
                selectPaintTool(PaintToolType.PAINTBRUSH);
                break;
            case PENCIL:
                selectPaintTool(PaintToolType.PENCIL);
                break;
            case BUCKET:
                selectPaintTool(PaintToolType.FILL);
                break;
            case POLYGON:
                selectPaintTool(PaintToolType.POLYGON);
                break;
            case BUTTON:
                toolModeProvider.set(ToolMode.BUTTON);
                break;
            case RECTANGLE:
                selectPaintTool(PaintToolType.RECTANGLE);
                break;
            case CURVE:
                selectPaintTool(PaintToolType.FREEFORM);
                break;
            case SHAPE:
                selectPaintTool(PaintToolType.SHAPE);
                break;
            case ERASER:
                selectPaintTool(PaintToolType.ERASER);
                break;
            case ROUNDRECT:
                selectPaintTool(PaintToolType.ROUND_RECTANGLE);
                break;
            case FIELD:
                toolModeProvider.set(ToolMode.FIELD);
                break;
            case SELECT:
                selectPaintTool(PaintToolType.SELECTION);
                break;
            case LASSO:
                selectPaintTool(PaintToolType.LASSO);
                break;
            case SPRAY:
                selectPaintTool(PaintToolType.AIRBRUSH);
                break;
            case LINE:
                selectPaintTool(PaintToolType.LINE);
                break;
            case TEXT:
                selectPaintTool(PaintToolType.TEXT);
                break;
        }
    }

    public Tool getSelectedTool() {
        return Tool.fromToolMode(getToolMode(), getPaintTool().getToolType());
    }

    private Provider<Stroke> getStrokeProviderForTool(PaintToolType type) {
        switch (type) {
            case PAINTBRUSH:
            case AIRBRUSH:
                return new Provider<>(brushStrokeProvider, value -> value.stroke);

            case ERASER:
                return new Provider<>(eraserStrokeProvider, value -> value.stroke);

            default:
                return lineStrokeProvider;
        }
    }

}
