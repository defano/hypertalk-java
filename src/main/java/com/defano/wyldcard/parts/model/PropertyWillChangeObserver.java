package com.defano.wyldcard.parts.model;

import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.runtime.context.ExecutionContext;

/**
 * An observer to pending changes in a {@link WyldCardPropertiesModel}.
 */
public interface PropertyWillChangeObserver {
    /**
     * Fired to indicate the value of an attribute is about to change. Note that this method is invoked, synchronously,
     * on whichever thread is attempting to modify the attribute. It is the receivers responsibility to invoke
     * {@link javax.swing.SwingUtilities#invokeLater(Runnable)} if this method needs to "touch" UI components.
     *
     * Note that this method will fire even when oldValue is equal to newValue.
     *
     * @param context  The execution context
     * @param property The name of the property (attribute) that will change.
     * @param oldValue The attribute's previous value
     * @param newValue The attribute's new value
     */
    void onPropertyWillChange(ExecutionContext context, String property, Value oldValue, Value newValue);
}
