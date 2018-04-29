package com.defano.wyldcard.editor;

import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSyntaxException;
import com.defano.hypertalk.utils.Range;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.aspect.RunOnDispatch;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.interpreter.Interpreter;
import com.defano.wyldcard.runtime.interpreter.MessageEvaluationObserver;
import com.defano.wyldcard.util.SquigglePainter;
import com.defano.wyldcard.util.ThreadUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MessageBoxTextField extends JTextField implements MessageEvaluationObserver {

    private final static Highlighter.HighlightPainter ERROR_HIGHLIGHTER = new SquigglePainter(Color.RED);

    private final ArrayList<String> messageStack = new ArrayList<>();
    private int messageStackIndex = -1;

    private ExecutionContext staticContext = new ExecutionContext();
    private MessageEvaluationObserver messageEvaluationObserver = this;

    public MessageBoxTextField() {

        // Handle syntax checking and message execution key typed events
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    messageStack.add(getText());
                    messageStackIndex = messageStack.size();

                    evaluate();
                } else {
                    SwingUtilities.invokeLater(() -> checkSyntax());
                }
            }
        });

        // Handle message stack key press events
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (messageStackIndex > 0) {
                            setText(messageStack.get(--messageStackIndex));
                        }
                        break;

                    case KeyEvent.VK_DOWN:
                        if (messageStackIndex < messageStack.size() - 1) {
                            setText(messageStack.get(++messageStackIndex));
                        }
                        break;
                }

                SwingUtilities.invokeLater(() -> checkSyntax());
            }
        });

    }

    public void setStaticContext(ExecutionContext staticContext) {
        this.staticContext = staticContext;
    }

    @RunOnDispatch
    private void checkSyntax() {
        try {
            getHighlighter().removeAllHighlights();
            Interpreter.blockingCompileScriptlet(getText());
        } catch (HtException e) {
            squiggleHighlight(e);
        }
    }

    @RunOnDispatch
    private void squiggleHighlight(HtException e) {
        int squiggleStart = 0;
        int squiggleEnd = getText().length();

        if (e instanceof HtSyntaxException) {
            Range offendingRange = e.getBreadcrumb().getCharRange();
            if (offendingRange != null) {
                squiggleStart = offendingRange.start;
                squiggleEnd = offendingRange.end;
            }
        }

        try {
            getHighlighter().addHighlight(squiggleStart, squiggleEnd, ERROR_HIGHLIGHTER);
        } catch (BadLocationException e1) {
            // Impossible!
        }
    }

    public void evaluate() {
        if (!getText().trim().isEmpty()) {
            String messageText = getText();
            Interpreter.asyncEvaluateMessage(staticContext, messageText, messageEvaluationObserver);
        }
    }

    @Override
    public void onMessageEvaluated(String result) {
        // Replace the message box text with the result of evaluating the expression (ignore if user entered statement)
        if (result != null) {
            ThreadUtils.invokeAndWaitAsNeeded(() -> setToolTipText(result));
        }
    }

    @Override
    public void onEvaluationError(HtException exception) {
        WyldCard.getInstance().showErrorDialog(exception);
    }
}
