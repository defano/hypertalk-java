package com.defano.wyldcard.parts;

import com.defano.hypertalk.ast.model.PartType;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.hypertalk.exception.NoSuchPropertyException;
import com.defano.hypertalk.exception.PropertyPermissionException;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.runtime.context.ToolsContext;

import java.awt.*;

/**
 * An interface defining behavior common to all card-embedded parts (buttons and fields).
 */
public interface Part {

    /**
     * Gets the type of this part.
     * @return The type of the part.
     */
    PartType getType();

    /**
     * Gets the data model associated with this part.
     * @return The part model.
     */
    PartModel getPartModel();

    /**
     * Invoked when the part is opened (added) to a card, background or stack.
     */
    void partOpened();

    /**
     * Invoked when the part is closed (removed) from a card, background or stack.
     */
    void partClosed();

    /**
     * Sets the property of the part.
     * @param property The name of the property to set
     * @param value The value to which it should be set
     * @throws NoSuchPropertyException Thrown if no such property exists on this part
     * @throws PropertyPermissionException Thrown when attempting to write a read-only property (like ID)
     * @throws HtSemanticException Thrown if value provided is invalid for this property
     */
    default void setProperty(String property, Value value) throws HtSemanticException {
        getPartModel().setProperty(property, value);
    }

    /**
     * Gets the value of a property on this part.
     * @param property The name of the property to get
     * @return The value of the property
     * @throws NoSuchPropertyException Thrown if no such property exists on the part.
     */
    default Value getProperty(String property) throws NoSuchPropertyException {
        return getPartModel().getProperty(property);
    }

    /**
     * Gets the bounds of this part.
     * @return The bounds of the part.
     */
    default Rectangle getRect() {
        return getPartModel().getRect();
    }

    /**
     * Gets the ID of this part.
     * @return The part id.
     */
    default int getId() {
        return getPartModel().getKnownProperty(PartModel.PROP_ID).integerValue();
    }

    /**
     * Gets the name of this part.
     * @return The part name.
     */
    default String getName() {
        return getPartModel().getKnownProperty(PartModel.PROP_NAME).stringValue();
    }

    /**
     * Determines if a part tool (button or field) is currently active.
     * @return True if a part tool is active; false otherwise.
     */
    default boolean isPartToolActive() {
        return ToolsContext.getInstance().getToolMode().isPartTool();
    }
}
