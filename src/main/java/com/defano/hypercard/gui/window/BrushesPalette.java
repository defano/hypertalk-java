/*
 * BrushesPalette
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard.gui.window;

import com.defano.hypercard.context.ToolsContext;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.defano.hypercard.gui.HyperCardWindow;
import com.defano.jmonet.tools.brushes.BasicBrush;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class BrushesPalette extends HyperCardWindow implements Observer {

    private JPanel brushesPanel;

    private JButton square16;
    private JButton square12;
    private JButton square8;
    private JButton square4;
    private JButton round16;
    private JButton round12;
    private JButton round8;
    private JButton round4;
    private JButton line16;
    private JButton line12;
    private JButton line8;
    private JButton line4;

    private JButton[] allButtons;

    public BrushesPalette() {
        allButtons = new JButton[]{square16, square12, square8, square4, round16, round12, round8, round4, line16, line12, line8, line4};

        square16.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.SQUARE_16X16));
        square12.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.SQUARE_12X12));
        square8.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.SQUARE_8X8));
        square4.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.SQUARE_4X4));
        round16.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.ROUND_16X16));
        round12.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.ROUND_12X12));
        round8.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.ROUND_8X8));
        round4.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.ROUND_4X4));
        line16.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.LINE_16));
        line12.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.LINE_12));
        line8.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.LINE_8));
        line4.addActionListener(a -> ToolsContext.getInstance().setSelectedBrush(BasicBrush.LINE_4));

        ToolsContext.getInstance().getSelectedBrushProvider().addObserverAndUpdate(this);
    }

    @Override
    public JPanel getWindowPanel() {
        return brushesPanel;
    }

    @Override
    public void bindModel(Object data) {
        // Nothing to do
    }

    @Override
    public void update(Observable o, Object arg) {
        BasicBrush newBrush = (BasicBrush) arg;

        for (JButton thisButton : allButtons) {
            thisButton.setEnabled(true);
        }

        getButtonForBrush(newBrush).setEnabled(false);
    }

    private JButton getButtonForBrush(BasicBrush newBrush) {
        switch (newBrush) {
            case SQUARE_16X16:
                return square16;
            case SQUARE_12X12:
                return square12;
            case SQUARE_8X8:
                return square8;
            case SQUARE_4X4:
                return square4;
            case ROUND_16X16:
                return round16;
            case ROUND_12X12:
                return round12;
            case ROUND_8X8:
                return round8;
            case ROUND_4X4:
                return round4;
            case LINE_16:
                return line16;
            case LINE_12:
                return line12;
            case LINE_8:
                return line8;
            case LINE_4:
                return line4;
        }

        return square16;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
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
        brushesPanel = new JPanel();
        brushesPanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), 0, 0));
        square16 = new JButton();
        square16.setIcon(new ImageIcon(getClass().getResource("/brushes/square_16x16.png")));
        square16.setText("");
        brushesPanel.add(square16, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        square12 = new JButton();
        square12.setIcon(new ImageIcon(getClass().getResource("/brushes/square_12x12.png")));
        square12.setText("");
        brushesPanel.add(square12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        square8 = new JButton();
        square8.setIcon(new ImageIcon(getClass().getResource("/brushes/square_8x8.png")));
        square8.setText("");
        brushesPanel.add(square8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        square4 = new JButton();
        square4.setHideActionText(false);
        square4.setIcon(new ImageIcon(getClass().getResource("/brushes/square_4x4.png")));
        square4.setText("");
        brushesPanel.add(square4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        round16 = new JButton();
        round16.setIcon(new ImageIcon(getClass().getResource("/brushes/round_16x16.png")));
        round16.setText("");
        brushesPanel.add(round16, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        round12 = new JButton();
        round12.setIcon(new ImageIcon(getClass().getResource("/brushes/round_12x12.png")));
        round12.setText("");
        brushesPanel.add(round12, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        round8 = new JButton();
        round8.setIcon(new ImageIcon(getClass().getResource("/brushes/round_8x8.png")));
        round8.setText("");
        brushesPanel.add(round8, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        round4 = new JButton();
        round4.setIcon(new ImageIcon(getClass().getResource("/brushes/round_4x4.png")));
        round4.setText("");
        brushesPanel.add(round4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        line16 = new JButton();
        line16.setIcon(new ImageIcon(getClass().getResource("/brushes/line_16.png")));
        line16.setText("");
        brushesPanel.add(line16, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        line12 = new JButton();
        line12.setIcon(new ImageIcon(getClass().getResource("/brushes/line_12.png")));
        line12.setText("");
        brushesPanel.add(line12, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        line8 = new JButton();
        line8.setIcon(new ImageIcon(getClass().getResource("/brushes/line_8.png")));
        line8.setText("");
        brushesPanel.add(line8, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        line4 = new JButton();
        line4.setIcon(new ImageIcon(getClass().getResource("/brushes/line_4.png")));
        line4.setText("");
        brushesPanel.add(line4, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return brushesPanel;
    }
}
