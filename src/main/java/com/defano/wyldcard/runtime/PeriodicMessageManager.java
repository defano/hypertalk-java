package com.defano.wyldcard.runtime;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.debug.DebugContext;
import com.defano.wyldcard.paint.ToolMode;
import com.defano.wyldcard.parts.card.CardPart;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.parts.stack.StackNavigationObserver;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.context.ToolsContext;
import com.defano.wyldcard.runtime.interpreter.Interpreter;
import com.defano.hypertalk.ast.expressions.ListExp;
import com.defano.hypertalk.ast.model.SystemMessage;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Mechanism for sending periodic messages to parts. Periodic messages include 'idle' and 'mouseWithin'.
 */
public class PeriodicMessageManager implements Runnable, StackNavigationObserver {

    private final static int IDLE_PERIOD_MS = 200;              // Frequency that periodic messages are sent
    private final static int IDLE_DEFERRAL_CYCLES = 50;         // Number of cycles we defer if error is encountered

    private final static PeriodicMessageManager instance = new PeriodicMessageManager();

    private int deferCycles = 0;
    private final ScheduledExecutorService idleTimeExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("periodic-executor-%d").build());
    private final Vector<PartModel> withinParts = new Vector<>();

    private PeriodicMessageManager() {}

    public static PeriodicMessageManager getInstance() {
        return instance;
    }

    public void start() {
        idleTimeExecutor.scheduleAtFixedRate(this, 0, IDLE_PERIOD_MS, TimeUnit.MILLISECONDS);

        // Stop tracking 'within' when card goes away
        WyldCard.getInstance().getActiveStack().addNavigationObserver(this);

        // Stop tracking 'within' when not in browse mode
        ToolsContext.getInstance().getToolModeProvider().subscribe(toolMode -> {
            if (toolMode != ToolMode.BROWSE) {
                withinParts.clear();
            }
        });
    }

    public void addWithin(PartModel part) {
        withinParts.add(part);
    }

    public void removeWithin(PartModel part) {
        withinParts.remove(part);
    }

    @Override
    public void run() {
        try {
            // Send 'idle' message to card if no other scripts are pending
            if (Interpreter.getPendingScriptCount() == 0) {
                HyperCardProperties.getInstance().resetProperties();
                DebugContext.getInstance().resume();
                send(SystemMessage.IDLE, new ExecutionContext().getCurrentCard().getCardModel());
            }

            // Send 'within' message to any parts whose bounds the mouse is within
            send(SystemMessage.MOUSE_WITHIN, withinParts.toArray(new PartModel[] {}));

            if (deferCycles > 0) {
                --deferCycles;
            }

        } catch (Exception e) {
            // Nothing to do
        }
    }

    @Override
    public void onCardClosed(CardPart oldCard) {
        withinParts.clear();
    }

    private void send(SystemMessage message, PartModel... models) {
        for (PartModel model : models) {
            if (ToolsContext.getInstance().getToolMode() == ToolMode.BROWSE && deferCycles < 1) {
                model.receiveMessage(new ExecutionContext(), message.messageName, new ListExp(null), (command, wasTrapped, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        deferCycles = IDLE_DEFERRAL_CYCLES;
                    }
                });
            }
        }
    }
}
