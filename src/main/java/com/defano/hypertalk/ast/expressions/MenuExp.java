package com.defano.hypertalk.ast.expressions;

import com.defano.hypertalk.ast.common.MenuItemSpecifier;
import com.defano.hypertalk.ast.common.MenuSpecifier;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.containers.ContainerMenu;
import com.defano.hypertalk.exception.HtSemanticException;

public class MenuExp extends Expression {

    private final MenuSpecifier menuSpecifier;
    private final MenuItemSpecifier menuItemSpecifier;

    public MenuExp(MenuSpecifier menuContainer) {
        this.menuSpecifier = menuContainer;
        this.menuItemSpecifier = null;
    }

    public MenuExp(MenuItemSpecifier menuItemSpecifier) {
        this.menuItemSpecifier = menuItemSpecifier;
        this.menuSpecifier = null;
    }

    @Override
    public Value evaluate() throws HtSemanticException {
        if (menuSpecifier != null) {
            return new ContainerMenu(menuSpecifier).getValue();
        } else {
            return new ContainerMenu(menuItemSpecifier).getValue();
        }
    }
}
