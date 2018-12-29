package com.defano.wyldcard.runtime.context;

import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.ast.model.SystemMessage;
import com.defano.hypertalk.ast.model.ToolType;
import com.defano.hypertalk.ast.model.Value;
import com.defano.jmonet.algo.dither.Ditherer;
import com.defano.jmonet.algo.dither.FloydSteinbergDitherer;
import com.defano.jmonet.canvas.layer.ImageLayerSet;
import com.defano.jmonet.canvas.PaintCanvas;
import com.defano.jmonet.model.Interpolation;
import com.defano.jmonet.model.PaintToolType;
import com.defano.jmonet.tools.SelectionTool;
import com.defano.jmonet.tools.base.AbstractSelectionTool;
import com.defano.jmonet.tools.builder.PaintTool;
import com.defano.jmonet.tools.builder.PaintToolBuilder;
import com.defano.jmonet.tools.builder.StrokeBuilder;
import com.defano.jmonet.tools.selection.TransformableCanvasSelection;
import com.defano.jmonet.tools.selection.TransformableImageSelection;
import com.defano.jmonet.tools.selection.TransformableSelection;
import com.defano.jmonet.tools.util.ImageUtils;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.paint.PaintBrush;
import com.defano.wyldcard.paint.ToolMode;
import com.defano.wyldcard.patterns.WyldCardPatternFactory;
import com.google.inject.Singleton;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.Subject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static io.reactivex.subjects.BehaviorSubject.createDefault;

/**
 * A singleton representation of paint tool context. Responsible for all configurable properties of the paint subsystem
 * including line width, brush selection, pattern, poly sides, draw multiple, draw centered, grid spacing, etc.
 */
@Singleton
public class DefaultToolsManager implements ToolsManager {

    // Tool mode properties
    private final Subject<ToolMode> toolModeProvider = createDefault(ToolMode.BROWSE);

    // Properties that the tools provide to us...
    private final Subject<Optional<BufferedImage>> selectedImageProvider = createDefault(Optional.empty());

    // Properties that we provide the tools...
    private final Subject<Boolean> shapesFilledProvider = createDefault(false);
    private final Subject<Boolean> isEditingBackground = createDefault(false);
    private final Subject<Stroke> lineStrokeProvider = createDefault(StrokeBuilder.withBasicStroke().ofWidth(2).withRoundCap().withRoundJoin().build());
    private final Subject<PaintBrush> brushStrokeProvider = createDefault(PaintBrush.ROUND_12X12);
    private final Subject<Paint> linePaintProvider = createDefault(Color.black);
    private final Subject<Integer> fillPatternProvider = createDefault(0);
    private final Subject<Integer> shapeSidesProvider = createDefault(5);
    private final Subject<PaintTool> paintToolProvider = createDefault(PaintToolBuilder.create(PaintToolType.ARROW).build());
    private final Subject<Boolean> drawMultipleProvider = createDefault(false);
    private final Subject<Boolean> drawCenteredProvider = createDefault(false);
    private final Subject<Color> foregroundColorProvider = createDefault(Color.BLACK);
    private final Subject<Color> backgroundColorProvider = createDefault(Color.WHITE);
    private final Subject<Double> intensityProvider = createDefault(0.1);
    private final Subject<Ditherer> dithererProvider = createDefault(new FloydSteinbergDitherer());
    private final Subject<Interpolation> antiAliasingProvider = createDefault(Interpolation.NONE);

    // Properties that we provide the canvas
    private final Subject<Integer> gridSpacingProvider = createDefault(1);

    private PaintToolType lastToolType;
    private Disposable selectedImageSubscription;
    private Disposable gridSpacingSubscription;

    @Override
    public int getGridSpacing() {
        return gridSpacingProvider.blockingFirst();
    }

    @Override
    public void setGridSpacing(int spacing) {
        if (this.gridSpacingSubscription == null) {
            this.gridSpacingSubscription = this.gridSpacingProvider.subscribe(integer -> WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas().setGridSpacing(integer));
        }
        gridSpacingProvider.onNext(spacing);
    }

    @Override
    public Subject<Integer> getGridSpacingProvider() {
        return gridSpacingProvider;
    }

    @Override
    public Subject<Stroke> getLineStrokeProvider() {
        return lineStrokeProvider;
    }

    @Override
    public Subject<Paint> getLinePaintProvider() {
        return linePaintProvider;
    }

