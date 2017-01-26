package hypercard.parts.model;

import hypertalk.ast.common.Value;
import hypertalk.exception.HtSemanticException;

public interface ComputedSetter {

    /**
     * Computes and sets the value of a property, either by modifying the provided value in some way, or by
     * converting the set operation into constituent property writes (e.g., converting a rectangle into top, left
     * height and width coordinates).
     *
     * @param model        The {@link PropertiesModel} whose property is being set.
     * @param propertyName The name of the property which is to be set.
     * @param value        The requested value to be set; this method is responsible for transforming this value as
     *                     required.
     * @throws HtSemanticException Thrown to indicate the property cannot accept the given/computed value.
     */
    void setComputedValue(PropertiesModel model, String propertyName, Value value) throws HtSemanticException;
}
