package hypercard.parts.fields;

import hypercard.context.ToolsContext;
import hypercard.parts.ToolEditablePart;
import hypercard.parts.fields.styles.OpaqueField;
import hypercard.parts.fields.styles.TransparentField;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractStylableField implements ToolEditablePart {

    private FieldComponent fieldComponent;
    private boolean isBeingEdited;

    public abstract void move();

    public abstract void resize(int fromQuadrant);

    public abstract void invalidateSwingComponent(Component oldComponent, Component newComponent);

    public AbstractStylableField(FieldStyle style) {
        fieldComponent = getComponentForStyle(style);
    }

    @Override
    public boolean isBeingEdited() {
        return isBeingEdited;
    }

    @Override
    public void setBeingEdited(boolean beingEdited) {
        fieldComponent.setEditable(!beingEdited);
        isBeingEdited = beingEdited;
    }

    public void setFieldStyle(FieldStyle style) {
        Component oldComponent = getFieldComponent();
        fieldComponent = getComponentForStyle(style);

        partClosed();
        invalidateSwingComponent(oldComponent, (JComponent) fieldComponent);
        partOpened();
    }

    private FieldComponent getComponentForStyle(FieldStyle style) {
        switch (style) {
            case TRANSPARENT:
                return new TransparentField(this);
            case OPAQUE:
                return new OpaqueField(this);

            default:
                throw new IllegalArgumentException("No such field style: " + style);
        }
    }

    public JComponent getFieldComponent() {
        return (JComponent) fieldComponent;
    }

    public String getText() {
        return fieldComponent.getText();
    }

    @Override
    public void partOpened() {
        getPartModel().addPropertyChangedObserver(fieldComponent);
        ToolsContext.getInstance().getToolModeProvider().addObserverAndUpdate((o, arg) -> onToolModeChanged());

        fieldComponent.partOpened();
    }

    @Override
    public void partClosed() {
        getPartModel().removePropertyChangedObserver(fieldComponent);
    }

}
