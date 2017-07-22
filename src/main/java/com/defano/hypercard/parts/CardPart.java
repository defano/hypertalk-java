/*
 * CardPart
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.parts;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.Serializer;
import com.defano.hypercard.context.PartToolContext;
import com.defano.hypercard.context.PartsTable;
import com.defano.hypercard.context.ToolsContext;
import com.defano.hypercard.gui.util.FileDrop;
import com.defano.hypercard.gui.util.ImageImporter;
import com.defano.hypercard.parts.clipboard.CardPartTransferHandler;
import com.defano.hypercard.parts.model.*;
import com.defano.hypercard.parts.model.ButtonModel;
import com.defano.hypercard.runtime.WindowManager;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.exception.HtException;
import com.defano.jmonet.canvas.ChangeSet;
import com.defano.jmonet.canvas.PaintCanvas;
import com.defano.jmonet.canvas.UndoablePaintCanvas;
import com.defano.jmonet.canvas.observable.CanvasCommitObserver;
import com.defano.jmonet.clipboard.CanvasTransferDelegate;
import com.defano.jmonet.clipboard.CanvasTransferHandler;
import com.defano.jmonet.model.PaintToolType;
import com.defano.jmonet.tools.SelectionTool;
import com.defano.jmonet.tools.base.AbstractSelectionTool;
import com.defano.jmonet.tools.base.PaintTool;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * A view object representing a card in the stack; extends the Swing JLayeredPane component.
 */
public class CardPart extends CardLayeredPane implements PartContainer, CanvasCommitObserver, CanvasTransferDelegate {

    private CardModel cardModel;
    private StackModel stackModel;

    private PartsTable<FieldPart> fields = new PartsTable<>();
    private PartsTable<ButtonPart> buttons = new PartsTable<>();

    private CardPart() {
        super();
        this.setLayout(null);
    }

    /**
     * Instantiates the CardPart at a specified location in a specified stack.
     *
     * @param cardIndex The location in the stack whose card should be returned.
     * @param stack The stack data model containing the card to return
     * @return The CardPart.
     * @throws HtException Thrown if an error occurs creating the card.
     */
    public static CardPart fromLocationInStack(int cardIndex, StackModel stack) throws HtException {
        CardPart card = new CardPart();
        card.cardModel = stack.getCardModel(cardIndex);
        card.stackModel = stack;

        // Add card parts to this card
        for (PartModel thisPart : card.cardModel.getPartModels()) {
            card.addPartFromModel(thisPart, CardLayer.CARD_PARTS);
        }

        // Add background parts to this card
        for (PartModel thisPart : card.getCardBackground().getPartModels()) {
            card.addPartFromModel(thisPart, CardLayer.BACKGROUND_PARTS);
        }

        // Setup part cut, copy and paste
        card.setTransferHandler(new CardPartTransferHandler(card));

        // Setup the foreground paint canvas
        card.setForegroundCanvas(new UndoablePaintCanvas(card.cardModel.getCardImage()));
        card.getForegroundCanvas().addCanvasCommitObserver(card);
        card.getForegroundCanvas().setTransferHandler(new CanvasTransferHandler(card.getForegroundCanvas(), card));
        card.getForegroundCanvas().setSize(stack.getWidth(), stack.getHeight());
        card.getForegroundCanvas().getScaleProvider().addObserver((o, arg) -> card.setBackgroundVisible(((Double) arg) == 1.0));

        // Setup the background paint canvas
        card.setBackgroundCanvas(new UndoablePaintCanvas(card.getCardBackground().getBackgroundImage()));
        card.getBackgroundCanvas().addCanvasCommitObserver(card);
        card.getBackgroundCanvas().setTransferHandler(new CanvasTransferHandler(card.getBackgroundCanvas(), card));
        card.getBackgroundCanvas().setSize(stack.getWidth(), stack.getHeight());
        card.getBackgroundCanvas().getScaleProvider().addObserver((o, arg) -> card.setForegroundVisible(((Double) arg) == 1.0));

        // Resize Swing component
        card.setMaximumSize(stack.getSize());
        card.setSize(stack.getWidth(), stack.getHeight());

        // Fire property change observers on the parts (so that they can draw themselves in their correct initial state)
        for (ButtonPart thisButton : card.buttons.getParts()) {
            thisButton.getPartModel().notifyPropertyChangedObserver(thisButton);
        }

        for (FieldPart thisField : card.fields.getParts()) {
            thisField.getPartModel().notifyPropertyChangedObserver(thisField);
        }

        // Listen for image files that are dropped onto the card
        new FileDrop(card, files -> ImageImporter.importAsSelection(files[0]));

        // Hide the foreground layer whenever the background is being edited
        ToolsContext.getInstance().isEditingBackgroundProvider().addObserver((oldValue, isEditingBackground) -> {
            card.setForegroundVisible(!(boolean) isEditingBackground);
        });

        return card;
    }