    @Override
    public void setLinePaint(Paint p) {
        linePaintProvider.onNext(p);
    }

    @Override
    public void reactivateTool(PaintCanvas canvas) {
        paintToolProvider.blockingFirst().deactivate();
        if (canvas != null) {
            paintToolProvider.blockingFirst().activate(canvas);
        }
    }

    @Override
    public Subject<PaintTool> getPaintToolProvider() {
        return paintToolProvider;
    }

    @Override
    public Subject<ToolMode> getToolModeProvider() {
        return toolModeProvider;
    }

    @Override
    public ToolMode getToolMode() {
        return toolModeProvider.blockingFirst();
    }

    @Override
    public PaintTool getPaintTool() {
        return paintToolProvider.blockingFirst();
    }

    @Override
    public void selectAll() {
        ((SelectionTool) forceToolSelection(ToolType.SELECT, false)).createSelection(new Rectangle(
                0,
                0,
                WyldCard.getInstance().getStackManager().getFocusedCard().getWidth() - 1,
                WyldCard.getInstance().getStackManager().getFocusedCard().getHeight() - 1)
        );
    }

    @Override
    public void select() {
        ImageLayerSet undid = WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas().undo();
        BufferedImage undidImage = undid.render();

        // Calculate minimum bounds sub-image.
        Rectangle reduction = ImageUtils.getMinimumBounds(undidImage);
        BufferedImage sub = undid.render().getSubimage(reduction.x, reduction.y, reduction.width + 1, reduction.height + 1);

        // Force selection tool and create new selection from it
        SelectionTool selectionTool = (SelectionTool) forceToolSelection(ToolType.SELECT, false);
        selectionTool.createSelection(sub, new Point(reduction.x, reduction.y));
    }

    @Override
    public Color getForegroundColor() {
        return foregroundColorProvider.blockingFirst();
    }

    @Override
    public void setForegroundColor(Color color) {
        foregroundColorProvider.onNext(color);
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColorProvider.blockingFirst();
    }

    @Override
    public void setBackgroundColor(Color color) {
        backgroundColorProvider.onNext(color);
    }

    @Override
    public Subject<Color> getBackgroundColorProvider() {
        return backgroundColorProvider;
    }

    @Override
    public Subject<Color> getForegroundColorProvider() {
        return foregroundColorProvider;
    }

    @Override
    public PaintBrush getSelectedBrush() {
        return brushStrokeProvider.blockingFirst();
    }

    @Override
    public void setSelectedBrush(PaintBrush brush) {
        brushStrokeProvider.onNext(brush);
    }

    @Override
    public Subject<PaintBrush> getSelectedBrushProvider() {
        return brushStrokeProvider;
    }

    @Override
    public void toggleDrawCentered() {
        setDrawCentered(!isDrawCentered());
    }

    @Override
    public boolean isDrawCentered() {
        return this.drawCenteredProvider.blockingFirst();
    }

    @Override
    public void setDrawCentered(boolean drawCentered) {
        this.drawCenteredProvider.onNext(drawCentered);
    }

    @Override
    public Subject<Boolean> getDrawCenteredProvider() {
        return drawCenteredProvider;
    }

    @Override
    public void toggleDrawMultiple() {
        setDrawMultiple(!isDrawMultiple());
    }

    @Override
    public boolean isDrawMultiple() {
        return this.drawMultipleProvider.blockingFirst();
    }

    @Override
    public void setDrawMultiple(boolean drawMultiple) {
        this.drawMultipleProvider.onNext(drawMultiple);
    }

    @Override
    public Subject<Boolean> getDrawMultipleProvider() {
        return drawMultipleProvider;
    }

    @Override
    public Subject<Optional<BufferedImage>> getSelectedImageProvider() {
        return selectedImageProvider;
    }

    @Override
    public BufferedImage getSelectedImage() {
        return selectedImageProvider.blockingFirst().orElse(null);
    }

    private void setSelectedImage(Observable<Optional<BufferedImage>> imageProvider) {
        if (selectedImageSubscription != null) {
            selectedImageSubscription.dispose();
        }
        selectedImageSubscription = imageProvider.subscribe(selectedImageProvider::onNext);
    }

    @Override
    public int getShapeSides() {
        return shapeSidesProvider.blockingFirst();
    }

