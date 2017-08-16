/*
 * ButtonPart
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * ButtonPart.java
 *
 * @author matt.defano@motorola.com
 * <p>
 * Implements the user interface for a HyperCard button part by extending the
 * Swing push button class.
 */

package com.defano.hypercard.parts.button;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.context.PartToolContext;
import com.defano.hypercard.context.ToolMode;
import com.defano.hypercard.gui.window.ButtonPropertyEditor;
import com.defano.hypercard.gui.window.WindowBuilder;
import com.defano.hypercard.parts.card.CardLayerPart;
import com.defano.hypercard.parts.card.CardPart;
import com.defano.hypercard.parts.PartMover;
import com.defano.hypercard.parts.PartResizer;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypercard.parts.model.PropertyChangeObserver;
import com.defano.hypercard.context.ToolsContext;
import com.defano.hypercard.runtime.Interpreter;
import com.defano.hypercard.runtime.WindowManager;
import com.defano.hypertalk.ast.common.*;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.WeakReference;

/**
 * The controller object associated with a button on a card.
 *
 * See {@link ButtonModel} for the model associated with this controller.
 * See {@link StyleableButton} for the view associated with this controller.
 */
public class ButtonPart extends StyleableButton implements CardLayerPart, MouseListener, PropertyChangeObserver {

    private static final int DEFAULT_WIDTH = 160;
    private static final int DEFAULT_HEIGHT = 40;

    private final Owner owner;
    private final PartMover mover;
    private ButtonModel partModel;
    private final WeakReference<CardPart> parent;

    private ButtonPart(ButtonStyle style, CardPart parent, Owner owner) {
        super(style);

        this.owner = owner;
        this.parent = new WeakReference<>(parent);
        this.mover = new PartMover(this, parent);
    }

    /**
     * Creates a new button on the given card with default size and position.
     *
     * @param parent The card that this button will belong to.
     * @return The new button.
     */
    public static ButtonPart newButton(CardPart parent, Owner owner) {
        ButtonPart newButton = fromGeometry(parent, new Rectangle(parent.getWidth() / 2 - (DEFAULT_WIDTH / 2), parent.getHeight() / 2 - (DEFAULT_HEIGHT / 2), DEFAULT_WIDTH, DEFAULT_HEIGHT), owner);

        // When a new button is created, make the button tool active and select the newly created button
        ToolsContext.getInstance().setToolMode(ToolMode.BUTTON);
        PartToolContext.getInstance().setSelectedPart(newButton);

        return newButton;
    }

    /**
     * Creates a new button on the given card with the provided geometry.
     *
     * @param parent The card that this button will belong to.
     * @param geometry The bounding rectangle of the new button.
     * @return The new button.
     */
    public static ButtonPart fromGeometry(CardPart parent, Rectangle geometry, Owner owner) {
        ButtonPart button = new ButtonPart(ButtonStyle.DEFAULT, parent, owner);
        button.initProperties(geometry);
        return button;
    }

    /**
     * Creates a new button view from an existing button data model.
     *
     * @param parent The card that this button will belong to.
     * @param partModel The data model representing this button.
     * @return The new button.
     * @throws Exception Thrown if an error occurs instantiating the button.
     */
    public static ButtonPart fromModel(CardPart parent, ButtonModel partModel, Owner owner) throws HtException {
        ButtonStyle style = ButtonStyle.fromName(partModel.getKnownProperty(ButtonModel.PROP_STYLE).stringValue());
        ButtonPart button = new ButtonPart(style, parent, owner);

        button.partModel = partModel;
        button.partModel.addPropertyChangedObserver(button);

        return button;
    }

    @Override
    public void partOpened() {
        super.partOpened();
        partModel.addPropertyChangedObserver(this);
    }

    @Override
    public void partClosed() {
        super.partClosed();
        partModel.removePropertyChangedObserver(this);
    }

    @Override
    public void editProperties() {
        WindowBuilder.make(new ButtonPropertyEditor())
                .asModal()
                .withTitle(getName())
                .withModel(partModel)
                .withLocationCenteredOver(WindowManager.getStackWindow().getWindowPanel())
                .build();
    }

    @Override
    public CardPart getCard() {
        return parent.get();
    }

    @Override
    public CardLayerPart getPart() {
        return this;
    }

    @Override
    public void move() {
        mover.startMoving();
    }

    @Override
    public void resize(int fromQuadrant) {
        new PartResizer(this, parent.get(), fromQuadrant);
    }

    @Override
    public void delete() {
        parent.get().removePart(getPartModel());
    }

    @Override
    public Tool getEditTool() {
        return Tool.BUTTON;
    }

    @Override
    public void replaceSwingComponent(Component oldButtonComponent, Component newButtonComponent) {
        parent.get().replaceSwingComponent(this, oldButtonComponent, newButtonComponent);
    }

    @Override
    public PartType getType() {
        return PartType.BUTTON;
    }

    @Override
    public JComponent getComponent() {
        return this.getButtonComponent();
    }

    @Override
    public PartModel getPartModel() {
        return partModel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            getPartModel().sendMessage(SystemMessage.MOUSE_DOWN.messageName);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            getPartModel().sendMessage(SystemMessage.MOUSE_UP.messageName);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        getPartModel().sendMessage(SystemMessage.MOUSE_ENTER.messageName);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        getPartModel().sendMessage(SystemMessage.MOUSE_LEAVE.messageName);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {
            getPartModel().sendMessage(SystemMessage.MOUSE_DOUBLE_CLICK.messageName);
        }
    }

    @Override
    public void onPropertyChanged(String property, Value oldValue, Value newValue) {
        switch (property) {
            case ButtonModel.PROP_STYLE:
                setStyle(ButtonStyle.fromName(newValue.stringValue()));
                break;
            case ButtonModel.PROP_SCRIPT:
                try {
                    Interpreter.compile(newValue.stringValue());
                } catch (HtException e) {
                    HyperCard.getInstance().showErrorDialog(new HtSemanticException("Didn't understand that.", e));
                }
                break;
            case ButtonModel.PROP_TOP:
            case ButtonModel.PROP_LEFT:
            case ButtonModel.PROP_WIDTH:
            case ButtonModel.PROP_HEIGHT:
                getButtonComponent().setBounds(partModel.getRect());
                getButtonComponent().validate();
                getButtonComponent().repaint();
                break;
            case ButtonModel.PROP_ENABLED:
                getButtonComponent().setEnabled(newValue.booleanValue());
                break;
            case ButtonModel.PROP_VISIBLE:
                setVisibleOnCard(newValue.booleanValue());
                break;
            case ButtonModel.PROP_ZORDER:
                getCard().onDisplayOrderChanged();
                break;
        }
    }

    private void initProperties(Rectangle geometry) {
        int id = parent.get().getStackModel().getNextButtonId();
        partModel = ButtonModel.newButtonModel(id, geometry, owner);
    }
}
