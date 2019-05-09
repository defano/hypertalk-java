package com.defano.wyldcard.menubar.main;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.menubar.WyldCardMenu;
import com.defano.wyldcard.menubar.MenuItemBuilder;
import com.defano.hypertalk.ast.model.Value;

import java.awt.*;

/**
 * The HyperCard Font menu.
 */
public class FontMenu extends WyldCardMenu {

    public static FontMenu instance = new FontMenu();

    private FontMenu() {
        super("Font");

        for (String thisFamily : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            MenuItemBuilder.ofCheckType()
                    .named(thisFamily)
                    .withDoMenuAction(e -> WyldCard.getInstance().getFontManager().setSelectedFontFamily(thisFamily))
                    .withCheckmarkProvider(WyldCard.getInstance().getFontManager().getFocusedFontFamilyProvider().map(f -> f.contains(new Value(thisFamily))))
                    .fontFamily(thisFamily)
                    .build(this);
        }
    }

    public void reset() {
        instance = new FontMenu();
    }
}
