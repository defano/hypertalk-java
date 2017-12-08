package com.defano.hypertalk.ast.containers;

import com.defano.hypertalk.ast.common.Preposition;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.specifiers.MenuItemSpecifier;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

import javax.swing.*;

public class MenuItemContainerExp extends ContainerExp {

    public final MenuItemSpecifier item;

    public MenuItemContainerExp(ParserRuleContext context, MenuItemSpecifier item) {
        super(context);
        this.item = item;
    }

    @Override
    public void putValue(Value value, Preposition preposition) throws HtException {
        putMenuItemValue(value, preposition);
    }

    @Override
    protected Value onEvaluate() throws HtException {
        Value evaluated = getMenuItemValue(item.getSpecifiedMenu(), item.getSpecifiedItemIndex());
        return chunkOf(evaluated, getChunk());
    }

    /**
     * Gets the value of a specific menu item.
     *
     * @param itemIndex The index of the menu item whose value should be returned.
     * @param menu The menu whose menu items should be returned.
     * @return The value of the specified menu item.
     */
    private Value getMenuItemValue(JMenu menu, int itemIndex) throws HtSemanticException {
        if (itemIndex < 0 || itemIndex >= menu.getItemCount()) {
            throw new HtSemanticException("No such menu item " + (itemIndex + 1));
        }

        if (menu.getItem(itemIndex) == null) {
            return new Value("-");
        } else {
            return new Value(menu.getItem(itemIndex).getText());
        }
    }

    /**
     * Puts a Value into a menu relative to a given menu item. See {@link MenuContainerExp#addValueToMenu(Value, JMenu, int)}
     *
     * @param value the value representing new menu items.
     * @param preposition The preposition representing where items should be added relative to the given menu item.
     * @throws HtSemanticException Thrown if an error occurs adding items.
     */
    private void putMenuItemValue(Value value, Preposition preposition) throws HtException {
        JMenu menu = item.getSpecifiedMenu();
        int itemIndex = item.getSpecifiedItemIndex();       // Location of specified item

        if (preposition == Preposition.AFTER) {
            itemIndex++;
        }

        if (itemIndex < 0 || itemIndex > menu.getItemCount()) {
            throw new HtSemanticException("No such menu item.");
        }

        if (preposition == Preposition.INTO) {
            menu.remove(itemIndex);
        }

        MenuContainerExp.addValueToMenu(value, menu, itemIndex);
    }
}
