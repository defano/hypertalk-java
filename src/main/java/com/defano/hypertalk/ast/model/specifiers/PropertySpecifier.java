package com.defano.hypertalk.ast.model.specifiers;

import com.defano.wyldcard.parts.model.PartModel;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.expressions.containers.MenuItemExp;
import com.defano.hypertalk.ast.expressions.containers.PartExp;
import com.defano.hypertalk.ast.model.Chunk;
import com.defano.hypertalk.ast.model.Adjective;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.runtime.context.ExecutionContext;

public class PropertySpecifier {

    private final String property;
    private final Expression partExp;
    private final Adjective adjective;

    public PropertySpecifier(String globalProperty) {
        this(globalProperty, null);
    }

    public PropertySpecifier(String property, Expression part) {
        this(Adjective.DEFAULT, property, part);
    }

    public PropertySpecifier(Adjective adjective, String property, Expression part) {
        this.property = property;
        this.partExp = part;
        this.adjective = adjective;
    }

    public boolean isGlobalPropertySpecifier(ExecutionContext context) {
        return partExp == null && getMenuItem(context) == null;
    }

    public boolean isMenuItemPropertySpecifier(ExecutionContext context) {
        return getMenuItem(context) != null;
    }

    public boolean isChunkPropertySpecifier(ExecutionContext context) {
        return getChunk(context) != null;
    }

    public Adjective getAdjective() {
        return adjective;
    }

    public String getProperty() {
        return property;
    }

    /**
     * Returns the name of the specified property with the specified adjective applied (where applicable). For example,
     * applying {@link Adjective#SHORT} to the property 'name' yields 'short name'
     * <p>
     * Certain properties (like name and id) support length adjectives (like 'long', 'short' or 'abbrev') when
     * applied to certain objects. This method attempts to compute an applied property name given a property
     * and adjective.
     * <p>
     * Note that objects may override the default adjective. For example, when requesting 'the name of' a button
     * or field, 'the abbrev name' is actually returned.
     *
     * @return The adjective-applied name of the specified property.
     * @param context The execution context.
     */
    public String getAdjectiveAppliedPropertyName(ExecutionContext context) {
        PartModel model = getPartModel(context);

        // Apply adjective only to properties that support it
        if (model != null && model.isAdjectiveSupportedProperty(property)) {
            if (adjective == Adjective.DEFAULT) {
                return model.getDefaultAdjectiveForProperty(property).apply(property);
            } else {
                return adjective.apply(property);
            }
        }

        // Ignore adjective on properties that don't support it (i.e., 'the long width' is the same as 'the width')
        else {
            return property;
        }
    }

    public Chunk getChunk(ExecutionContext context) {
        if (partExp == null) {
            return null;
        } else {
            PartExp factor = partExp.factor(context, PartExp.class);
            return factor == null ? null : factor.getChunk();
        }
    }

    /**
     * Gets the model of the part specified, or null if this specifier either refers to a non-existent part or
     * doesn't specify a part type at all.
     *
     * @return The model of the part specified by this object, or null
     * @param context The execution context.
     */
    public PartModel getPartModel(ExecutionContext context) {
        if (partExp == null) {
            return null;
        } else {
            return partExp.partFactor(context, PartModel.class);
        }
    }

    public PartExp getPartExp(ExecutionContext context) throws HtException {
        if (partExp == null) {
            return null;
        } else {
            return partExp.factor(context, PartExp.class, new HtSemanticException("Expected a part here."));
        }
    }

    public MenuItemSpecifier getMenuItem(ExecutionContext context) {
        if (partExp == null) {
            return null;
        } else {
            MenuItemExp factor = partExp.factor(context, MenuItemExp.class);
            return factor == null ? null : factor.item;
        }
    }
}
