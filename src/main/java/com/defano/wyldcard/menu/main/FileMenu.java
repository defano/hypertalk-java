package com.defano.wyldcard.menu.main;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.menu.HyperCardMenu;
import com.defano.wyldcard.menu.MenuItemBuilder;
import com.defano.wyldcard.paint.ArtVandelay;
import com.defano.wyldcard.paint.ToolMode;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.context.ToolsContext;
import com.defano.wyldcard.runtime.print.PrintCardAction;
import com.defano.wyldcard.runtime.print.PrintStackAction;
import com.defano.wyldcard.window.WindowManager;

import java.util.Optional;

/**
 * The HyperCard File menu.
 */
public class FileMenu extends HyperCardMenu {

    public static FileMenu instance = new FileMenu();

    private FileMenu() {
        super("File");

        MenuItemBuilder.ofDefaultType()
                .named("New Stack...")
                .withAction(e -> WyldCard.getInstance().newStack(new ExecutionContext()))
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Open Stack...")
                .withAction(e -> WyldCard.getInstance().open(new ExecutionContext()))
                .withShortcut('O')
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Close Stack")
                .withShortcut('W')
                .withAction(e -> WyldCard.getInstance().quit())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Save Stack")
                .withAction(e -> WyldCard.getInstance().saveActiveStack(new ExecutionContext()))
                .withEnabledProvider(WyldCard.getInstance().getSavedStackFileProvider().map(Optional::isPresent))
                .withShortcut('S')
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Save Stack As...")
                .withAction(e -> WyldCard.getInstance().saveActiveStackAs(new ExecutionContext()))
                .withShiftShortcut('S')
                .build(this);

        this.addSeparator();

        MenuItemBuilder.ofDefaultType()
                .named("Import Paint...")
                .withAction(e -> ArtVandelay.importPaint())
                .withDisabledProvider(ToolsContext.getInstance().getToolModeProvider().map(m -> m != ToolMode.PAINT))
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Export Paint...")
                .withAction(e -> ArtVandelay.exportPaint())
                .withDisabledProvider(ToolsContext.getInstance().getToolModeProvider().map(m -> m != ToolMode.PAINT))
                .build(this);

        this.addSeparator();

        MenuItemBuilder.ofDefaultType()
                .named("Compact Stack")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Protect Stack...")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Delete Stack...")
                .disabled()
                .build(this);

        this.addSeparator();

        MenuItemBuilder.ofDefaultType()
                .named("Page Setup...")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Print Field...")
                .disabled()
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Print Card...")
                .withShortcut('P')
                .withAction(new PrintCardAction())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Print Stack...")
                .withShiftShortcut('P')
                .withAction(new PrintStackAction())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Print Report...")
                .disabled()
                .build(this);

        if (!WindowManager.getInstance().isMacOs()) {

            this.addSeparator();

            MenuItemBuilder.ofDefaultType()
                    .named("Quit HyperCard")
                    .withAction(e -> WyldCard.getInstance().quit())
                    .withShortcut('Q')
                    .build(this);
        }
    }

    public void reset() {
        instance = new FileMenu();
    }
}
