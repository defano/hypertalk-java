package com.defano.wyldcard.menubar.main;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.menubar.HyperCardMenu;
import com.defano.wyldcard.menubar.MenuItemBuilder;
import com.defano.wyldcard.paint.ToolMode;
import com.defano.wyldcard.parts.button.ButtonPart;
import com.defano.wyldcard.parts.field.FieldPart;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.context.PartToolContext;
import com.defano.wyldcard.runtime.context.ToolsContext;
import com.defano.wyldcard.window.WindowBuilder;
import com.defano.wyldcard.window.WindowManager;
import com.defano.wyldcard.window.layouts.BackgroundPropertyEditor;
import com.defano.wyldcard.window.layouts.CardPropertyEditor;
import com.defano.wyldcard.window.layouts.StackPropertyEditor;

import java.util.Optional;

/**
 * The HyperCard Objects menu.
 */
public class ObjectsMenu extends HyperCardMenu {

    public static ObjectsMenu instance = new ObjectsMenu();

    private ObjectsMenu() {
        super("Objects");

        // Show this menu only when an object tool is active
        // Show this menu only when a paint tool is active
        ToolsContext.getInstance().getToolModeProvider().subscribe(toolMode -> ObjectsMenu.this.setVisible(ToolMode.PAINT != toolMode));

        MenuItemBuilder.ofDefaultType()
                .named("Button Info...")
                .withEnabledProvider(PartToolContext.getInstance().getSelectedPartProvider().map(toolEditablePart -> toolEditablePart.isPresent() && toolEditablePart.get() instanceof ButtonPart))
                .withAction(a -> PartToolContext.getInstance().getSelectedPart().getPartModel().editProperties(new ExecutionContext()))
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Field Info...")
                .withEnabledProvider(PartToolContext.getInstance().getSelectedPartProvider().map(toolEditablePart -> toolEditablePart.isPresent() && toolEditablePart.get() instanceof FieldPart))
                .withAction(a -> PartToolContext.getInstance().getSelectedPart().getPartModel().editProperties(new ExecutionContext()))
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Card Info...")
                .withAction(e -> new WindowBuilder<>(new CardPropertyEditor())
                        .withModel(WyldCard.getInstance().getFocusedCard())
                        .asModal()
                        .withTitle("Card Properties")
                        .withLocationCenteredOver(WindowManager.getInstance().getFocusedStackWindow().getWindowPanel())
                        .build())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Background Info...")
                .withAction(e -> new WindowBuilder<>(new BackgroundPropertyEditor())
                        .withModel(WyldCard.getInstance().getFocusedCard())
                        .withTitle("Background Properties")
                        .asModal()
                        .withLocationCenteredOver(WindowManager.getInstance().getFocusedStackWindow().getWindowPanel())
                        .build())
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Stack Info...")
                .withAction(e -> new WindowBuilder<>(new StackPropertyEditor())
                        .withModel(WyldCard.getInstance().getFocusedStack().getStackModel())
                        .withTitle("Stack Properties")
                        .asModal()
                        .withLocationCenteredOver(WindowManager.getInstance().getFocusedStackWindow().getWindowPanel())
                        .build())
                .build(this);

        this.addSeparator();

        MenuItemBuilder.ofDefaultType()
                .named("Bring Closer")
                .withEnabledProvider(PartToolContext.getInstance().getSelectedPartProvider().map(Optional::isPresent))
                .withAction(a -> PartToolContext.getInstance().bringSelectedPartCloser())
                .withShortcut('=')
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("Send Further")
                .withEnabledProvider(PartToolContext.getInstance().getSelectedPartProvider().map(Optional::isPresent))
                .withAction(a -> PartToolContext.getInstance().sendSelectedPartFurther())
                .withShortcut('-')
                .build(this);

        this.addSeparator();

        MenuItemBuilder.ofDefaultType()
                .named("New Button")
                .withAction(e -> WyldCard.getInstance().getFocusedCard().newButton(new ExecutionContext()))
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("New Field")
                .withAction(e -> WyldCard.getInstance().getFocusedCard().newField(new ExecutionContext()))
                .build(this);

        MenuItemBuilder.ofDefaultType()
                .named("New Background")
                .withAction(e -> WyldCard.getInstance().getFocusedStack().newBackground(new ExecutionContext()))
                .build(this);
    }

    public void reset() {
        instance = new ObjectsMenu();
    }
}
