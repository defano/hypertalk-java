/*
 * FieldPart
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

/**
 * FieldPart.java
 *
 * @author matt.defano@motorola.com
 * <p>
 * Implements a HyperCard field part user interface by extending the Swing
 * scroll panel.
 */

package com.defano.hypercard.parts;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.context.PartToolContext;
import com.defano.hypercard.context.ToolMode;
import com.defano.hypercard.context.ToolsContext;
import com.defano.hypercard.gui.window.FieldPropertyEditor;
import com.defano.hypercard.gui.window.WindowBuilder;
import com.defano.hypercard.parts.fields.AbstractFieldView;
import com.defano.hypercard.parts.fields.FieldStyle;
import com.defano.hypercard.parts.model.*;
import com.defano.hypercard.runtime.WindowManager;
import com.defano.hypercard.runtime.Interpreter;
import com.defano.hypertalk.ast.common.PartType;
import com.defano.hypertalk.ast.common.Script;
import com.defano.hypertalk.ast.common.Tool;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.exception.HtSemanticException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FieldPart extends AbstractFieldView implements Part, MouseListener, PropertyChangeObserver, KeyListener {

    private static final int DEFAULT_WIDTH = 250;
    private static final int DEFAULT_HEIGHT = 100;

    private Script script;
    private FieldModel partModel;
    private CardPart parent;
    private PartMover mover;

    private FieldPart(FieldStyle style, CardPart parent) {
        super(style);

        this.mover = new PartMover(this, parent);
        this.parent = parent;
        this.script = new Script();
    }

    public static FieldPart newField(CardPart parent) {
        FieldPart newField = fromGeometry(parent, new Rectangle(parent.getWidth() / 2 - (DEFAULT_WIDTH / 2), parent.getHeight() / 2 - (DEFAULT_HEIGHT / 2), DEFAULT_WIDTH, DEFAULT_HEIGHT));

        // When a new field is created, make the field tool active and select the newly created part
        ToolsContext.getInstance().setToolMode(ToolMode.FIELD);
        PartToolContext.getInstance().setSelectedPart(newField);

        return newField;
    }

    public static FieldPart fromGeometry(CardPart parent, Rectangle geometry) {
        FieldPart field = new FieldPart(FieldStyle.OPAQUE, parent);

        field.initProperties(geometry);

        return field;
    }

    public static FieldPart fromModel(CardPart parent, FieldModel model) throws Exception {
        FieldPart field = new FieldPart(FieldStyle.fromName(model.getKnownProperty(FieldModel.PROP_STYLE).stringValue()), parent);

        field.script = Interpreter.compile(model.getKnownProperty(FieldModel.PROP_SCRIPT).stringValue());
        field.partModel = model;
        field.partModel.addPropertyChangedObserver(field);

        return field;
    }

    private void initProperties(Rectangle geometry) {
        int id = parent.nextFieldId();

        partModel = FieldModel.newFieldModel(id, geometry);
        partModel.addPropertyChangedObserver(this);
    }

    @Override
    public void editProperties() {
        WindowBuilder.make(new FieldPropertyEditor())
                .withTitle("Properties of field " + getName())
                .withModel(partModel)
                .withLocationCenteredOver(WindowManager.getStackWindow().getWindowPanel())
                .resizeable(false)
                .build();
    }

    @Override
    public void move() {
        mover.startMoving();
    }

    @Override
    public void resize(int fromQuadrant) {
        new PartResizer(this, parent, fromQuadrant);
    }

    @Override
    public void delete() {
        parent.removeField(this);
    }

    @Override
    public Tool getEditTool() {
        return Tool.FIELD;
    }

    @Override
    public void invalidateSwingComponent(Component oldComponent, Component newComponent) {
        parent.invalidateSwingComponent(this, oldComponent, newComponent);
    }

    @Override
    public PartType getType() {
        return PartType.FIELD;
    }

    @Override
    public JComponent getComponent() {
        return this.getFieldView();
    }

    @Override
    public Part getPart() {
        return this;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public AbstractPartModel getPartModel() {
        return partModel;
    }

    @Override
    public void setValue(Value value) {
        try {
            partModel.setProperty(FieldModel.PROP_TEXT, value);
        } catch (Exception e) {
            throw new RuntimeException("Field's text property cannot be set");
        }
    }

    @Override
    public String getValueProperty() {
        return com.defano.hypercard.parts.model.ButtonModel.PROP_CONTENTS;
    }

    @Override
    public boolean isPartToolActive() {
        return ToolsContext.getInstance().getToolMode() == ToolMode.FIELD;
    }

    @Override
    public Value getValue() {
        return partModel.getKnownProperty(FieldModel.PROP_TEXT);
    }

    @Override
    public CardPart getCard() {
        return parent;
    }

    private void compile() throws HtSemanticException {

        try {
            script = Interpreter.compile(partModel.getKnownProperty(FieldModel.PROP_SCRIPT).toString());
        } catch (Exception e) {
            throw new HtSemanticException("Didn't understand that.");
        }
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
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if (getTextComponent().hasFocus()) {
            sendMessage("keyDown");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);

        if (getTextComponent().hasFocus()) {
            sendMessage("keyUp");
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {
            sendMessage("mouseDoubleClick");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
    }

    @Override
    public void onPropertyChanged(String property, Value oldValue, Value newValue) {
        switch (property) {
            case FieldModel.PROP_STYLE:
                setFieldStyle(FieldStyle.fromName(newValue.stringValue()));
                break;
            case FieldModel.PROP_SCRIPT:
                try {
                    compile();
                } catch (HtSemanticException e) {
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
                getComponent().setVisible(newValue.booleanValue());
                getComponent().validate();
                getComponent().repaint();
                break;
            case AbstractPartModel.PROP_ZORDER:
                getCard().onZOrderChanged();
                break;
        }
    }
}
