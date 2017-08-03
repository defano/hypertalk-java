/*
 * MenuItemBuilder
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.gui.menu;

import com.defano.jmonet.model.ImmutableProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class MenuItemBuilder {

    private final JMenuItem item;
    private ImmutableProvider<Boolean> checkmarkProvider;
    private ImmutableProvider<Boolean> disabledProvider;

    private MenuItemBuilder(JMenuItem item) {
        this.item = item;
    }

    public static MenuItemBuilder ofCheckType () {
        return new MenuItemBuilder(new JCheckBoxMenuItem());
    }

    public static MenuItemBuilder ofRadioType () {
        return new MenuItemBuilder(new JRadioButtonMenuItem());
    }

    public static MenuItemBuilder ofDefaultType () {
        return new MenuItemBuilder(new JMenuItem());
    }

    public static MenuItemBuilder ofAction(Action action) {
        return new MenuItemBuilder(new JMenuItem(action));
    }

    public static MenuItemBuilder ofHeirarchicalType() {
        return new MenuItemBuilder(new JMenu());
    }

    public MenuItemBuilder withAction (ActionListener action) {
        this.item.addActionListener(action);
        return this;
    }

    public MenuItemBuilder fontStyle (int fontStyle) {
        Font defaultFont = this.item.getFont();
        Font customFont = new Font(defaultFont.getFamily(), fontStyle, defaultFont.getSize());
        this.item.setFont(customFont);
        return this;

    }

    public MenuItemBuilder fontFamily (String fontFamily) {
        Font defaultFont = this.item.getFont();
        Font customFont = new Font(fontFamily, defaultFont.getStyle(), defaultFont.getSize());
        this.item.setFont(customFont);
        return this;
    }

    public MenuItemBuilder withCheckmarkProvider(ImmutableProvider<Boolean> checkmarkProvider) {
        this.checkmarkProvider = checkmarkProvider;
        return this;
    }

    public MenuItemBuilder disabled () {
        this.item.setEnabled(false);
        return this;
    }

    public MenuItemBuilder withDisabledProvider(ImmutableProvider<Boolean> disabledProvider) {
        this.disabledProvider = disabledProvider;
        return this;
    }

    public MenuItemBuilder named (String name) {
        this.item.setName(name);
        this.item.setText(name);
        return this;
    }

    public MenuItemBuilder withShiftShortcut (char shortcut) {
        this.item.setMnemonic(shortcut);
        this.item.setAccelerator(KeyStroke.getKeyStroke(shortcut, KeyEvent.SHIFT_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        return this;
    }

    public MenuItemBuilder withShortcut (char shortcut) {
        if (shortcut == '+') {
            this.item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else {
            this.item.setAccelerator(KeyStroke.getKeyStroke(shortcut, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        this.item.setMnemonic(shortcut);
        return this;
    }

    public MenuItemBuilder withIcon(Icon icon) {
        this.item.setIcon(icon);
        return this;
    }

    public MenuItemBuilder withActionListener(ActionListener actionListener) {
        this.item.addActionListener(actionListener);
        return this;
    }

    public MenuItemBuilder withActionCommand(String actionCommand) {
        this.item.setActionCommand(actionCommand);
        return this;
    }

    public JMenuItem build (JMenuItem intoMenu) {

        if (checkmarkProvider != null) {
            checkmarkProvider.addObserver((o, newValue) -> item.setSelected((boolean) newValue));
            item.setSelected(checkmarkProvider.get());
        }

        if (disabledProvider != null) {
            disabledProvider.addObserver((o, arg) -> item.setEnabled(!(boolean) arg));
            item.setEnabled(!disabledProvider.get());
        }

        intoMenu.add(item);
        return item;
    }
}
