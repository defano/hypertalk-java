package com.defano.wyldcard.parts.card;

import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.ast.expressions.LiteralExp;
import com.defano.hypertalk.ast.model.*;
import com.defano.jmonet.canvas.JMonetCanvas;
import com.defano.jmonet.canvas.PaintCanvas;
import com.defano.jmonet.canvas.layer.ImageLayerSet;
import com.defano.jmonet.canvas.observable.CanvasCommitObserver;
import com.defano.jmonet.clipboard.CanvasTransferDelegate;
import com.defano.jmonet.clipboard.CanvasTransferHandler;
import com.defano.jmonet.tools.MarqueeTool;
import com.defano.jmonet.tools.base.SelectionTool;
import com.defano.jmonet.tools.base.Tool;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.aspect.RunOnDispatch;
import com.defano.wyldcard.awt.MouseListenable;
import com.defano.wyldcard.awt.MouseStillDown;
import com.defano.wyldcard.parts.Part;
import com.defano.wyldcard.parts.ToolEditablePart;
import com.defano.wyldcard.parts.bkgnd.BackgroundModel;
import com.defano.wyldcard.parts.button.ButtonModel;
import com.defano.wyldcard.parts.button.ButtonPart;
import com.defano.wyldcard.parts.clipboard.CardPartTransferHandler;
import com.defano.wyldcard.parts.field.FieldModel;
import com.defano.wyldcard.parts.field.FieldPart;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.parts.model.PropertyChangeObserver;
import com.defano.wyldcard.parts.model.WyldCardPropertiesModel;
import com.defano.wyldcard.parts.stack.StackModel;
import com.defano.wyldcard.parts.util.TextArrowsMessageCompletionObserver;
import com.defano.wyldcard.runtime.PartTable;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.util.ThreadUtils;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * The "controller" object that represents a card in a WyldCard stack.
 *
 * Note that this controller is responsible for the merged view of foreground and background layers, and aggregates
 * all card and background graphics, buttons and fields.
 *
 * See {@link CardLayeredPane} for the view object, a Swing component.
 * See {@link CardModel} and {@link BackgroundModel} for the model object.
 */
public class CardPart extends CardLayeredPane implements Part<CardModel>, CanvasCommitObserver, CanvasTransferDelegate, MouseListenable, KeyListener, PropertyChangeObserver {

    private CardModel cardModel;

    private final static int CANVAS_UNDO_DEPTH = 20;

    private final PartTable<FieldPart> fields = new PartTable<>();
    private final PartTable<ButtonPart> buttons = new PartTable<>();

    private final EditingBackgroundObserver editingBackgroundObserver = new EditingBackgroundObserver(this);
    private final ForegroundScaleObserver foregroundScaleObserver = new ForegroundScaleObserver(this);
    private final BackgroundScaleObserver backgroundScaleObserver = new BackgroundScaleObserver(this);
    private final CardModelObserver cardModelObserver = new CardPartModelObserver(this);

    private Disposable editingBackgroundSubscription;
    private Disposable foregroundScaleSubscription;
    private Disposable backgroundScaleSubscription;

    /**
     * Instantiates the CardPart occurring at a specified position in a the stack.
     *
     * @param context The execution context.
     * @param cardIndex The location in the stack where the card should be created.
     * @param stack The stack data model containing the card to return
     * @return The CardPart.
     */
    public static CardPart fromPositionInStack(ExecutionContext context, int cardIndex, StackModel stack) {
        return fromModel(context, stack.getCardModel(cardIndex));
    }

    /**
     * Instantiates a CardPart given a {@link CardModel} and {@link StackModel}.
     *
     * @param context The execution context.
     * @param model The model of the card to instantiate.
     * @return The fully instantiated CardPart.
     */
    public static CardPart fromModel(ExecutionContext context, CardModel model) {
        CardPart card = new CardPart();
        card.cardModel = model;

        // Add card parts to this card
        for (PartModel thisPart : model.getPartModels(context)) {
            card.addPartToView(context, thisPart);
        }

        // Add background parts to this card
        for (PartModel thisPart : model.getBackgroundModel().getPartModels(context)) {
            card.addPartToView(context, thisPart);
        }

        return card;
    }

