package com.defano.wyldcard.parts.card;

import com.defano.hypertalk.ast.model.LengthAdjective;
import com.defano.hypertalk.ast.model.Owner;
import com.defano.hypertalk.ast.model.PartType;
import com.defano.hypertalk.ast.model.Value;
import com.defano.wyldcard.parts.NamedPart;
import com.defano.wyldcard.parts.bkgnd.BackgroundModel;
import com.defano.wyldcard.parts.button.ButtonModel;
import com.defano.wyldcard.parts.field.FieldModel;
import com.defano.wyldcard.parts.finder.LayeredPartFinder;
import com.defano.wyldcard.parts.finder.OrderedPartFinder;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.parts.stack.StackModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.runtime.serializer.BufferedImageSerializer;
import com.defano.wyldcard.runtime.serializer.Serializer;
import com.defano.wyldcard.util.ThreadUtils;
import com.google.common.collect.Lists;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A data model representing a card in a stack. See {@link CardPart} for the associated
 * controller object.
 */
@SuppressWarnings("WeakerAccess")
public class CardModel extends PartModel implements LayeredPartFinder, NamedPart, PartOwner {

    public final static String PROP_ID = "id";
    public final static String PROP_MARKED = "marked";
    public final static String PROP_CANTDELETE = "cantdelete";
    public final static String PROP_DONTSEARCH = "dontsearch";
    public final static String PROP_NAME = "name";
    public final static String PROP_SHOWPICT = "showpict";
    public static final String PROP_SHORTNAME = "short name";
    public static final String PROP_ABBREVNAME = "abbreviated name";
    public static final String PROP_LONGNAME = "long name";
    public static final String PROP_SHORTID = "short id";
    public static final String PROP_ABBREVID = "abbreviated id";
    public static final String PROP_LONGID = "long id";
    public static final String PROP_OWNER = "owner";
    public static final String PROP_LONGOWNER = "long owner";
    public static final String PROP_SHORTOWNER = "short owner";

    private final Collection<FieldModel> fields = new ArrayList<>();
    private final Collection<ButtonModel> buttons = new ArrayList<>();
    private int backgroundId;
    private BufferedImage cardImage;

    private transient CardModelObserver observer;

    public CardModel(StackModel parentPartModel) {
        super(PartType.CARD, Owner.STACK, parentPartModel);

        newProperty(PROP_ID, new Value(), true);
        newProperty(PROP_MARKED, new Value(false), false);
        newProperty(PROP_CANTDELETE, new Value(false), false);
        newProperty(PROP_DONTSEARCH, new Value(false), false);
        newProperty(PROP_NAME, new Value(), false);
        newProperty(PROP_CONTENTS, new Value(), false);
        newProperty(PROP_SHOWPICT, new Value(true), false);

        initialize();
    }

    @Override
    @PostConstruct
    public void initialize() {
        super.initialize();

        newComputedReadOnlyProperty(PROP_NUMBER, (context, model, propertyName) -> new Value(((OrderedPartFinder) ((CardModel) model).getParentPartModel()).getPartNumber(context, (CardModel) model, PartType.CARD)));

        newComputedReadOnlyProperty(PROP_OWNER, (context, model, propertyName) -> new Value(getBackgroundModel().getName(context)));
        newComputedReadOnlyProperty(PROP_LONGOWNER, (context, model, propertyName) -> new Value(getBackgroundModel().getLongName(context)));
        newComputedReadOnlyProperty(PROP_SHORTOWNER, (context, model, propertyName) -> new Value(getBackgroundModel().getShortName(context)));

        newComputedReadOnlyProperty(PROP_LONGNAME, (context, model, propertyName) -> new Value(getLongName(context)));
        newComputedReadOnlyProperty(PROP_ABBREVNAME, (context, model, propertyName) -> new Value(getAbbreviatedName(context)));
        newComputedReadOnlyProperty(PROP_SHORTNAME, (context, model, propertyName) -> new Value(getShortName(context)));

        newComputedReadOnlyProperty(PROP_LONGID, (context, model, propertyName) -> new Value(getLongId(context)));
        newComputedReadOnlyProperty(PROP_ABBREVID, (context, model, propertyName) -> new Value(getAbbrevId(context)));
        newComputedReadOnlyProperty(PROP_SHORTID, (context, model, propertyName) -> new Value(getShortId(context)));

        // When no name of card is provided, returns 'card id xxx'
        newComputedGetterProperty(PROP_NAME, (context, model, propertyName) -> {
            Value raw = model.getRawProperty(propertyName);
            if (raw == null || raw.isEmpty()) {
                return new Value("card id " + model.getKnownProperty(context, PROP_ID));
            } else {
                return raw;
            }
        });

        // Card inherits size and location properties from the stack
        delegateProperties(Lists.newArrayList(PROP_RECT, PROP_RECTANGLE, PROP_TOP, PROP_LEFT, PROP_BOTTOM, PROP_RIGHT, PROP_TOPLEFT, PROP_BOTTOMRIGHT, PROP_HEIGHT, PROP_WIDTH, StackModel.PROP_RESIZABLE),
                (context, property) -> context.getCurrentStack().getStackModel());
    }

    @Override
    public Collection<FieldModel> getFieldModels() {
        return fields;
    }

