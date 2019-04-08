package com.defano.wyldcard.window.layouts;

import com.defano.hypertalk.ast.model.Script;
import com.defano.hypertalk.exception.HtException;
import com.defano.wyldcard.editor.HyperTalkTextEditor;
import com.defano.wyldcard.editor.SyntaxParserDelegate;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.compiler.CompilationUnit;
import com.defano.wyldcard.runtime.compiler.Compiler;
import com.defano.wyldcard.runtime.compiler.MessageEvaluationObserver;
import com.defano.wyldcard.window.WyldCardDialog;
import com.defano.wyldcard.window.WyldCardWindow;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import io.reactivex.functions.Consumer;
import org.fife.ui.rsyntaxtextarea.parser.Parser;

import javax.swing.*;
import java.awt.*;

public class ExpressionEvaluator extends WyldCardDialog<Object> implements SyntaxParserDelegate {

    private final static ExpressionEvaluator instance = new ExpressionEvaluator();

    private JButton evaluateButton;
    private JTextArea resultField;
    private JPanel windowPanel;
    private JLabel contextField;
    private JPanel editorArea;

    private ExecutionContext context = new ExecutionContext();
    private HyperTalkTextEditor editor;

    private ExpressionEvaluator() {
        setContext(ExecutionContext.unboundInstance());

        editor = new HyperTalkTextEditor(this);
        editorArea.setLayout(new BorderLayout());
        editorArea.add(editor);

        evaluateButton.addActionListener(a -> {
            Compiler.asyncStaticContextEvaluate(context, editor.getScriptField().getText(), new MessageEvaluationObserver() {
                @Override
                public void onMessageEvaluated(String result) {
                    SwingUtilities.invokeLater(() -> resultField.setText(result));
                }

                @Override
                public void onEvaluationError(HtException exception) {
                    SwingUtilities.invokeLater(() -> resultField.setText("Error: " + exception.getMessage()));
                }
            });
        });

        getWindowVisibleProvider().subscribe(isVisible -> {
            if (!isVisible) {
                setContext(ExecutionContext.unboundInstance());
            }
        });
    }

    public static ExpressionEvaluator getInstance() {
        return instance;
    }

    @Override
    public JComponent getWindowPanel() {
        return windowPanel;
    }

    @Override
    public void bindModel(Object data) {
        // Nothing to do
    }

    public ExecutionContext getContext() {
        return context;
    }

    public void setContext(ExecutionContext context) {
        this.context = context;
        contextField.setText(context.toString());
    }

    @Override
    public JButton getDefaultButton() {
        return evaluateButton;
    }

    @Override
    public CompilationUnit getParseCompilationUnit() {
        return CompilationUnit.SCRIPTLET;
    }

    @Override
    public void onRequestParse(Parser syntaxParser) {
        editor.getScriptField().forceReparsing(syntaxParser);
    }

    @Override
    public void onCompileStarted() {
        // Nothing to do
    }

    @Override
    public void onCompileCompleted(Script compiledScript, String resultMessage) {
        // Nothing to do
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        windowPanel = new JPanel();
        windowPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(10, 10, 10, 10), -1, -1));
        contextField = new JLabel();
        contextField.setEnabled(false);
        contextField.setText("Context");
        windowPanel.add(contextField, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        evaluateButton = new JButton();
        evaluateButton.setText("Evaluate");
        windowPanel.add(evaluateButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editorArea = new JPanel();
        editorArea.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        windowPanel.add(editorArea, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, 200), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        windowPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        windowPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(-1, 60), null, 0, false));
        resultField = new JTextArea();
        resultField.setEditable(false);
        resultField.setEnabled(true);
        scrollPane1.setViewportView(resultField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return windowPanel;
    }

}