    /**
     * Adds a new button to this card, activates the button tool from the tool palette and makes the new button the
     * active part selection.
     *
     * The new button is added to the layer (foreground or background) currently being edited.
     *
     * @param context The execution context
     * @param rectangle The location and size of the new button, or null to produce a default-sized button in the
     *                  center of the card.
     * @return The newly created button.
     */
    @RunOnDispatch
    public ButtonPart newButton(ExecutionContext context, Rectangle rectangle) {
        ButtonPart newButton = ButtonPart.newButton(context, this, CardLayerPart.getActivePartLayer().asOwner(), rectangle);
        addNewPartToCard(context, newButton);
        return newButton;
    }

    /**
     * Adds a new field to this card, activates the field tool from the tool palette and makes the new field the
     * active part selection.
     *
     * The new field is added to the layer (foreground or background) currently being edited.
     *
     * @param context The execution context
     * @param rectangle The location and size of the new field, or null to produce a default-sized field in the
     *                  center of the card.
     * @return The newly created field.
     */
    @RunOnDispatch
    public FieldPart newField(ExecutionContext context, Rectangle rectangle) {
        FieldPart newField = FieldPart.newField(context, this, CardLayerPart.getActivePartLayer().asOwner(), rectangle);
        addNewPartToCard(context, newField);
        return newField;
    }

    /**
     * Adds a new part to this card or its background.
     *
     * This method adds the part to the card (or its background) model, depending on the owning layer specified by the
     * part. It makes the part visible on the card, sends either the 'newButton' or 'newField' to the part (and its
     * message passing order), and finally makes the part the active selection with either the button or field tool,
     * as applicable.
     *
     * This method should only be used to add new parts to a card that is presently open and displayed in a stack
     * window.
     *
     * @param context The execution context
     * @param newPart The new part to be added, one of {@link ButtonPart} or {@link FieldPart}.
     */
    @RunOnDispatch
    public void addNewPartToCard(ExecutionContext context, ToolEditablePart newPart) {
        String newPartMessage;

        // Add the part to the parts table
        if (newPart instanceof ButtonPart) {
            buttons.add((ButtonPart) newPart);
            newPartMessage = SystemMessage.NEW_BUTTON.messageName;
        } else if (newPart instanceof FieldPart) {
            fields.add((FieldPart) newPart);
            newPartMessage = SystemMessage.NEW_FIELD.messageName;
        } else {
            throw new IllegalStateException("Bug! Can't add this part to a card: " + newPart);
        }

        // Register the part as a child component of the foreground or background layer
        if (newPart.getPartModel().getLayer() == CardLayer.CARD_PARTS) {
            cardModel.addPartModel(newPart.getPartModel());
        } else if (newPart.getPartModel().getLayer() == CardLayer.BACKGROUND_PARTS) {
            cardModel.getBackgroundModel().addPartModel(newPart.getPartModel());
        }

        // Add the Swing component (view) to the card
        addSwingComponent(newPart.getComponent(), newPart.getRect(context), newPart.getPartModel().getLayer());
        newPart.partOpened(context);

        // Send the 'newButton' or 'newField' message to the part
        newPart.getPartModel().receiveMessage(context.bind(this), newPartMessage);

        // Make the part tool active and select the newly created part
        WyldCard.getInstance().getPartToolManager().setSelectedPart(newPart);
    }

    /**
     * Gets an unordered collection of buttons that appear on this card in either the foreground or background layer.
     * @return The buttons on this card.
     */
    public Collection<ButtonPart> getButtons() {
        return buttons.getAll();
    }

    /**
     * Gets an unordered collection of fields that appear on this card in either the foreground or background layer.
     * @return The fields on this card.
     */
    public Collection<FieldPart> getFields() {
        return fields.getAll();
    }

