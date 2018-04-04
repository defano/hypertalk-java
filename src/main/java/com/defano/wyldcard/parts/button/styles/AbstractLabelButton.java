package com.defano.wyldcard.parts.button.styles;

import com.defano.wyldcard.fonts.FontUtils;
import com.defano.wyldcard.icons.ButtonIcon;
import com.defano.wyldcard.icons.IconFactory;
import com.defano.wyldcard.icons.AlphaImageIcon;
import com.defano.wyldcard.parts.ContainerWrappedPart;
import com.defano.wyldcard.parts.button.HyperCardButton;
import com.defano.wyldcard.parts.ToolEditablePart;
import com.defano.wyldcard.parts.button.ButtonModel;
import com.defano.wyldcard.parts.button.IconAlignable;
import com.defano.wyldcard.parts.model.PropertiesModel;
import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.runtime.context.ExecutionContext;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLabelButton extends JPanel implements ContainerWrappedPart, HyperCardButton, IconAlignable {

    protected final Color DEFAULT_HILITE_COLOR = new Color(0, 0, 0, 0x90);

    protected final ToolEditablePart toolEditablePart;
    private boolean isHilited = false;
    private JLabel label;

    protected abstract void paintHilite(boolean isHilited, Graphics2D g);

    public AbstractLabelButton(ToolEditablePart toolEditablePart) {
        label = new JLabel("", SwingConstants.CENTER);
        setLayout(new BorderLayout());
        add(label);

        this.toolEditablePart = toolEditablePart;
        super.setEnabled(true);

        super.addMouseListener(toolEditablePart);
        super.addKeyListener(toolEditablePart);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (label.getIcon() != null && label.getIcon() instanceof AlphaImageIcon) {
            ((AlphaImageIcon) label.getIcon()).setAlpha(isHilited ? 0.5f : 1.0f);
        }
        paintHilite(isHilited, (Graphics2D) g);
        label.paintComponents(g);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        toolEditablePart.drawSelectionRectangle(g);
    }

    @Override
    public void onPropertyChanged(ExecutionContext context, PropertiesModel model, String property, Value oldValue, Value newValue) {
        switch (property) {
            case ButtonModel.PROP_NAME:
            case ButtonModel.PROP_SHOWNAME:
                boolean showName = toolEditablePart.getPartModel().getKnownProperty(context, ButtonModel.PROP_SHOWNAME).booleanValue();
                label.setText(showName ? toolEditablePart.getPartModel().getKnownProperty(context, ButtonModel.PROP_NAME).stringValue() : "");

            case ButtonModel.PROP_HILITE:
                isHilited = newValue.booleanValue() && isEnabled();
                label.setForeground(getLabelColor());
                break;

            case ButtonModel.PROP_ENABLED:
                setEnabled(newValue.booleanValue());
                break;

            case ButtonModel.PROP_TEXTSIZE:
                label.setFont(FontUtils.getFontByNameStyleSize(label.getFont().getFamily(), label.getFont().getStyle(), newValue.integerValue()));
                break;

            case ButtonModel.PROP_TEXTFONT:
                label.setFont(FontUtils.getFontByNameStyleSize(newValue.stringValue(), label.getFont().getStyle(), label.getFont().getSize()));
                break;

            case ButtonModel.PROP_TEXTSTYLE:
                label.setFont(FontUtils.getFontByNameStyleSize(label.getFont().getFamily(), FontUtils.getFontStyleForValue(context, newValue), label.getFont().getSize()));
                break;

            case ButtonModel.PROP_TEXTALIGN:
                label.setHorizontalAlignment(FontUtils.getAlignmentForValue(newValue));
                break;

            case ButtonModel.PROP_ICON:
                ButtonIcon icon = IconFactory.findIconForValue(newValue);
                label.setIcon(icon == null ? null : icon.getImage());
                break;

            case ButtonModel.PROP_ICONALIGN:
                setIconAlignment(newValue);
                break;
        }

        revalidate();
        repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            isHilited = false;
        }

        label.setForeground(getLabelColor());
    }

    @Override
    public JComponent getIconComponent() {
        return label;
    }

    @Override
    public JComponent getWrappedComponent() {
        return this;
    }

    private Color getLabelColor() {
        return isHilited ? Color.WHITE : isEnabled() ? Color.BLACK : Color.GRAY;
    }

}
