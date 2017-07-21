package com.defano.hypercard.gui.window;

import com.defano.hypercard.gui.HyperCardWindow;
import com.defano.hypercard.parts.CardLayer;
import com.defano.hypercard.parts.CardPart;
import com.defano.hypertalk.ast.common.PartType;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;

public class BackgroundPropertyEditor extends HyperCardWindow {
    private CardPart cardPart;

    private JPanel propertiesPanel;
    private JTextField backgroundName;
    private JLabel fieldCountLabel;
    private JLabel buttonCountLabel;
    private JCheckBox cantDeleteBkgndCheckBox;
    private JButton cancelButton;
    private JButton saveButton;
    private JLabel cardCountLabel;
    private JLabel backgroundIdLabel;
    private JButton editScriptButton;

    public BackgroundPropertyEditor() {
        saveButton.addActionListener(e -> {
            updateProperties();
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    @Override
    public JPanel getWindowPanel() {
        return propertiesPanel;
    }

    @Override
    public void bindModel(Object data) {
        cardPart = (CardPart) data;

        int backgroundId = cardPart.getCardModel().getBackgroundId();
        backgroundIdLabel.setText("Background ID: " + backgroundId);
        backgroundName.setText(cardPart.getCardBackground().getName());
        cantDeleteBkgndCheckBox.setSelected(cardPart.getCardBackground().isCantDelete());

        long cardCount = cardPart.getStackModel().getCardCountInBackground(backgroundId);
        long fieldCount = cardPart.getPartCount(PartType.FIELD, CardLayer.BACKGROUND_PARTS);
        long buttonCount = cardPart.getPartCount(PartType.BUTTON, CardLayer.BACKGROUND_PARTS);

        cardCountLabel.setText("Background shared by " + cardCount + " cards.");
        buttonCountLabel.setText("Contains " + buttonCount + " background buttons.");
        fieldCountLabel.setText("Contains " + fieldCount + " background fields.");
    }

    private void updateProperties() {
        cardPart.getCardBackground().setName(backgroundName.getText());
        cardPart.getCardBackground().setCantDelete(cantDeleteBkgndCheckBox.isSelected());
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
        propertiesPanel.setLayout(new GridLayoutManager(11, 3, new Insets(10, 10, 10, 10), -1, -1));
        panel1.add(propertiesPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Background Name:");
        propertiesPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        backgroundName = new JTextField();
        propertiesPanel.add(backgroundName, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        backgroundIdLabel = new JLabel();
        backgroundIdLabel.setText("Background ID:");
        propertiesPanel.add(backgroundIdLabel, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fieldCountLabel = new JLabel();
        fieldCountLabel.setText("Contains 2 background fields.");
        propertiesPanel.add(fieldCountLabel, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCountLabel = new JLabel();
        buttonCountLabel.setText("Contains 2 background buttons.");
        propertiesPanel.add(buttonCountLabel, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cantDeleteBkgndCheckBox = new JCheckBox();
        cantDeleteBkgndCheckBox.setText("Can't Delete Background");
        propertiesPanel.add(cantDeleteBkgndCheckBox, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        propertiesPanel.add(panel2, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setMargin(new Insets(0, 0, 0, 0));
        saveButton.setText("Save");
        panel2.add(saveButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.setText("Cancel");
        panel2.add(cancelButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("");
        propertiesPanel.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("");
        propertiesPanel.add(label3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("");
        propertiesPanel.add(label4, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cardCountLabel = new JLabel();
        cardCountLabel.setText("Background shared by 9 cards.");
        propertiesPanel.add(cardCountLabel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("");
        propertiesPanel.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editScriptButton = new JButton();
        editScriptButton.setEnabled(false);
        editScriptButton.setText("Edit Script...");
        editScriptButton.setToolTipText("Not implemented");
        propertiesPanel.add(editScriptButton, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        propertiesPanel.add(spacer2, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }
}