    /**
     * Gets the active paint canvas associated with this card, either the foreground or background canvas depending
     * on whether the user is currently editing the card's background.
     * @return The active paint canvas for this card.
     */
    public JMonetCanvas getActiveCanvas() {
        return WyldCard.getInstance().getToolsManager().isEditingBackground() ? getBackgroundCanvas() : getForegroundCanvas();
    }

    /**
     * Hides or shows the card foreground, including the canvas and all parts. When made visible, only those parts
     * whose visible property is true will actually become visible.
     *
     * @param context The execution context.
     * @param isEditingBackground Hides the foreground when true; shows it otherwise.
     */
    @RunOnDispatch
    void setEditingBackground(ExecutionContext context, boolean isEditingBackground) {
        if (getForegroundCanvas() != null) {
            getForegroundCanvasScrollPane().setVisible(!isEditingBackground);

            setPartsVisible(context, Owner.CARD, !isEditingBackground);
        }
    }

    /**
     * Determines whether the foreground layer of the card is hidden, revealing only the background layer underneath it.
     * @return True if foreground layer has been hidden, false otherwise.
     */
    public boolean isEditingBackground() {
        return getForegroundCanvas() == null || !getForegroundCanvas().isVisible();
    }

    /**
     * Hides or shows all parts on the card's foreground. When made visible, only those parts whose visible property is
     * true will actually become visible.
     *
     * @param context The execution context.
     * @param visible True to show card parts; false to hide them
     */
    void setPartsVisible(ExecutionContext context, Owner owningLayer, boolean visible) {
        ThreadUtils.invokeAndWaitAsNeeded(() -> {
            for (PartModel thisPartModel : getPartModel().getPartModels(context)) {
                if (thisPartModel.getOwner() == owningLayer) {
                    if (!visible) {
                        getPart(thisPartModel).getComponent().setVisible(false);
                    } else {
                        getPart(thisPartModel).getComponent().setVisible(thisPartModel.getKnownProperty(context, PartModel.PROP_VISIBLE).booleanValue());
                    }
                }
            }
        });
    }

    /**
     * Hides or shows the background layer for this card.
     * @param context The execution context.
     * @param isVisible True to show the background; false to hide it.
     */
    @RunOnDispatch
    void setBackgroundVisible(ExecutionContext context, boolean isVisible) {
        if (getBackgroundCanvas() != null) {
            getBackgroundCanvas().setVisible(isVisible);
        }

        setPartsVisible(context, Owner.BACKGROUND, isVisible);
    }

    /**
     * Removes a button from this card view. Does not affect the {@link CardModel}. Has no effect if the button does
     * not exist on the card.
     *
     * @param context The execution context.
     * @param buttonModel The button to be removed.
     */
    @RunOnDispatch
    void closeButton(ExecutionContext context, ButtonModel buttonModel) {
        ButtonPart button = buttons.get(buttonModel);

        if (button != null) {
            buttons.remove(button);
            removeSwingComponent(button.getComponent());
            button.partClosed(context);
        }
    }

    /**
     * Removes a field from this card view. Does not affect the {@link CardModel}. Has no effect if the field does not
     * exist on the card.
     *
     * @param context The execution context.
     * @param fieldModel The field to be removed.
     */
    @RunOnDispatch
    void closeField(ExecutionContext context, FieldModel fieldModel) {
        FieldPart field = fields.get(fieldModel);

        if (field != null) {
            fields.remove(field);
            removeSwingComponent(field.getComponent());
            field.partClosed(context);
        }
    }