    @Override
    public void setShapeSides(int shapeSides) {
        shapeSidesProvider.onNext(shapeSides);
    }

    @Override
    public Subject<Integer> getShapeSidesProvider() {
        return shapeSidesProvider;
    }

    @Override
    public Subject<Integer> getFillPatternProvider() {
        return fillPatternProvider;
    }

    @Override
    public int getFillPattern() {
        return fillPatternProvider.blockingFirst();
    }

    @Override
    public void setFillPattern(int pattern) {
        fillPatternProvider.onNext(pattern);
    }

    @Override
    public double getIntensity() {
        return intensityProvider.blockingFirst();
    }

    @Override
    public void setIntensity(double intensity) {
        intensityProvider.onNext(intensity);
    }

    @Override
    public Ditherer getDitherer() {
        return dithererProvider.blockingFirst();
    }

    @Override
    public void setDitherer(Ditherer ditherer) {
        dithererProvider.onNext(ditherer);
    }

    @Override
    public Subject<Interpolation> getAntiAliasingProvider() {
        return antiAliasingProvider;
    }

    @Override
    public void setAntiAliasingMode(Interpolation antiAliasingMode) {
        this.antiAliasingProvider.onNext(antiAliasingMode);
    }

    @Override
    public Observable<Ditherer> getDithererProvider() {
        return dithererProvider;
    }

    @Override
    public void toggleMagnifier() {
        if (getPaintTool().getToolType() == PaintToolType.MAGNIFIER) {
            WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas().setScale(1.0);
            forceToolSelection(ToolType.fromPaintTool(lastToolType), false);
        } else if (WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas().getScale() != 1.0) {
            WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas().setScale(1.0);
        } else {
            forceToolSelection(ToolType.MAGNIFIER, false);
        }
    }

    @Override
    public int getLineWidth() {
        return Math.round((int) ((BasicStroke) lineStrokeProvider.blockingFirst()).getLineWidth());
    }

    @Override
    public void setLineWidth(int width) {
        lineStrokeProvider.onNext(StrokeBuilder.withBasicStroke().ofWidth(width).withRoundCap().withRoundJoin().build());
    }

    @Override
    public void setPattern(int patternId) {
        fillPatternProvider.onNext(patternId);
    }

    @Override
    public boolean isEditingBackground() {
        return isEditingBackground.blockingFirst();
    }

    @Override
    public Subject<Boolean> isEditingBackgroundProvider() {
        return isEditingBackground;
    }

    @Override
    public void toggleIsEditingBackground() {
        getPaintTool().deactivate();
        isEditingBackground.onNext(!isEditingBackground.blockingFirst());
        reactivateTool(WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas());
        WyldCard.getInstance().getWindowManager().getFocusedStackWindow().invalidateWindowTitle();
    }

    @Override
    public void setIsEditingBackground(boolean isEditingBackground) {
        getPaintTool().deactivate();
        this.isEditingBackground.onNext(isEditingBackground);
        reactivateTool(WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas());
    }

    @Override
    public boolean isShapesFilled() {
        return shapesFilledProvider.blockingFirst();
    }

    @Override
    public void setShapesFilled(boolean shapesFilled) {
        this.shapesFilledProvider.onNext(shapesFilled);
    }

    @Override
    public void toggleShapesFilled() {
        shapesFilledProvider.onNext(!shapesFilledProvider.blockingFirst());
        forceToolSelection(ToolType.fromPaintTool(paintToolProvider.blockingFirst().getToolType()), false);
    }

    @Override
    public Subject<Boolean> getShapesFilledProvider() {
        return shapesFilledProvider;
    }

    @Override
    public void chooseTool(ToolType toolType) {
        WyldCard.getInstance().getStackManager().getFocusedCard().getCardModel().receiveMessage(ExecutionContext.unboundInstance(), SystemMessage.CHOOSE.messageName, ListExp.fromValues(null, new Value(toolType.getPrimaryToolName()), new Value(toolType.getToolNumber())), (command, wasTrapped, err) -> {
            if (!wasTrapped) {
                SwingUtilities.invokeLater(() -> forceToolSelection(toolType, false));
            }
        });
    }

