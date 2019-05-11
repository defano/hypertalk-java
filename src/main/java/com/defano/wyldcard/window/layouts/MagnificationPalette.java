package com.defano.wyldcard.window.layouts;

import com.defano.hypertalk.ast.model.enums.ToolType;
import com.defano.jmonet.model.PaintToolType;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.window.WyldCardDialog;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

public class MagnificationPalette extends WyldCardDialog<Object> {

    private final static MagnificationPalette instance = new MagnificationPalette();

    private JSlider slider1;
    private JPanel windowPanel;
    private JButton magnifierButton;

    private MagnificationPalette() {

        ImageIcon magPlusIcon = new ImageIcon(getClass().getResource("/icons/magnifier_plus.png"));
        ImageIcon magMinusIcon = new ImageIcon(getClass().getResource("/icons/magnifier_minus.png"));
        ImageIcon magIcon = new ImageIcon(getClass().getResource("/icons/magnifier.png"));

        Hashtable<Integer, JLabel> map = new Hashtable<>();
        map.put(1, new JLabel("1x"));
        map.put(16, new JLabel("16x"));
        map.put(32, new JLabel("32x"));
        slider1.setLabelTable(map);

        magnifierButton.addActionListener(e -> WyldCard.getInstance().getPaintManager().forceToolSelection(ToolType.MAGNIFIER, false));
        WyldCard.getInstance().getPaintManager().getPaintToolProvider().subscribe(tool -> magnifierButton.setEnabled(tool.getPaintToolType() != PaintToolType.MAGNIFIER));

        WyldCard.getInstance().getKeyboardManager().addGlobalKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isShiftDown()) {
                    magnifierButton.setIcon(magMinusIcon);
                } else if (e.isControlDown() || e.isAltDown() || e.isMetaDown()) {
                    magnifierButton.setIcon(magIcon);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                magnifierButton.setIcon(magPlusIcon);
            }
        });

        WyldCard.getInstance().getStackManager().getScaleProvider().subscribe(aDouble -> slider1.setValue(WyldCard.getInstance().getStackManager().getScaleProvider().blockingFirst().intValue()));

        slider1.setValue(WyldCard.getInstance().getStackManager().getScaleProvider().blockingFirst().intValue());
        slider1.addChangeListener(e -> WyldCard.getInstance().getStackManager().getFocusedCard().getActiveCanvas().setScale(slider1.getValue()));
    }

    public static MagnificationPalette getInstance() {
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
        windowPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        windowPanel.setMaximumSize(new Dimension(246, 32767));
        windowPanel.setMinimumSize(new Dimension(246, 10));
        slider1 = new JSlider();
        slider1.setMajorTickSpacing(8);
        slider1.setMaximum(32);
        slider1.setMinimum(1);
        slider1.setMinorTickSpacing(2);
        slider1.setPaintLabels(false);
        slider1.setPaintTicks(true);
        slider1.setValueIsAdjusting(false);
        windowPanel.add(slider1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        windowPanel.add(spacer1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        magnifierButton = new JButton();
        magnifierButton.setEnabled(true);
        magnifierButton.setHorizontalTextPosition(2);
        magnifierButton.setIcon(new ImageIcon(getClass().getResource("/icons/magnifier_plus.png")));
        magnifierButton.setIconTextGap(0);
        magnifierButton.setMargin(new Insets(0, 0, 0, 0));
        magnifierButton.setOpaque(true);
        magnifierButton.setText("");
        magnifierButton.setVisible(true);
        windowPanel.add(magnifierButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return windowPanel;
    }

}
