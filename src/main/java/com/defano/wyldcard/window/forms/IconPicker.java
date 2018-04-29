package com.defano.wyldcard.window.forms;

import com.defano.wyldcard.aspect.RunOnDispatch;
import com.defano.wyldcard.icons.ButtonIcon;
import com.defano.wyldcard.icons.IconFactory;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.util.WrapLayout;
import com.defano.wyldcard.window.WyldCardDialog;
import com.defano.wyldcard.parts.button.ButtonModel;
import com.defano.hypertalk.ast.model.Value;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class IconPicker extends WyldCardDialog {

    private static final int BUTTON_SIZE = 60;

    private JButton okButton;
    private JPanel windowPanel;
    private JPanel iconPanel;
    private JButton noneButton;
    private JLabel iconSelection;
    private List<JButton> buttons;
    private ButtonModel model;
    private Value selectedIconValue;

    public IconPicker() {

        okButton.addActionListener(e -> {
            model.setKnownProperty(new ExecutionContext(), ButtonModel.PROP_ICON, selectedIconValue);
            dispose();
        });

        noneButton.addActionListener(e -> {
            model.setKnownProperty(new ExecutionContext(), ButtonModel.PROP_ICON, new Value());
            dispose();
        });
    }

    @Override
    public JButton getDefaultButton() {
        return okButton;
    }

    @Override
    public JComponent getWindowPanel() {
        return windowPanel;
    }

    @Override
    @RunOnDispatch
    public void bindModel(Object data) {
        this.model = (ButtonModel) data;
        selectedIconValue = this.model.getKnownProperty(new ExecutionContext(), ButtonModel.PROP_ICON);

        buttons = getButtons();

        for (JButton thisButt : buttons) {
            iconPanel.add(thisButt);
        }

        iconPanel.setSize(400, 1);
        iconPanel.setLayout(new WrapLayout());
        ((WrapLayout) iconPanel.getLayout()).setHgap(2);
        ((WrapLayout) iconPanel.getLayout()).setVgap(2);

        invalidate();
    }

    @RunOnDispatch
    private List<JButton> getButtons() {
        List<JButton> buttons = new ArrayList<>();

        List<ButtonIcon> icons = IconFactory.getAllIcons();
        ButtonIcon selectedIcon = IconFactory.findIconForValue(selectedIconValue, icons);

        for (ButtonIcon thisIcon : IconFactory.getAllIcons()) {
            buttons.add(getButtonForIcon(thisIcon, thisIcon == selectedIcon));
        }

        return buttons;
    }

    @RunOnDispatch
    private JButton getButtonForIcon(ButtonIcon buttonIcon, boolean isSelected) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setIcon(buttonIcon.getIcon());
        button.setFocusable(false);
        button.addActionListener(e -> {
            enableButtons();
            ((JButton) e.getSource()).setEnabled(false);
            selectedIconValue = new Value(buttonIcon.getId());
            iconSelection.setText("Icon ID: " + buttonIcon.getId() + " \"" + buttonIcon.getName() + "\"");
        });

        button.setEnabled(!isSelected);
        return button;
    }

    @RunOnDispatch
    private void enableButtons() {
        for (JButton thisButton : buttons) {
            thisButton.setEnabled(true);
        }
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
        windowPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 10, 0), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(22);
        windowPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(550, 400), null, 0, false));
        iconPanel = new JPanel();
        iconPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
        iconPanel.setFocusable(false);
        iconPanel.setRequestFocusEnabled(false);
        scrollPane1.setViewportView(iconPanel);
        okButton = new JButton();
        okButton.setText("OK");
        windowPanel.add(okButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noneButton = new JButton();
        noneButton.setText("None");
        windowPanel.add(noneButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        iconSelection = new JLabel();
        iconSelection.setText("No icon selected.");
        windowPanel.add(iconSelection, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final Spacer spacer1 = new Spacer();
        windowPanel.add(spacer1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return windowPanel;
    }
}
