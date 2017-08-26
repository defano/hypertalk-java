package com.defano.hypercard.gui.window;

import com.defano.hypercard.gui.HyperCardDialog;
import com.defano.hypercard.parts.stack.StackModel;
import com.defano.hypercard.runtime.WindowManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;

public class StackPropertyEditor extends HyperCardDialog {
    private StackModel model;

    private JPanel propertiesPanel;
    private JTextField stackName;
    private JLabel cardCountLabel;
    private JLabel backgroundCountLabel;
    private JButton saveButton;
    private JButton editScriptButton;
    private JButton resizeButton;

    public StackPropertyEditor() {
        saveButton.addActionListener(e -> {
            updateProperties();
            dispose();
        });

        editScriptButton.addActionListener(e -> {
            dispose();
            WindowBuilder.make(new ScriptEditor())
                    .withTitle("Script of stack " + model.getKnownProperty(StackModel.PROP_NAME).stringValue())
                    .withModel(model)
                    .resizeable(true)
                    .withLocationCenteredOver(WindowManager.getStackWindow().getWindowPanel())
                    .build();
        });
        resizeButton.addActionListener(e -> model.setDimension(StackSizeEditor.editStackSize(this.model.getDimension(), getWindowPanel())));
    }

    @Override
    public JComponent getWindowPanel() {
        return propertiesPanel;
    }

    @Override
    public void bindModel(Object data) {
        model = (StackModel) data;

        stackName.setText(model.getStackName());
        cardCountLabel.setText("Stack contains " + model.getCardCount() + " cards.");
        backgroundCountLabel.setText("Stack contains " + model.getBackgroundCount() + " backgrounds.");
    }

    private void updateProperties() {
        model.setStackName(stackName.getText());
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new GridLayoutManager(8, 3, new Insets(10, 10, 10, 10), -1, -1));
        panel1.add(propertiesPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Stack name:");
        propertiesPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stackName = new JTextField();
        propertiesPanel.add(stackName, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        cardCountLabel = new JLabel();
        cardCountLabel.setText("Stack contains 2 cards.");
        propertiesPanel.add(cardCountLabel, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        backgroundCountLabel = new JLabel();
        backgroundCountLabel.setText("Stack contains 1 background.");
        propertiesPanel.add(backgroundCountLabel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("");
        propertiesPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("");
        propertiesPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("");
        propertiesPanel.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editScriptButton = new JButton();
        editScriptButton.setEnabled(true);
        editScriptButton.setText("Edit Script...");
        editScriptButton.setToolTipText("Not implemented");
        propertiesPanel.add(editScriptButton, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resizeButton = new JButton();
        resizeButton.setEnabled(true);
        resizeButton.setText("Resize...");
        propertiesPanel.add(resizeButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        propertiesPanel.add(saveButton, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        propertiesPanel.add(spacer1, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }
}
