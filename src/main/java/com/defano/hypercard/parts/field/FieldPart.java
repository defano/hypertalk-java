/*
 * FieldPart
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.parts.field;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.context.*;
import com.defano.hypercard.gui.window.FieldPropertyEditor;
import com.defano.hypercard.gui.window.WindowBuilder;
import com.defano.hypercard.parts.card.CardLayerPart;
import com.defano.hypercard.parts.card.CardPart;
import com.defano.hypercard.parts.editor.PartMover;
import com.defano.hypercard.parts.card.CardLayerPartModel;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypercard.parts.model.PropertyChangeObserver;
import com.defano.hypercard.runtime.Interpreter;
import com.defano.hypercard.runtime.WindowManager;
import com.defano.hypertalk.ast.common.*;
import com.defano.hypertalk.exception.HtException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.WeakReference;

/**
 * The controller object associated with a field on the card.
 *
 * See {@link FieldModel} for the model object associated with this controller.
 * See {@link StyleableField} for the view object associated with this view.
 */
public class FieldPart extends StyleableField implements CardLayerPart, PropertyChangeObserver {

    private static final int DEFAULT_WIDTH = 250;
    private static final int DEFAULT_HEIGHT = 100;

    private final Owner owner;
    private FieldModel partModel;
    private final WeakReference<CardPart> parent;
    private final PartMover mover;

    private FieldPart(FieldStyle style, CardPart parent, Owner owner) {
        super(style);

        this.owner = owner;
        this.mover = new PartMover(this, parent);
        this.parent = new WeakReference<>(parent);
    }

    /**
     * Creates a new field with default attributes on the given card.
     * @param parent The card in which the field should be generated.
     * @return The newly created FieldPart
     */
    public static FieldPart newField(CardPart parent, Owner owner) {
        FieldPart newField = fromGeometry(parent, new Rectangle(parent.getWidth() / 2 - (DEFAULT_WIDTH / 2), parent.getHeight() / 2 - (DEFAULT_HEIGHT / 2), DEFAULT_WIDTH, DEFAULT_HEIGHT), owner);

        // When a new field is created, make the field tool active and select the newly created part
        ToolsContext.getInstance().setToolMode(ToolMode.FIELD);
        PartToolContext.getInstance().setSelectedPart(newField);

        return newField;
    }

    /**
     * Creates a new field of the given size and location on the provided card.
     * @param parent The card in which to create the field.
     * @param geometry The size and location of the field.
     * @return The newly created field.
     */
    public static FieldPart fromGeometry(CardPart parent, Rectangle geometry, Owner owner) {
        FieldPart field = new FieldPart(FieldStyle.TRANSPARENT, parent, owner);

        field.initProperties(geometry);

        return field;
    }

    /**
     * Creates a new field from an existing field data model.
     *
     * @param parent The card in which the field should be created.
     * @param model The data model of the field to be created.
     * @return The newly created field.
     * @throws HtException Thrown if an error occurs instantiating this field on the card.
     */
    public static FieldPart fromModel(CardPart parent, FieldModel model, Owner owner) throws HtException {
        FieldPart field = new FieldPart(FieldStyle.fromName(model.getKnownProperty(FieldModel.PROP_STYLE).stringValue()), parent, owner);

        field.partModel = model;
        field.partModel.addPropertyChangedObserver(field);

        return field;
    }

    /** {@inheritDoc} */
    @Override
    public void editProperties() {
        WindowBuilder.make(new FieldPropertyEditor())
                .asModal()
                .withTitle(getName())
                .withModel(partModel)
                .withLocationCenteredOver(WindowManager.getStackWindow().getWindowPanel())
                .resizeable(false)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public void partClosed() {
        super.partClosed();
    }

    /** {@inheritDoc} */
    @Override
    public void partOpened() {
        super.partOpened();
    }

    /** {@inheritDoc} */
    @Override
    public Tool getEditTool() {
        return Tool.FIELD;
    }

    /** {@inheritDoc} */
    @Override
    public void replaceSwingComponent(Component oldComponent, Component newComponent) {
        parent.get().replaceSwingComponent(this, oldComponent, newComponent);
    }

    /** {@inheritDoc} */
    @Override
    public PartType getType() {
        return PartType.FIELD;
    }

    /** {@inheritDoc} */
    @Override
    public JComponent getComponent() {
        return this.getFieldComponent();
    }

    /** {@inheritDoc} */
    @Override
    public CardLayerPart getPart() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PartModel getPartModel() {
        return partModel;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPartToolActive() {
        return ToolsContext.getInstance().getToolMode() == ToolMode.FIELD;
    }

    /** {@inheritDoc} */
    @Override
    public CardPart getCard() {
        return parent.get();
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        // Update the clickText property
        setClickText(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            getPartModel().sendMessage(SystemMessage.MOUSE_DOWN.messageName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            getPartModel().sendMessage(SystemMessage.MOUSE_UP.messageName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        getPartModel().sendMessage(SystemMessage.MOUSE_ENTER.messageName);
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        getPartModel().sendMessage(SystemMessage.MOUSE_LEAVE.messageName);
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        if (e.getClickCount() == 2) {
            getPartModel().sendMessage(SystemMessage.MOUSE_DOUBLE_CLICK.messageName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);

        if (getTextPane().hasFocus()) {
            getPartModel().sendAndConsume(SystemMessage.KEY_DOWN.messageName, new ExpressionList(String.valueOf(e.getKeyChar())), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if (getTextPane().hasFocus()) {
            SystemMessage command = SystemMessage.fromKeyEvent(e, true);
            if (command != null) {
                getPartModel().sendAndConsume(command.messageName, new ExpressionList(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPropertyChanged(String property, Value oldValue, Value newValue) {
        switch (property) {
            case FieldModel.PROP_STYLE:
                setStyle(FieldStyle.fromName(newValue.stringValue()));
                break;
            case FieldModel.PROP_SCRIPT:
                try {
                    Interpreter.compile(newValue.stringValue());
                } catch (HtException e) {
                    HyperCard.getInstance().showErrorDialog(e);
                }
                break;
            case FieldModel.PROP_TOP:
            case FieldModel.PROP_LEFT:
            case FieldModel.PROP_WIDTH:
            case FieldModel.PROP_HEIGHT:
                getComponent().setBounds(partModel.getRect());
                getComponent().validate();
                getComponent().repaint();
                break;
            case FieldModel.PROP_VISIBLE:
                setVisibleOnCard(newValue.booleanValue());
                break;
            case CardLayerPartModel.PROP_ZORDER:
                getCard().onDisplayOrderChanged();
                break;
        }
    }

    private void setClickText(MouseEvent evt) {
        try {
            int clickIndex = getTextPane().viewToModel(evt.getPoint());
            int startWordIndex = Utilities.getWordStart(getTextPane(), clickIndex);
            int endWordIndex = Utilities.getWordEnd(getTextPane(), clickIndex);

            String clickText = getTextPane().getStyledDocument().getText(startWordIndex, endWordIndex - startWordIndex);
            ExecutionContext.getContext().getGlobalProperties().defineProperty(HyperCardProperties.PROP_CLICKTEXT, new Value(clickText), true);

        } catch (BadLocationException e) {
            // Nothing to do
        }
    }

    private void initProperties(Rectangle geometry) {
        int id = parent.get().getStackModel().getNextFieldId();

        partModel = FieldModel.newFieldModel(id, geometry, owner);
        partModel.setFont(ToolsContext.getInstance().getFontProvider().get());
        partModel.addPropertyChangedObserver(this);
    }

}
