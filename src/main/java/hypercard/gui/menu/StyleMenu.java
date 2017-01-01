package hypercard.gui.menu;

import hypercard.context.ToolsContext;
import hypercard.paint.observers.Provider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StyleMenu extends JMenu {

    public StyleMenu() {
        super("Style");

        MenuItemBuilder.ofCheckType()
                .named("Plain")
                .withAction(e -> ToolsContext.getInstance().setFontStyle(Font.PLAIN))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font) e).getStyle() == Font.PLAIN))
                .fontStyle(Font.PLAIN)
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("Bold")
                .withAction(e -> ToolsContext.getInstance().setFontStyle(Font.BOLD))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font) e).getStyle() == Font.BOLD))
                .fontStyle(Font.BOLD)
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("Italic")
                .withAction(e -> ToolsContext.getInstance().setFontStyle(Font.ITALIC))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font) e).getStyle() == Font.ITALIC))
                .fontStyle(Font.ITALIC)
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Underline")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Outline")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Shadow")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Condense")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Extend")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Group")
                .disabled()
                .build(this);

        addSeparator();

        MenuItemBuilder.ofCheckType()
                .named("9")
                .withAction(e -> ToolsContext.getInstance().setFontSize(9))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font)e).getSize() == 9))
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("10")
                .withAction(e -> ToolsContext.getInstance().setFontSize(10))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font)e).getSize() == 10))
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("12")
                .withAction(e -> ToolsContext.getInstance().setFontSize(12))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font)e).getSize() == 12))
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("14")
                .withAction(e -> ToolsContext.getInstance().setFontSize(14))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font)e).getSize() == 14))
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("18")
                .withAction(e -> ToolsContext.getInstance().setFontSize(18))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font)e).getSize() == 18))
                .build(this);

        MenuItemBuilder.ofCheckType()
                .named("24")
                .withAction(e -> ToolsContext.getInstance().setFontSize(24))
                .withCheckmarkProvider(new Provider<>(ToolsContext.getInstance().getFontProvider(), e -> ((Font)e).getSize() == 24))
                .build(this);

        this.addSeparator();

        MenuItemBuilder.ofDefaultType()
                .named("Other...")
                .disabled()
                .build(this);
    }
}
