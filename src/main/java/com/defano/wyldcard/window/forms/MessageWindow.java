package com.defano.wyldcard.window.forms;

import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.utils.Range;
import com.defano.wyldcard.aspect.RunOnDispatch;
import com.defano.wyldcard.parts.model.PropertiesModel;
import com.defano.wyldcard.parts.model.PropertyChangeObserver;
import com.defano.wyldcard.parts.msgbox.MsgBoxModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.util.ThreadUtils;
import com.defano.wyldcard.window.HyperCardFrame;
import com.defano.wyldcard.window.MessageBoxTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class MessageWindow extends HyperCardFrame implements PropertyChangeObserver {

    private final ExecutionContext staticContext;

    private MsgBoxModel partModel;
    private MessageBoxTextField messageBox;
    private JPanel messageWindow;

    public MessageWindow() {

        staticContext = new ExecutionContext();
        staticContext.pushStackFrame();

        // Update selection
        messageBox.addCaretListener(e -> getPartModel().updateSelectionContext(new ExecutionContext(), Range.ofMarkAndDot(e.getDot(), e.getMark()), getPartModel(), true));
        messageBox.setStaticContext(staticContext);

        SwingUtilities.invokeLater(() -> {
            partModel = new MsgBoxModel();
            partModel.addPropertyChangedObserver(MessageWindow.this);
        });
    }

    @Override
    public JPanel getWindowPanel() {
        return messageWindow;
    }

    @Override
    public void bindModel(Object data) {
        // Nothing to do
    }

    public JTextComponent getTextComponent() {
        return messageBox;
    }

    public MsgBoxModel getPartModel() {
        return partModel;
    }

    @Override
    @RunOnDispatch
    public void onPropertyChanged(ExecutionContext context, PropertiesModel model, String property, Value oldValue, Value newValue) {
        switch (property) {
            case MsgBoxModel.PROP_CONTENTS:
                getTextComponent().setText(newValue.stringValue());
                break;
        }
    }

    public void setMsgBoxText(String text) {
        ThreadUtils.invokeAndWaitAsNeeded(() -> partModel.setKnownProperty(new ExecutionContext(), MsgBoxModel.PROP_CONTENTS, new Value(text)));
    }

    public String getMsgBoxText() {
        AtomicReference<String> text = new AtomicReference<>();
        ThreadUtils.invokeAndWaitAsNeeded(() -> text.set(getTextComponent().getText()));
        return text.get();
    }

    /**
     * Show the message window, populate the field with a find command, and position the caret inside the query string.
     */
    @RunOnDispatch
    public void doFind() {
        setVisible(true);
        setMsgBoxText("find \"\"");
        getTextComponent().setCaretPosition(6);
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
        messageWindow = new JPanel();
        messageWindow.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        messageWindow.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        messageBox = new MessageBoxTextField();
        messageWindow.add(messageBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(600, 25), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return messageWindow;
    }
}