    /**
     * Swap the Swing component associated with a given part (button or field) for a new component. This is useful
     * when changing button or field styles.
     *
     * @param context The execution context.
     * @param forPart The part whose Swing component should be replaced.
     * @param oldButtonComponent The old Swing component associated with this part.
     * @param newButtonComponent The new Swing component to be used.
     */
    @RunOnDispatch
    public void replaceViewComponent(ExecutionContext context, Part forPart, Component oldButtonComponent, Component newButtonComponent) {
        CardLayer partLayer = getCardLayer(oldButtonComponent);
        removeSwingComponent(oldButtonComponent);
        addSwingComponent(newButtonComponent, forPart.getRect(context), partLayer);
        forPart.partOpened(context);
        onDisplayOrderChanged(context);
    }

    /**
     * Indicates that the z-order of a part changed (and that components should be reordered on the pane according to
     * their new position).
     * @param context The execution context.
     */
    public void onDisplayOrderChanged(ExecutionContext context) {
        // Redraw all parts in their display order
        SwingUtilities.invokeLater(() -> {
            for (PartModel thisPart : getPartModel().getPartsInDisplayOrder(context)) {
                moveToFront(getPart(thisPart).getComponent());
            }
        });
    }

    /**
     * Gets a screenshot of this card; a pixel accurate rendering of the card as would be visible to the user when the
     * card is visible in the stack window including graphics and part layers (the rendering of which will differ
     * based on Swing's current look-and-feel setting).
     *
     * @return A screenshot image of this card.
     */
    public BufferedImage getScreenshot() {

        // Swing cannot print components that are not actively displayed in a window (this is a side effect of the
        // native component peering architecture). Therefore, if this card is not already being displayed in a
        // window, we will need to create a window and place ourselves inside of it before attempting to print. However,
        // note that a card (or any component) cannot be the content pane of multiple frames simultaneously, so we
        // utilize the screenshot buffer frame only when not already attached to a card window.
        if (getRootPane() == null) {
            WyldCard.getInstance().getWindowManager().getScreenshotBufferWindow().setContentPane(this);
        }

        BufferedImage screenshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = screenshot.createGraphics();
        ThreadUtils.invokeAndWaitAsNeeded(() -> CardPart.this.printAll(g));
        g.dispose();

        return screenshot;
    }

    /** {@inheritDoc} */
    @Override
    public void onCommit(PaintCanvas canvas, ImageLayerSet imageLayerSet, BufferedImage canvasImage) {

        // Save the modified canvas image to the card or background model
        if (WyldCard.getInstance().getToolsManager().isEditingBackground()) {
            cardModel.getBackgroundModel().setBackgroundImage(canvasImage);
        } else {
            cardModel.setCardImage(canvasImage);
        }
    }