    /**
     * Imports an existing part (button or field) into this card. Note that this differs from {@link #addField(FieldPart, CardLayer)}
     * or {@link #addButton(ButtonPart, CardLayer)} in that a new ID for the part is generated before it is added to the card. This
     * method is typically used to "paste" a copied part from another card onto this card.
     *
     * @param part The part to be imported.
     * @return The newly imported part (identical to the given part, but with a new ID)
     * @throws HtException Thrown if an error occurs importing the part.
     */
    public Part importExistingPart(Part part, CardLayer layer) throws HtException {
        if (part instanceof ButtonPart) {
            return importButton((ButtonPart) part, layer);
        } else if (part instanceof FieldPart) {
            return importField((FieldPart) part, layer);
        } else {
            throw new IllegalArgumentException("Bug! Unimplemented import of part type: " + part.getClass());
        }
    }

    /**
     * Removes a part (button or field) from this card. Has no effect of the part is not on this card.
     * @param part The part to be removed.
     */
    public void removePart(Part part) {
        if (part instanceof ButtonPart) {
            removeButton((ButtonPart) part);
        } else if (part instanceof FieldPart) {
            removeField((FieldPart) part);
        }
    }

    /**
     * Adds a new button (with default attributes) to this card. Represents the behavior of the user choosing
     * "New Button" from the Objects menu.
     */
    public void newButton() {
        try {
            ButtonPart newButton = ButtonPart.newButton(this);
            addButton(newButton, Part.getActivePartLayer());
            PartToolContext.getInstance().setSelectedPart(newButton);
        } catch (PartException ex) {
            throw new RuntimeException("Bug! Shouldn't be possible.", ex);
        }
    }

    /**
     * Adds a new field (with default attributes) to this card. Represents the behavior of the user choosing
     * "New Field" from the Objects menu.
     */
    public void newField() {
        try {
            FieldPart newField = FieldPart.newField(this);
            addField(newField, Part.getActivePartLayer());
            PartToolContext.getInstance().setSelectedPart(newField);
        } catch (PartException ex) {
            throw new RuntimeException("Bug! Shouldn't be possible.", ex);
        }
    }

    /**
     * Gets an unordered collection of buttons that exist on this card in the active layer.
     * @return The buttons on this card.
     */
    @Override
    public Collection<ButtonPart> getButtons() {
        return buttons.getParts();
    }

    /**
     * Gets an unordered collection of fields that exist on this card in the active layer.
     * @return The fields on this card.
     */
    @Override
    public Collection<FieldPart> getFields() {
        return fields.getParts();
    }

    /**
     * Adds a part to a specfied layer on this card based on its model.
     * @param thisPart The data model of the part to be added.
     * @param layer The layer of this card on which the part should be added.
     * @throws HtException Thrown if an error occurs adding the part.
     */
    private void addPartFromModel(PartModel thisPart, CardLayer layer) throws HtException {
        switch (thisPart.getType()) {
            case BUTTON:
                ButtonPart button = ButtonPart.fromModel(this, (ButtonModel) thisPart);
                buttons.addPart(button);
                addSwingComponent(button.getComponent(), button.getRect(), layer);
                break;
            case FIELD:
                FieldPart field = FieldPart.fromModel(this, (FieldModel) thisPart);
                fields.addPart(field);
                addSwingComponent(field.getComponent(), field.getRect(), layer);
                break;
            default:
                throw new IllegalStateException("Bug! Unimplemented part model: " + thisPart);
        }
    }

    /**
     * Gets the data model associated with this card.
     * @return The CardModel for this card.
     */
    public CardModel getCardModel() {
        return cardModel;
    }

