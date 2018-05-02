package com.defano.wyldcard.menubar.script;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.debug.DebugContext;
import com.defano.wyldcard.menubar.HyperCardMenu;
import com.defano.wyldcard.menubar.MenuItemBuilder;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.window.WindowManager;
import com.defano.wyldcard.window.layouts.ScriptEditor;

public class FileMenu extends HyperCardMenu {

    public FileMenu(ScriptEditor editor) {
        super("File");

        MenuItemBuilder.ofDefaultType()
                .named("Close Script")
                .withShortcut('W')
                .withAction(e -> editor.close())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Save Script")
                .withDisabledProvider(DebugContext.getInstance().getIsDebuggingProvider())
                .withAction(e -> editor.save())
                .withShortcut('S')
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Revert to Saved")
                .withDisabledProvider(DebugContext.getInstance().getIsDebuggingProvider())
                .withAction(e -> editor.revertToSaved())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Print Script...")
                .withShortcut('P')
                .disabled()
                .build(this);

        if (!WindowManager.getInstance().isMacOsTheme()) {

            addSeparator();

            MenuItemBuilder.ofDefaultType()
                    .named("Quit HyperCard")
                    .withAction(e -> WyldCard.getInstance().closeAllStacks(new ExecutionContext()))
                    .withShortcut('Q')
                    .build(this);
        }

    }
}
