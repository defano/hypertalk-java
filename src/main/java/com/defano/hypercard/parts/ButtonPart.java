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

package com.defano.hypercard.parts;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.context.PartToolContext;
import com.defano.hypercard.context.ToolMode;
import com.defano.hypercard.gui.window.ButtonPropertyEditor;
import com.defano.hypercard.gui.window.WindowBuilder;
import com.defano.hypercard.parts.buttons.StyleableButton;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypercard.parts.model.PropertyChangeObserver;
import com.defano.hypercard.context.ToolsContext;
import com.defano.hypercard.parts.buttons.ButtonStyle;
import com.defano.hypercard.parts.model.ButtonModel;
import com.defano.hypercard.runtime.Interpreter;
import com.defano.hypercard.runtime.WindowManager;
import com.defano.hypertalk.ast.common.PartType;
import com.defano.hypertalk.ast.common.Script;
import com.defano.hypertalk.ast.common.Tool;
import com.defano.hypertalk.ast.common.Value;
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
public class ButtonPart extends StyleableButton implements MouseListener, PropertyChangeObserver {

    private static final int DEFAULT_WIDTH = 160;
    private static final int DEFAULT_HEIGHT = 40;

    private final PartMover mover;
    private Script script;
    private ButtonModel partModel;
    private WeakReference<CardPart> parent;

    private ButtonPart(ButtonStyle style, CardPart parent) {
        super(style);

        this.parent = new WeakReference<>(parent);
        this.script = new Script();
        this.mover = new PartMover(this, parent);
    }

    /**
     * Creates a new button on the given card with default size and position.
     *
     * @param parent The card that this button will belong to.
     * @return The new button.
     */
    public static ButtonPart newButton(CardPart parent) {
        ButtonPart newButton = fromGeometry(parent, new Rectangle(parent.getWidth() / 2 - (DEFAULT_WIDTH / 2), parent.getHeight() / 2 - (DEFAULT_HEIGHT / 2), DEFAULT_WIDTH, DEFAULT_HEIGHT));

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
    public static ButtonPart fromGeometry(CardPart parent, Rectangle geometry) {
        ButtonPart button = new ButtonPart(ButtonStyle.DEFAULT, parent);
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
    public static ButtonPart fromModel(CardPart parent, ButtonModel partModel) throws HtException {
        ButtonStyle style = ButtonStyle.fromName(partModel.getKnownProperty(ButtonModel.PROP_STYLE).stringValue());
        ButtonPart button = new ButtonPart(style, parent);

        button.partModel = partModel;
        button.partModel.addPropertyChangedObserver(button);
        button.script = Interpreter.compile(partModel.getKnownProperty(ButtonModel.PROP_SCRIPT).stringValue());

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
    public Part getPart() {
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
        parent.get().removePart(this);
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
    public String getValueProperty() {
        return ButtonModel.PROP_CONTENTS;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            sendMessage("mouseDown");
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            sendMessage("mouseUp");
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        sendMessage("mouseEnter");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        sendMessage("mouseLeave");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {
            sendMessage("mouseDoubleClick");
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
                    compile();
                } catch (HtSemanticException e) {
                    HyperCard.getInstance().showErrorDialog(e);
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
            case PartModel.PROP_ZORDER:
                getCard().onZOrderChanged();
                break;
        }
    }

    /**
     * Compiles the script associated with this button and stores the AST in the script property.
     * @throws HtSemanticException Thrown if a syntax or semantic error occurs.
     */
    private void compile() throws HtSemanticException {
        try {
            String scriptText = partModel.getKnownProperty(ButtonModel.PROP_SCRIPT).toString();
            script = Interpreter.compile(scriptText);
        } catch (Exception e) {
            throw new HtSemanticException("Didn't understand that.");
        }
    }

    private void initProperties(Rectangle geometry) {
        int id = parent.get().getStackModel().getNextButtonId();
        partModel = ButtonModel.newButtonModel(id, geometry);
    }
}