    /**
     * Gets the active paint canvas associated with this card, either the foreground or background canvas depending
     * on whether the user is currently editing the card's background.
     * @return The active paint canvas for this card.
     */
    public UndoablePaintCanvas getCanvas() {
        return ToolsContext.getInstance().isEditingBackground() ? getBackgroundCanvas() : getForegroundCanvas();
    }

    /**
     * Hides or shows the card foreground.
     * @param isVisible Shows the foreground when true; hides it otherwise.
     */
    @Override
    public void setForegroundVisible(boolean isVisible) {
        super.setForegroundVisible(isVisible);

        // Notify the window manager than background editing changed
        WindowManager.getStackWindow().invalidateWindowTitle();
    }

    /**
     * Gets the zero-based location of this card in its stack.
     * @return The location of this card in the stack.
     */
    public int getCardIndexInStack() {
        return getStackModel().getIndexOfCard(this.getCardModel());
    }

    /**
     * Gets the data model associated with this card's stack.
     * @return The stack model for this card.
     */
    public StackModel getStackModel() {
        return stackModel;
    }

    /**
     * Hides or shows the background layer for this card.
     * @param isVisible True to show the background; false to hide it.
     */
    private void setBackgroundVisible(boolean isVisible) {
        getBackgroundCanvas().setVisible(isVisible);
    }

    /**
     * Returns the data model associated with this card. Note that backgrounds are shared across cards; mutating this
     * object may affect other cards in the stack, too.
     * @return The data model associated with this card's background.
     */
    public BackgroundModel getCardBackground() {
        return stackModel.getBackground(cardModel.getBackgroundId());
    }

    /**
     * Swap the Swing component associated with a given part (button or field) for a new component. This is useful
     * when changing button or field styles.
     *
     * @param forPart The part whose Swing component should be replaced.
     * @param oldButtonComponent The old Swing component associated with this part.
     * @param newButtonComponent The new Swing component to be used.
     */
    public void replaceSwingComponent(Part forPart, Component oldButtonComponent, Component newButtonComponent) {
        CardLayer partLayer = getCardLayer(oldButtonComponent);
        removeSwingComponent(oldButtonComponent);
        addSwingComponent(newButtonComponent, forPart.getRect(), partLayer);
    }

    /**
     * Indicates that the z-order of a part changed (and that components should be reordered on the pane according to
     * their new position).
     */
    public void onZOrderChanged() {
        SwingUtilities.invokeLater(() -> {
            for (Part thisPart : getPartsInZOrder()) {
                moveToBack(thisPart.getComponent());
            }
        });
    }

    @Override
    public void onCommit(PaintCanvas canvas, ChangeSet changeSet, BufferedImage canvasImage) {
        if (ToolsContext.getInstance().isEditingBackground()) {
            getCardBackground().setBackgroundImage(canvasImage);
        } else {
            cardModel.setCardImage(canvasImage);
        }
    }

    @Override
    public BufferedImage copySelection() {
        PaintTool activeTool = ToolsContext.getInstance().getPaintTool();
        if (activeTool instanceof AbstractSelectionTool) {
            return ((AbstractSelectionTool) activeTool).getSelectedImage();
        }

        return null;
    }

    @Override
    public void deleteSelection() {
        PaintTool activeTool = ToolsContext.getInstance().getPaintTool();
        if (activeTool instanceof AbstractSelectionTool) {
            ((AbstractSelectionTool) activeTool).deleteSelection();
        }
    }

    @Override
    public void pasteSelection(BufferedImage image) {
        int cardCenterX = getWidth() / 2;
        int cardCenterY = getHeight() / 2;

        SelectionTool tool = (SelectionTool) ToolsContext.getInstance().selectPaintTool(PaintToolType.SELECTION);
        tool.createSelection(image, new Point(cardCenterX - image.getWidth() / 2, cardCenterY - image.getHeight() / 2));
    }

    public BufferedImage getScreenshot() {
        BufferedImage screenshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = screenshot.createGraphics();

        g.drawImage(getBackgroundCanvas().getCanvasImage(), 0, 0, null);
        g.drawImage(getCardBackground().getPartsScreenshot(), 0, 0, null);
        g.drawImage(getForegroundCanvas().getCanvasImage(), 0, 0, null);
        g.drawImage(getCardModel().getPartsScreenshot(), 0, 0, null);
        g.dispose();

        return screenshot;
    }

