package com.defano.wyldcard.parts;

import com.defano.hypertalk.ast.model.PartType;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.hypertalk.exception.HtNoSuchPropertyException;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.parts.bkgnd.BackgroundModel;
import com.defano.wyldcard.parts.card.CardLayerPartModel;
import com.defano.wyldcard.parts.card.CardModel;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.parts.stack.StackModel;
import com.defano.wyldcard.parts.stack.StackPart;
import com.defano.wyldcard.runtime.ExecutionContext;
import com.defano.wyldcard.window.layouts.StackWindow;

import java.awt.*;

/**
 * An interface defining behavior common to all scriptable WyldCard UI elements, including buttons, fields, cards,
 * and stacks.
 *
 * Note that in WyldCard, a background is not considered a part (even though its scriptable), because the background has
 * no standalone view object (it's view--canvas, buttons and fields) are merged with the card elements into a single
 * {@link com.defano.wyldcard.parts.card.CardPart}.
 */
public interface Part<T extends PartModel> {

    /**
     * Gets the type of this part.
     * @return The type of the part.
     */
    PartType getType();

    /**
     * Gets the data model associated with this part.
     * @return The part model.
     */
    T getPartModel();

    /**
     * Invoked exactly once when the part is opened (displayed on-screen). This method should be used to register UI
     * listeners or otherwise initially the view component.
     *
     * @param context The execution context.
     */
    void partOpened(ExecutionContext context);

    /**
     * Invoked exactly once when the part is closed (removed from on-screen display). This method should be used to
     * de-register listeners or otherwise cleanup the view component.
     *
     * @param context The execution context.
     */
    void partClosed(ExecutionContext context);

    /**
     * Sets the property of the part.
     *
     * @param context The execution context.
     * @param property The name of the property to set
     * @param value The value to which it should be set
     * @throws HtNoSuchPropertyException Thrown if no such property exists on this part
     * @throws HtSemanticException Thrown if value provided is invalid for this property
     */
    default void setProperty(ExecutionContext context, String property, Value value) throws HtException {
        getPartModel().trySet(context, property, value);
    }

    /**
     * Gets the value of a property on this part.
     *
     * @param context The execution context.
     * @param property The name of the property to get
     * @return The value of the property
     * @throws HtNoSuchPropertyException Thrown if no such property exists on the part.
     */
    default Value getProperty(ExecutionContext context, String property) throws HtException {
        return getPartModel().tryGet(context, property);
    }

    /**
     * Gets the bounds of this part.
     * @return The bounds of the part.
     * @param context The execution context.
     */
    default Rectangle getRect(ExecutionContext context) {
        return getPartModel().getRect(context);
    }

    /**
     * Gets the ID of this part.
     * @return The part id.
     * @param context The execution context.
     */
    default int getId(ExecutionContext context) {
        return getPartModel().get(context, PartModel.PROP_ID).integerValue();
    }

    /**
     * Gets the name of this part.
     * @return The part name.
     * @param context The execution context.
     */
    default String getName(ExecutionContext context) {
        return getPartModel().get(context, PartModel.PROP_NAME).toString();
    }

    /**
     * Determines if a part tool (button or field) is currently active.
     * @return True if a part tool is active; false otherwise.
     */
    default boolean isPartToolActive() {
        return WyldCard.getInstance().getToolsManager().getToolMode().isPartTool();
    }

    /**
     * Gets the stack that this part is a component of.
     *
     * @return The stack that this part is a component of or null if the component is not part of a stack, or has not
     * yet been bound to a specific stack.
     */
    default StackPart getOwningStack() {
        return WyldCard.getInstance().getStackManager().getOpenStack(getOwningStackModel());
    }

    /**
     * Gets the stack model that this part is a component of.
     *
     * @return The model of the stack that this part is a component of, or null if the component is not part of a stack,
     * or has not yet been bound to a specific stack.
     */
    default StackModel getOwningStackModel() {
        switch (getType()) {
            case FIELD:
            case BUTTON:
                CardLayerPartModel partModel = (CardLayerPartModel) getPartModel();
                PartModel parentModel = partModel.getParentPartModel();

                if (parentModel instanceof BackgroundModel) {
                    return ((BackgroundModel) parentModel).getStackModel();
                } else {
                    return ((CardModel) parentModel).getStackModel();
                }

            case CARD:
                return ((CardModel) getPartModel()).getStackModel();
            case BACKGROUND:
                return ((BackgroundModel) getPartModel()).getStackModel();
            case STACK:
                return (StackModel) getPartModel();

            default:
                return null;
        }
    }

    /**
     * Gets the stack window bound to the stack that this part is a component of.
     *
     * @return The stack window in which this part appears, or null, if this component is not part of a stack.
     */
    default StackWindow getOwningStackWindow() {
        StackModel boundModel = getOwningStackModel();
        if (boundModel != null) {
            return WyldCard.getInstance().getWindowManager().findWindowForStack(boundModel);
        } else {
            return null;
        }
    }
}