    @Override
    public PaintTool forceToolSelection(ToolType tool, boolean keepSelection) {
        PaintTool selected = activatePaintTool(tool.toPaintTool(), keepSelection);

        switch (tool) {
            case BROWSE:
                toolModeProvider.onNext(ToolMode.BROWSE);
                break;
            case BUTTON:
                toolModeProvider.onNext(ToolMode.BUTTON);
                break;
            case FIELD:
                toolModeProvider.onNext(ToolMode.FIELD);
                break;

            default:
                toolModeProvider.onNext(ToolMode.PAINT);
                break;
        }

        return selected;
    }

    @Override
    public ToolType getSelectedTool() {
        return ToolType.fromToolMode(getToolMode(), getPaintTool().getToolType());
    }

    @Override
    public Observable<Boolean> hasTransformableImageSelectionProvider() {
        return Observable.combineLatest(
                DefaultToolsManager.this.getPaintToolProvider(),
                DefaultToolsManager.this.getSelectedImageProvider(),
                (paintTool, bufferedImage) -> paintTool instanceof TransformableImageSelection && bufferedImage.isPresent()
        );
    }

    @Override
    public Observable<Boolean> hasTransformableSelectionProvider() {
        return Observable.combineLatest(
                DefaultToolsManager.this.getPaintToolProvider(),
                DefaultToolsManager.this.getSelectedImageProvider(),
                (paintTool, bufferedImage) -> paintTool instanceof TransformableSelection && bufferedImage.isPresent()
        );
    }

    @Override
    public Observable<Boolean> hasTransformableCanvasSelectionProvider() {
        return Observable.combineLatest(
                DefaultToolsManager.this.getPaintToolProvider(),
                DefaultToolsManager.this.getSelectedImageProvider(),
                (paintTool, bufferedImage) -> paintTool instanceof TransformableCanvasSelection && bufferedImage.isPresent()
        );
    }

    private PaintTool activatePaintTool(PaintToolType selectedToolType, boolean keepSelection) {

        // Create and activate new paint tool
        PaintTool selectedTool = PaintToolBuilder.create(selectedToolType)
                .withStrokeObservable(getStrokeProviderForTool(selectedToolType))
                .withStrokePaintObservable(getStrokePaintProviderForTool(selectedToolType))
                .withFillPaintObservable(fillPatternProvider.map(t -> isShapesFilled() || !selectedToolType.isShapeTool() ? Optional.of(WyldCardPatternFactory.getInstance().getPattern(t)) : Optional.empty()))
                .withFontObservable(WyldCard.getInstance().getFontManager().getPaintFontProvider())
                .withFontColorObservable(foregroundColorProvider)
                .withShapeSidesObservable(shapeSidesProvider)
                .withIntensityObservable(intensityProvider)
                .withDrawCenteredObservable(drawCenteredProvider)
                .withDrawMultipleObservable(drawMultipleProvider)
                .withAntiAliasingObservable(antiAliasingProvider)
                .makeActiveOnCanvas(WyldCard.getInstance().getStackManager().getFocusedCard().getCanvas())
                .build();

        // When requested, move current selection over to the new tool
        if (keepSelection) {
            PaintTool lastTool = paintToolProvider.blockingFirst();
            if (lastTool instanceof AbstractSelectionTool && selectedTool instanceof AbstractSelectionTool) {
                ((AbstractSelectionTool) lastTool).morphSelection((AbstractSelectionTool) selectedTool);
            }
        }

        // Deactivate current tool
        lastToolType = paintToolProvider.blockingFirst().getToolType();
        paintToolProvider.blockingFirst().deactivate();

        // Update selected image provider (so UI can tell when a selection exists)
        if (selectedTool instanceof AbstractSelectionTool) {
            setSelectedImage(((AbstractSelectionTool) selectedTool).getSelectedImageObservable());
        }

        paintToolProvider.onNext(selectedTool);
        return selectedTool;
    }

    private Observable<Stroke> getStrokeProviderForTool(PaintToolType type) {
        switch (type) {
            case PAINTBRUSH:
            case AIRBRUSH:
            case ERASER:
                return brushStrokeProvider.map(value -> value.stroke);

            default:
                return lineStrokeProvider;
        }
    }

    private Observable<Paint> getStrokePaintProviderForTool(PaintToolType type) {
        switch (type) {
            case PAINTBRUSH:
            case AIRBRUSH:
                return fillPatternProvider.map(patternId -> WyldCardPatternFactory.getInstance().getPattern(patternId));

            default:
                return linePaintProvider;
        }
    }

}