    /** {@inheritDoc} */
    @Override
    public BufferedImage copySelection() {
        Tool activeTool = WyldCard.getInstance().getToolsManager().getPaintTool();
        if (activeTool instanceof SelectionTool) {
            return ((SelectionTool) activeTool).getSelectedImage();
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteSelection() {
        Tool activeTool = WyldCard.getInstance().getToolsManager().getPaintTool();
        if (activeTool instanceof SelectionTool) {
            ((SelectionTool) activeTool).deleteSelection();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void pasteSelection(BufferedImage image) {
        int cardCenterX = getWidth() / 2;
        int cardCenterY = getHeight() / 2;

        MarqueeTool tool = (MarqueeTool) WyldCard.getInstance().getToolsManager().forceToolSelection(ToolType.SELECT, false);
        tool.createSelection(image, new Point(cardCenterX - image.getWidth() / 2, cardCenterY - image.getHeight() / 2));
    }

    /** {@inheritDoc} */
    @Override
    public int getHeight() {
        return cardModel.getStackModel().getHeight(new ExecutionContext());
    }

    /** {@inheritDoc} */
    @Override
    public int getWidth() {
        return cardModel.getStackModel().getWidth(new ExecutionContext());
    }

    /**
     * Adds a part to this card's view. This method is intended to add parts that are already part of the card or
     * background's model to the view when the card is "re-hydrated" from its model. Has no effect if the specified
     * part cannot be added to a card.
     *
     * This method does not affect the model's understanding of how many parts exist on the card or background. Use
     * {@link #addNewPartToCard(ExecutionContext, ToolEditablePart)} to add a new button or field to this card.
     *
     * @param context The execution context.
     * @param thisPart The data model of the part to be added.
     */
    @RunOnDispatch
    private void addPartToView(ExecutionContext context, PartModel thisPart) {
        switch (thisPart.getType()) {
            case BUTTON:
                ButtonPart button = ButtonPart.fromModel(context, this, (ButtonModel) thisPart);
                buttons.add(button);
                addSwingComponent(button.getComponent(), button.getRect(context), thisPart.getLayer());
                break;
            case FIELD:
                FieldPart field = FieldPart.fromModel(context, this, (FieldModel) thisPart);
                fields.add(field);
                addSwingComponent(field.getComponent(), field.getRect(context), thisPart.getLayer());
                break;
            default:
                // Intentionally ignored
        }
    }

    /**
     * Removes a Swing component from this card's JLayeredPane.
     * @param component The component to remove.
     */
    @RunOnDispatch
    private void removeSwingComponent(Component component) {
        remove(component);
        revalidate(); repaint();
    }

    /**
     * Adds a Swing component to this card's JLayeredPane.
     * @param component The component to add.
     * @param bounds The component's desired location and size.
     */
    @RunOnDispatch
    private void addSwingComponent(Component component, Rectangle bounds, CardLayer layer) {
        component.setBounds(bounds);
        addToCardLayer(component, layer);
        moveToFront(component);

        revalidate(); repaint();
    }

    /** {@inheritDoc} */
    @Override
    public PartType getType() {
        return PartType.CARD;
    }

    /** {@inheritDoc} */
    @Override
    public CardModel getPartModel() {
        return cardModel;
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void partOpened(ExecutionContext context) {
        StackModel stack = getOwningStackModel();
        Dimension dimension = stack.getSize(context);

        // Setup part cut, copy and paste
        setTransferHandler(new CardPartTransferHandler(this));

        // Setup the foreground paint canvas
        BufferedImage cardImage = cardModel.getCardImage(dimension);
        setForegroundCanvas(new JMonetScrollPane(new JMonetCanvas(cardImage, CANVAS_UNDO_DEPTH)));
        getForegroundCanvas().addCanvasCommitObserver(this);
        getForegroundCanvas().setTransferHandler(new CanvasTransferHandler(getForegroundCanvas(), this));
        getForegroundCanvas().setSize(stack.getWidth(context), stack.getHeight(context));

        // Setup the background paint canvas
        BufferedImage backgroundImage = getPartModel().getBackgroundModel().getBackgroundImage(dimension);
        setBackgroundCanvas(new JMonetScrollPane(new JMonetCanvas(backgroundImage, CANVAS_UNDO_DEPTH)));
        getBackgroundCanvas().addCanvasCommitObserver(this);
        getBackgroundCanvas().setTransferHandler(new CanvasTransferHandler(getBackgroundCanvas(), this));
        getBackgroundCanvas().setSize(stack.getWidth(context), stack.getHeight(context));

        // Resize card (Swing) component
        setMaximumSize(stack.getSize(context));
        setSize(stack.getWidth(context), stack.getHeight(context));

        // Fire property change observers on the parts (so that they can draw themselves in their correct initial state)
        for (ButtonPart thisButton : buttons.getAll()) {
            thisButton.getPartModel().notifyPropertyChangedObserver(context, thisButton);
        }

        for (FieldPart thisField : fields.getAll()) {
            thisField.getPartModel().notifyPropertyChangedObserver(context, thisField);
        }

        editingBackgroundSubscription = WyldCard.getInstance().getToolsManager().isEditingBackgroundProvider().subscribe(editingBackgroundObserver);
        foregroundScaleSubscription = getForegroundCanvas().getScaleObservable().subscribe(foregroundScaleObserver);
        backgroundScaleSubscription = getBackgroundCanvas().getScaleObservable().subscribe(backgroundScaleObserver);

        getForegroundCanvas().addMouseListener(this);
        getForegroundCanvas().addKeyListener(this);

        getPartModel().receiveMessage(context.bind(this), SystemMessage.OPEN_CARD.messageName);
        getPartModel().setObserver(cardModelObserver);

        getPartModel().addPropertyChangedObserver(this);
        getPartModel().notifyPropertyChangedObserver(context, this);

        getPartModel().getBackgroundModel().addPropertyChangedObserver(this);
        getPartModel().getBackgroundModel().notifyPropertyChangedObserver(context, this);
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void partClosed(ExecutionContext context) {
        getPartModel().receiveMessage(context.bind(this), SystemMessage.CLOSE_CARD.messageName);

        // Lets parts know they're about to go away
        for (ButtonPart p : buttons.getAll()) {
            p.partClosed(context);
        }

        for (FieldPart p : fields.getAll()) {
            p.partClosed(context);
        }

        // Remove their Swing components from the card to free memory
        removeAll();

        editingBackgroundSubscription.dispose();
        foregroundScaleSubscription.dispose();
        backgroundScaleSubscription.dispose();

        getForegroundCanvas().removeMouseListener(this);
        getForegroundCanvas().removeKeyListener(this);

        getForegroundCanvas().dispose();
        getBackgroundCanvas().dispose();

        setTransferHandler(null);
        getPartModel().setObserver(null);

        getPartModel().removePropertyChangedObserver(this);
        getPartModel().getBackgroundModel().removePropertyChangedObserver(this);

        super.dispose();
    }

    /**
     * Returns the CardLayerPart represented by the given PartModel.
     *
     * @param partModel The PartModel associated with the desired Part.
     * @throws IllegalArgumentException Thrown if no such part exists on this card
     * @return The matching CardLayerPart
     */
    public CardLayerPart getPart(PartModel partModel) {
        CardLayerPart part = null;

        if (partModel instanceof FieldModel) {
            part = fields.get(partModel);
        } else if (partModel instanceof ButtonModel) {
            part = buttons.get(partModel);
        }

        if (part == null) {
            throw new IllegalArgumentException("No part on card " + this.getPartModel() + " for model: " + partModel);
        }

        return part;
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_DOUBLE_CLICK.messageName);
        }

        // Search results are reset/cleared whenever the card is clicked
        WyldCard.getInstance().getSearchManager().reset();
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void mousePressed(MouseEvent e) {
        getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_DOWN.messageName);
        MouseStillDown.then(() -> getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_STILL_DOWN.messageName));
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void mouseReleased(MouseEvent e) {
        getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_UP.messageName);
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void mouseEntered(MouseEvent e) {
        getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_ENTER.messageName);
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void mouseExited(MouseEvent e) {
        getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_LEAVE.messageName);
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void keyTyped(KeyEvent e) {
        getPartModel().receiveMessage(
                new ExecutionContext(this),
                SystemMessage.KEY_DOWN.messageName, new ListExp(null, new LiteralExp(null, String.valueOf(e.getKeyChar()))));
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void keyPressed(KeyEvent e) {
        BoundSystemMessage bsm = SystemMessage.fromKeyEvent(e, false);
        if (bsm != null) {
            getPartModel().receiveMessage(
                    new ExecutionContext(this),
                    bsm.message.messageName,
                    bsm.boundArguments,
                    new TextArrowsMessageCompletionObserver(this, e));
        }
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void keyReleased(KeyEvent e) {
        // Nothing to do
    }

    /** {@inheritDoc} */
    @Override
    @RunOnDispatch
    public void onPropertyChanged(ExecutionContext context, WyldCardPropertiesModel model, String property, Value oldValue, Value newValue) {
        if (CardModel.PROP_SHOWPICT.equals(property.toLowerCase())) {
            if (model == getPartModel()) {
                setCardImageVisible(newValue.booleanValue());
            } else if (model == getPartModel().getBackgroundModel()) {
                setBackgroundImageVisible(newValue.booleanValue());
            }
        }
    }

}
