package com.defano.wyldcard.parts.button;

import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.aspect.RunOnDispatch;
import com.defano.wyldcard.awt.mouse.MouseStillDown;
import com.defano.wyldcard.message.SystemMessage;
import com.defano.wyldcard.parts.builder.ButtonModelBuilder;
import com.defano.wyldcard.parts.card.CardLayerPart;
import com.defano.wyldcard.parts.card.CardPart;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.parts.model.PropertyChangeObserver;
import com.defano.wyldcard.properties.PropertiesModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.WeakReference;

/**
 * The controller object associated with a button on a card.
 *
 * See {@link ButtonModel} for the model associated with this controller.
 * See {@link StyleableButton} for the view associated with this controller.
 */
public class ButtonPart extends StyleableButton implements CardLayerPart<ButtonModel>, MouseListener, PropertyChangeObserver {

    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_HEIGHT = 30;

    private final Owner owner;
    private ButtonModel partModel;
    private final WeakReference<CardPart> parent;

    private ButtonPart(ButtonStyle style, CardPart parent, Owner owner) {
        super(style);

        this.owner = owner;
        this.parent = new WeakReference<>(parent);
    }

    /**
     * Creates a new button on the given card with a given geometry.
     *
     * @param parent The card that this button will belong to.
     * @param rectangle The location and size of this button; when null, the default size and location will be assumed.
     * @return The new button.
     */
    public static ButtonPart newButton(CardPart parent, Owner owner, Rectangle rectangle) {
        if (rectangle == null) {
            rectangle = new Rectangle(
                    parent.getWidth() / 2 - (DEFAULT_WIDTH / 2),
                    parent.getHeight() / 2 - (DEFAULT_HEIGHT / 2),
                    DEFAULT_WIDTH,
                    DEFAULT_HEIGHT);
        }

        ButtonPart button = new ButtonPart(ButtonStyle.ROUND_RECT, parent, owner);
        button.initProperties(rectangle, parent.getPartModel());
        return button;
    }

    /**
     * Creates a new button view from an existing button data model.
     *
     *
     * @param context The execution context.
     * @param parent The card that this button will belong to.
     * @param partModel The data model representing this button.
     * @return The new button.
     */
    public static ButtonPart fromModel(ExecutionContext context, CardPart parent, ButtonModel partModel) {
        ButtonStyle style = ButtonStyle.fromName(partModel.get(context, ButtonModel.PROP_STYLE).toString());
        ButtonPart button = new ButtonPart(style, parent, partModel.getOwner());

        button.partModel = partModel;
        button.partModel.setCurrentCardId(parent.getId(context));
        return button;
    }

    @Override
    @RunOnDispatch
    public void partOpened(ExecutionContext context) {
        super.partOpened(context);
        partModel.addPropertyChangedObserver(this);
    }

    @Override
    @RunOnDispatch
    public void partClosed(ExecutionContext context) {
        super.partClosed(context);
        partModel.removePropertyChangedObserver(this);
        WyldCard.getInstance().getPeriodicMessageManager().removeWithin(getPartModel());
    }

    @Override
    public CardPart getCard() {
        return parent.get();
    }

    @Override
    public CardLayerPart getPart() {
        return this;
    }

    @Override
    public ToolType getEditTool() {
        return ToolType.BUTTON;
    }

    @Override
    @RunOnDispatch
    public void replaceViewComponent(ExecutionContext context, Component oldButtonComponent, Component newButtonComponent) {
        CardPart cardPart = parent.get();
        if (cardPart != null) {
            cardPart.replaceViewComponent(context, this, oldButtonComponent, newButtonComponent);
        }
    }

    @Override
    public PartType getType() {
        return PartType.BUTTON;
    }

    @Override
    public JComponent getComponent() {
        return this.getButtonComponent();
    }

    @Override
    public ButtonModel getPartModel() {
        return partModel;
    }

    @Override
    @RunOnDispatch
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (SwingUtilities.isLeftMouseButton(e) && !isPartToolActive()) {
            getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_DOWN);
            MouseStillDown.then(() -> getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_STILL_DOWN));
        }
    }

    @Override
    @RunOnDispatch
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        boolean isStillInFocus = new Rectangle(this.getButtonComponent().getSize()).contains(e.getPoint());

        // Do not send mouseUp if cursor is not released over the part
        if (SwingUtilities.isLeftMouseButton(e) && isStillInFocus && !isPartToolActive()) {
            getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_UP);
        }
    }

    @Override
    @RunOnDispatch
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);

        if (!isPartToolActive()) {
            getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_ENTER);
            WyldCard.getInstance().getPeriodicMessageManager().addWithin(getPartModel());
        }
    }

    @Override
    @RunOnDispatch
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);

        if (!isPartToolActive()) {
            getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_LEAVE);
            WyldCard.getInstance().getPeriodicMessageManager().removeWithin(getPartModel());
        }
    }

    @Override
    @RunOnDispatch
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2 && !isPartToolActive()) {
            getPartModel().receiveMessage(new ExecutionContext(this), SystemMessage.MOUSE_DOUBLE_CLICK);
        }
    }

    @Override
    @RunOnDispatch
    public void onPropertyChanged(ExecutionContext context, PropertiesModel model, String property, Value oldValue, Value newValue) {
        switch (property) {
            case ButtonModel.PROP_STYLE:
                setStyle(context, ButtonStyle.fromName(newValue.toString()));
                break;
            case ButtonModel.PROP_TOP:
            case ButtonModel.PROP_LEFT:
            case ButtonModel.PROP_WIDTH:
            case ButtonModel.PROP_HEIGHT:
                getButtonComponent().setBounds(partModel.getRect(context));
                getButtonComponent().validate();
                getButtonComponent().repaint();
                break;
            case ButtonModel.PROP_ENABLED:
                setEnabledOnCard(context, newValue.booleanValue());
                break;
            case ButtonModel.PROP_VISIBLE:
                setVisibleWhenBrowsing(context, newValue.booleanValue());
                break;
            case ButtonModel.PROP_ZORDER:
                getCard().onDisplayOrderChanged(context);
                break;
        }
    }

    private void initProperties(Rectangle geometry, PartModel parentPartModel) {
        CardPart cardPart = parent.get();
        if (cardPart != null) {
            int id = cardPart.getPartModel().getStackModel().getNextButtonId(parentPartModel.getId());
            partModel = new ButtonModelBuilder(owner, parentPartModel).withId(id).withBounds(geometry).build();
        }
    }
}
