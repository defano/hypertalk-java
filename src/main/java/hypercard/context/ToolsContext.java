package hypercard.context;

import com.defano.jmonet.canvas.PaintCanvas;
import com.defano.jmonet.model.ImmutableProvider;
import com.defano.jmonet.model.PaintToolType;
import com.defano.jmonet.model.Provider;
import com.defano.jmonet.tools.base.AbstractBoundsTool;
import com.defano.jmonet.tools.base.AbstractSelectionTool;
import com.defano.jmonet.tools.base.PaintTool;
import com.defano.jmonet.tools.brushes.BasicBrush;
import com.defano.jmonet.tools.builder.PaintToolBuilder;
import hypercard.HyperCard;
import hypercard.parts.CardPart;
import hypercard.parts.model.StackModelObserver;
import hypercard.patterns.HyperCardPatternFactory;
import hypertalk.ast.common.Tool;

import java.awt.*;
import java.awt.image.BufferedImage;


public class ToolsContext implements StackModelObserver {

    private final static ToolsContext instance = new ToolsContext();

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
    private Provider<PaintTool> toolProvider = new Provider<>(PaintToolBuilder.create(PaintToolType.ARROW).build());
    private Provider<Boolean> drawMultiple = new Provider<>(false);
    private Provider<Boolean> drawCentered = new Provider<>(false);

    private ToolsContext() {
        HyperCard.getRuntimeEnv().getStack().addObserver(this);

        // Re-activate tool whenever background visibility changes
        isEditingBackground.addObserver((oldValue, newValue) -> {
            ToolsContext.getInstance().reactivateTool(HyperCard.getRuntimeEnv().getCard().getCanvas());
        });
    }

    public static ToolsContext getInstance() {
        return instance;
    }

    public Provider<Stroke> getLineStrokeProvider() {
        return lineStrokeProvider;
    }

    public void reactivateTool(PaintCanvas canvas) {
        toolProvider.get().deactivate();
        toolProvider.get().activate(canvas);
    }

    public Provider<PaintTool> getPaintToolProvider() {
        return toolProvider;
    }

    public PaintTool getPaintTool() {
        return toolProvider.get();
    }

    public void setSelectedToolType(PaintToolType selectedToolType) {

        toolProvider.get().deactivate();
        PaintTool selectedTool = PaintToolBuilder.create(selectedToolType)
                .withStrokeProvider(getStrokeProviderForTool(selectedToolType))
                .withStrokePaintProvider(linePaintProvider)
                .withFillPaintProvider(Provider.derivedFrom(fillPatternProvider, t -> isShapesFilled() || !selectedToolType.isShapeTool() ? HyperCardPatternFactory.create(t) : (Paint) null))
                .withFontProvider(fontProvider)
                .withShapeSidesProvider(shapeSidesProvider)
                .makeActiveOnCanvas(HyperCard.getRuntimeEnv().getCard().getCanvas())
                .build();

        if (selectedTool instanceof AbstractSelectionTool) {
            selectedImageProvider.setSource(((AbstractSelectionTool) selectedTool).getSelectedImageProvider());
        }

        if (selectedTool instanceof AbstractBoundsTool) {
            ((AbstractBoundsTool)selectedTool).setDrawMultiple(drawMultiple);
            ((AbstractBoundsTool)selectedTool).setDrawCentered(drawCentered);
        }

        toolProvider.set(selectedTool);
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

    public Provider<Boolean> isEditingBackgroundProvider() {
        return isEditingBackground;
    }

    public void toggleIsEditingBackground() {
        isEditingBackground.set(!isEditingBackground.get());
    }

    public void setIsEditingBackground(boolean isEditingBackground) {
        this.isEditingBackground.set(isEditingBackground);
    }

    public boolean isShapesFilled() {
        return shapesFilled.get();
    }

    public void toggleShapesFilled() {
        shapesFilled.set(!shapesFilled.get());
        setSelectedToolType(toolProvider.get().getToolType());
    }

    public Provider<Boolean> getShapesFilledProvider() {
        return shapesFilled;
    }

    public void setSelectedTool (Tool tool) {
        switch (tool) {
            case BROWSE:
                setSelectedToolType(PaintToolType.ARROW);
                break;
            case OVAL:
                setSelectedToolType(PaintToolType.OVAL);
                break;
            case BRUSH:
                setSelectedToolType(PaintToolType.PAINTBRUSH);
                break;
            case PENCIL:
                setSelectedToolType(PaintToolType.PENCIL);
                break;
            case BUCKET:
                setSelectedToolType(PaintToolType.FILL);
                break;
            case POLYGON:
                setSelectedToolType(PaintToolType.POLYGON);
                break;
            case BUTTON:
                // TODO: Not implemented
                break;
            case RECTANGLE:
                setSelectedToolType(PaintToolType.RECTANGLE);
                break;
            case CURVE:
                setSelectedToolType(PaintToolType.CURVE);
                break;
            case SHAPE:
                setSelectedToolType(PaintToolType.SHAPE);
                break;
            case ERASER:
                setSelectedToolType(PaintToolType.ERASER);
                break;
            case ROUNDRECT:
                setSelectedToolType(PaintToolType.ROUND_RECTANGLE);
                break;
            case FIELD:
                // TODO: Not implemented
                break;
            case SELECT:
                setSelectedToolType(PaintToolType.SELECTION);
                break;
            case LASSO:
                setSelectedToolType(PaintToolType.LASSO);
                break;
            case SPRAY:
                setSelectedToolType(PaintToolType.AIRBRUSH);
                break;
            case LINE:
                setSelectedToolType(PaintToolType.LINE);
                break;
            case TEXT:
                setSelectedToolType(PaintToolType.TEXT);
                break;
        }
    }

    @Override
    public void onCardClosing(CardPart oldCard) {
        toolProvider.get().deactivate();
    }

    @Override
    public void onCardOpening(CardPart newCard) {
    }

    @Override
    public void onCardOpened(CardPart newCard) {
        toolProvider.get().activate(newCard.getCanvas());
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
