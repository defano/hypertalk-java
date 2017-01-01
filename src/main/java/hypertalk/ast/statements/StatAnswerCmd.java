/**
 * StatAnswerCmd.java
 * @author matt.defano@gmail.com
 * 
 * Implementation of the "answer" statement
 */

package hypertalk.ast.statements;

import hypercard.context.GlobalContext;
import hypercard.runtime.WindowManager;
import hypertalk.ast.common.Value;
import hypertalk.ast.expressions.Expression;
import hypertalk.exception.HtSemanticException;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;

public class StatAnswerCmd extends Statement {

    public final Expression message;
    public final Expression ch1;
    public final Expression ch2;
    public final Expression ch3;
    
    public StatAnswerCmd (Expression message, Expression ch1, Expression ch2, Expression ch3) {
        this.message = message;
        this.ch1 = ch1;
        this.ch2 = ch2;
        this.ch3 = ch3;
    }
    
    public StatAnswerCmd (Expression message, Expression ch1, Expression ch2) {
        this.message = message;
        this.ch1 = ch1;
        this.ch2 = ch2;
        this.ch3 = null;
    }
    
    public StatAnswerCmd (Expression message, Expression ch1) {
        this.message = message;
        this.ch1 = ch1;
        this.ch2 = null;
        this.ch3 = null;
    }

    public StatAnswerCmd (Expression message) {
        this.message = message;
        this.ch1 = null;
        this.ch2 = null;
        this.ch3 = null;
    }
    
    public void execute () throws HtSemanticException {
        if (ch1 != null && ch2 != null && ch3 != null)
            answer(message.evaluate(), ch1.evaluate(), ch2.evaluate(), ch3.evaluate());
        else if (ch1 != null && ch2 != null)
            answer(message.evaluate(), ch1.evaluate(), ch2.evaluate());
        else if (ch1 != null)
            answer(message.evaluate(), ch1.evaluate());
        else
            answer(message.evaluate());
    }
    
    private void answer (Value msg, Value choice1, Value choice2, Value choice3) {

        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            Component parent = WindowManager.getStackWindow().getWindowPanel();
            Object[] choices = null;

            if (choice1 != null && choice2 != null && choice3 != null) {
                choices = new Object[]{choice1, choice2, choice3};
            }
            else if (choice1 != null && choice2 != null) {
                choices = new Object[]{choice1, choice2};
            }
            else if (choice1 != null) {
                choices = new Object[]{choice1};
            }

            int choice = JOptionPane.showOptionDialog(parent, msg, "Answer",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);

            switch (choice) {
                case 0:     GlobalContext.getContext().setIt(choice1); break;
                case 1:     GlobalContext.getContext().setIt(choice2); break;
                case 2:     GlobalContext.getContext().setIt(choice3); break;
                default:     GlobalContext.getContext().setIt(new Value()); break;
            }

            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    private void answer (Value msg, Value choice1, Value choice2) {
        answer(msg, choice1, choice2, null);
    }
    
    private void answer (Value msg, Value choice1) {
        answer(msg, choice1, null, null);
    }
    
    private void answer (Value msg) {
        answer(msg, new Value("OK"), null, null);
    }
}