    @Override
    public Collection<ButtonModel> getButtonModels() {
        return buttons;
    }

    @Override
    public void removePartModel(ExecutionContext context, PartModel partModel) {
        if (partModel instanceof FieldModel) {
            if (partModel.getOwner() == Owner.BACKGROUND) {
                getBackgroundModel().removePartModel(context, partModel);
            } else {
                fields.remove(partModel);
            }
        } else if (partModel instanceof ButtonModel) {
            if (partModel.getOwner() == Owner.BACKGROUND) {
                getBackgroundModel().removePartModel(context, partModel);
            } else {
                buttons.remove(partModel);
            }
        } else {
            throw new IllegalArgumentException("Bug! Can't delete this kind of part from a card: " + partModel.getType());
        }

        firePartRemoved(context, partModel);
    }

    @Override
    public void addPartModel(PartModel partModel) {
        if (partModel instanceof FieldModel) {
            fields.add((FieldModel) partModel);
        } else if (partModel instanceof ButtonModel) {
            buttons.add((ButtonModel) partModel);
        } else {
            throw new IllegalArgumentException("Bug! Can't add this kind of part to a card: " + partModel.getType());
        }

        partModel.setParentPartModel(this);
    }

    /**
     * Gets the background ID of this card.
     *
     * @return The ID of this card's background.
     */
    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    public BackgroundModel getBackgroundModel() {
        return ((StackModel) getParentPartModel()).getBackground(getBackgroundId());
    }

    public StackModel getStackModel() {
        return getBackgroundModel().getStackModel();
    }

    /**
     * Sets the image representing this card's foreground graphics.
     *
     * @param image The card image.
     */
    public void setCardImage(BufferedImage image) {
        this.cardImage = image;
    }

    /**
     * Returns the image of this card's foreground.
     *
     * @return The foreground image.
     */
    public BufferedImage getCardImage(Dimension dimension) {
        if (cardImage == null) {
            return BufferedImageSerializer.emptyImage(dimension);
        } else {
            return this.cardImage;
        }
    }

    public boolean hasCardImage() {
        return cardImage != null;
    }

    /**
     * Create's a deep copy of this card.
     *
     * @return A copy of this card.
     */
    public CardModel copyOf() {
        return Serializer.copy(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueProperty() {
        return PROP_CONTENTS;
    }

    @Override
    public void relinkParentPartModel(PartModel parentPartModel) {
        setParentPartModel(parentPartModel);

        for (FieldModel thisField : fields) {
            thisField.relinkParentPartModel(this);
        }

        for (ButtonModel thisButton : buttons) {
            thisButton.relinkParentPartModel(this);
        }
    }

    public boolean isMarked(ExecutionContext context) {
        return getKnownProperty(context, PROP_MARKED).booleanValue();
    }

    public void setMarked(ExecutionContext context, boolean marked) {
        setKnownProperty(context, PROP_MARKED, new Value(marked));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LengthAdjective getDefaultAdjectiveForProperty(String propertyName) {
        if (propertyName.equalsIgnoreCase(PROP_NAME)) {
            return LengthAdjective.ABBREVIATED;
        } else if (propertyName.equalsIgnoreCase(PROP_ID)) {
            return LengthAdjective.ABBREVIATED;
        } else {
            return LengthAdjective.DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAdjectiveSupportedProperty(String propertyName) {
        return propertyName.equalsIgnoreCase(PROP_NAME) || propertyName.equalsIgnoreCase(PROP_ID);
    }

    @Override
    public Collection<PartModel> getPartModels(ExecutionContext context) {
        Collection<PartModel> models = new ArrayList<>();
        models.addAll(buttons);
        models.addAll(fields);

        return models;
    }

    /**
     * Gets the zero-based location of this card in its stack.
     *
     * @return The location of this card in the stack.
     */
    public int getCardIndexInStack() {
        return ((StackModel) getParentPartModel()).getIndexOfCard(this);
    }

    public CardModelObserver getObserver() {
        return observer;
    }

    public void setObserver(CardModelObserver observer) {
        this.observer = observer;
    }

    public String getShortId(ExecutionContext context) {
        return String.valueOf(getId(context));
    }

    public String getAbbrevId(ExecutionContext context) {
        return "card id " + getShortId(context);
    }

    public String getLongId(ExecutionContext context) {
        return getAbbrevId(context) + " of " + getStackModel().getLongName(context);
    }

    private boolean hasName() {
        Value raw = getRawProperty(PROP_NAME);
        return raw != null && !raw.isEmpty();
    }

    @Override
    public String getShortName(ExecutionContext context) {
        return getKnownProperty(context, PROP_NAME).toString();
    }

    @Override
    public String getAbbreviatedName(ExecutionContext context) {
        if (hasName()) {
            return "card \"" + getShortName(context) + "\"";
        } else {
            return getShortName(context);
        }
    }

    @Override
    public String getLongName(ExecutionContext context) {
        return getAbbreviatedName(context) + " of " + getStackModel().getLongName(context);
    }

    private void firePartRemoved(ExecutionContext context, PartModel part) {
        if (observer != null) {
            ThreadUtils.invokeAndWaitAsNeeded(() -> observer.onPartRemoved(context, part));
        }
    }

}