    @Override
    public int getHeight() {
        return stackModel.getHeight();
    }

    @Override
    public int getWidth() {
        return stackModel.getWidth();
    }

    /**
     * Imports an existing button into this card. Note that this differs from {@link #addButton(ButtonPart, CardLayer)} in that a
     * new ID for the part is generated before it is added to the card. This method is typically used to "paste" a
     * copied button from another card onto this card.
     *
     * @param part The button to be imported.
     * @return The newly imported button (identical to the given part, but with a new ID)
     * @throws HtException Thrown if an error occurs importing the part.
     */
    private ButtonPart importButton(ButtonPart part, CardLayer layer) throws HtException {
        ButtonModel model = (ButtonModel) Serializer.copy(part.getPartModel());
        model.defineProperty(PartModel.PROP_ID, new Value(stackModel.getNextButtonId()), true);

        ButtonPart newButton = ButtonPart.fromModel(this, model);
        addButton(newButton, layer);
        return newButton;
    }

    /**
     * Imports an existing field into this card. Note that this differs from {@link #addField(FieldPart, CardLayer)} in that a
     * new ID for the part is generated before it is added to the card. This method is typically used to "paste" a
     * copied field from another card onto this card.
     *
     * @param part The field to be imported.
     * @return The newly imported field (identical to the given part, but with a new ID)
     * @throws HtException Thrown if an error occurs importing the part.
     */
    private FieldPart importField(FieldPart part, CardLayer layer) throws HtException {
        FieldModel model = (FieldModel) Serializer.copy(part.getPartModel());
        model.defineProperty(PartModel.PROP_ID, new Value(stackModel.getNextFieldId()), true);

        FieldPart newField = FieldPart.fromModel(this, model);
        addField(newField, layer);
        return newField;
    }

    /**
     * Adds a field to this card. Assumes that the field has a unique ID.
     * @param field The field to add to this card.
     * @throws PartException Thrown if an error occurs adding the field.
     */
    private void addField(FieldPart field, CardLayer layer) throws PartException {
        if (layer == CardLayer.CARD_PARTS) {
            cardModel.addPart(field);
        } else if (layer == CardLayer.BACKGROUND_PARTS) {
            getCardBackground().addFieldModel((FieldModel) field.getPartModel());
        }

        fields.addPart(field);
        addSwingComponent(field.getComponent(), field.getRect(), layer);
        field.partOpened();
    }

    /**
     * Removes a field from this card. Has no effect if the field does not exist on the card.
     * @param field The field to be removed.
     */
    private void removeField(FieldPart field) {
        cardModel.removePart(field);
        fields.removePart(field);
        removeSwingComponent(field.getComponent());
        field.partClosed();
    }

    /**
     * Adds a button to this card. Assumes the button has a unique ID.
     * @param button The button to be added.
     * @throws PartException Thrown if an error occurs adding this button to the card.
     */
    private void addButton(ButtonPart button, CardLayer layer) throws PartException {
        if (layer == CardLayer.CARD_PARTS) {
            cardModel.addPart(button);
        } else if (layer == CardLayer.BACKGROUND_PARTS) {
            getCardBackground().addButtonModel((ButtonModel) button.getPartModel());
        }

        buttons.addPart(button);
        addSwingComponent(button.getComponent(), button.getRect(), layer);
        button.partOpened();
    }

    /**
     * Removes a button from this card. Has no effect if the button does not exist on the card.
     * @param button The button to be removed.
     */
    private void removeButton(ButtonPart button) {
        cardModel.removePart(button);
        buttons.removePart(button);
        removeSwingComponent(button.getComponent());
        button.partClosed();
    }

    /**
     * Removes a Swing component from this card's JLayeredPane.
     * @param component The component to remove.
     */
    private void removeSwingComponent(Component component) {
        remove(component);
        revalidate(); repaint();
    }

    /**
     * Adds a Swing component to this card's JLayeredPane.
     * @param component The component to add.
     * @param bounds The component's desired location and size.
     */
    private void addSwingComponent(Component component, Rectangle bounds, CardLayer layer) {
        component.setBounds(bounds);
        addToCardLayer(component, layer);
        moveToFront(component);

        revalidate(); repaint();
    }

}
