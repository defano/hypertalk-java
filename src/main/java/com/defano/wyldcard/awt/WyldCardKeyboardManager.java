package com.defano.wyldcard.awt;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.parts.stack.StackModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.google.inject.Singleton;

import javax.swing.FocusManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an AWT-level view of keyboard events; see {@link RoboticTypist} for a utility to produce keystroke events
 * from a String.
 */
@Singleton
public class WyldCardKeyboardManager implements KeyboardManager {

    private boolean isShiftDown;
    private boolean isAltOptionDown;          // Either 'alt' or 'option' (Mac)
    private boolean isCtrlCommandDown;        // Either 'ctrl' or 'command' (Mac
    private Long breakTime;

    private final static Set<KeyListener> observers = new HashSet<>();

    public void start() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            isShiftDown = e.isShiftDown();
            isAltOptionDown = e.isAltDown();
            isCtrlCommandDown = e.isMetaDown() || e.isControlDown();

            if (e.getKeyCode() == KeyEvent.VK_PERIOD && isCtrlCommandDown) {
                breakTime = System.currentTimeMillis();
            }

            fireGlobalKeyListeners(e);

            // When no UI element has keyboard focus, send key press events to the displayed card
            if (FocusManager.getCurrentManager().getFocusOwner() == null) {
                delegateKeyEventToFocusedCard(e);
            }

            return false;
        });
    }

    public void addGlobalKeyListener(KeyListener observer) {
        observers.add(observer);
    }

    public boolean removeGlobalKeyListener(KeyListener observer) {
        return observers.remove(observer);
    }

    public Long getBreakTime() {
        return breakTime;
    }

    public boolean isShiftDown() {
        return isShiftDown;
    }

    public boolean isAltOptionDown() {
        return isAltOptionDown;
    }

    public boolean isCtrlCommandDown() {
        return isCtrlCommandDown;
    }

    public boolean isPeeking(ExecutionContext context) {
        return isAltOptionDown() && isCtrlCommandDown() &&
                !WyldCard.getInstance().getStackManager().getFocusedStack()
                    .getStackModel()
                    .getKnownProperty(context, StackModel.PROP_CANTPEEK)
                    .booleanValue();
    }

    private void delegateKeyEventToFocusedCard(KeyEvent e) {
        switch (e.getID()) {
            case KeyEvent.KEY_PRESSED:
                WyldCard.getInstance().getStackManager().getFocusedStack().getDisplayedCard().keyPressed(e);
                break;

            case KeyEvent.KEY_TYPED:
                WyldCard.getInstance().getStackManager().getFocusedStack().getDisplayedCard().keyTyped(e);
                break;

            case KeyEvent.KEY_RELEASED:
                WyldCard.getInstance().getStackManager().getFocusedStack().getDisplayedCard().keyReleased(e);
                break;
        }
    }

    private static void fireGlobalKeyListeners(KeyEvent e) {
        Set<KeyListener> listeners = new HashSet<>(observers);

        for (KeyListener thisListener : listeners) {
            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    thisListener.keyPressed(e);
                    break;
                case KeyEvent.KEY_RELEASED:
                    thisListener.keyReleased(e);
                    break;
                case KeyEvent.KEY_TYPED:
                    thisListener.keyTyped(e);
                    break;
            }
        }
    }

}
