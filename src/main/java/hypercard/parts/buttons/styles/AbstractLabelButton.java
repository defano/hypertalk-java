package hypercard.parts.buttons.styles;

import com.defano.jmonet.tools.util.MarchingAnts;
import hypercard.parts.ToolEditablePart;
import hypercard.parts.buttons.ButtonComponent;
import hypercard.parts.model.ButtonModel;
import hypertalk.ast.common.Value;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLabelButton extends JLabel implements ButtonComponent {

    protected final ToolEditablePart toolEditablePart;
    protected boolean drawnDisabled = false;

    protected abstract void drawBorder(boolean isDisabled, Graphics2D g);
    protected abstract void setName(boolean isDisabled, String name);
    protected abstract void setHilite(boolean isDisabled, boolean isHilited);

    public AbstractLabelButton(ToolEditablePart toolEditablePart) {
        super("", SwingConstants.CENTER);
        setBackground(Color.BLACK);

        this.toolEditablePart = toolEditablePart;
        super.setEnabled(true);

        MarchingAnts.getInstance().addObserver(this::repaint);
        super.addMouseListener(toolEditablePart);
        super.addKeyListener(toolEditablePart);
    }

    @Override
    public void paintComponent(Graphics g) {
        drawBorder(drawnDisabled, (Graphics2D) g);
        super.paintComponent(g);
        toolEditablePart.drawSelectionRectangle(g);
    }

    @Override
    public void onPropertyChanged(String property, Value oldValue, Value newValue) {
        switch (property) {
            case ButtonModel.PROP_NAME:
            case ButtonModel.PROP_SHOWNAME:
                boolean showName = toolEditablePart.getPartModel().getKnownProperty(ButtonModel.PROP_SHOWNAME).booleanValue();
                setName(drawnDisabled, showName ? toolEditablePart.getPartModel().getKnownProperty(ButtonModel.PROP_NAME).stringValue() : "");

            case ButtonModel.PROP_HILITE:
                setHilite(drawnDisabled, newValue.booleanValue());
                break;

            case ButtonModel.PROP_ENABLED:
                drawnDisabled = !newValue.booleanValue();
                break;
        }
    }

    protected Color textColor(boolean isDisabled) {
        return isDisabled ? Color.GRAY : Color.BLACK;
    }
}
