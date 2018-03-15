package com.defano.wyldcard.parts.msgbox;

import com.defano.wyldcard.aspect.RunOnDispatch;
import com.defano.wyldcard.parts.field.AddressableSelection;
import com.defano.wyldcard.parts.field.SelectableTextModel;
import com.defano.wyldcard.parts.model.DispatchComputedGetter;
import com.defano.wyldcard.parts.model.DispatchComputedSetter;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.window.WindowManager;
import com.defano.hypertalk.ast.model.Owner;
import com.defano.hypertalk.ast.model.PartType;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.model.specifiers.PartMessageSpecifier;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import com.defano.hypertalk.utils.Range;

import javax.swing.text.JTextComponent;

public class MsgBoxModel extends PartModel implements AddressableSelection, SelectableTextModel {

    public MsgBoxModel() {
        super(PartType.MESSAGE_BOX, Owner.HYPERCARD, null);

        defineProperty(PROP_ID, new Value(0), true);
        defineProperty(PROP_CONTENTS, new Value(), false);
        defineComputedGetterProperty(PROP_CONTENTS, (DispatchComputedGetter) (model, propertyName) -> new Value(getText()));

        defineProperty(PROP_WIDTH, new Value(WindowManager.getInstance().getMessageWindow().getWindow().getWidth()), true);
        defineProperty(PROP_HEIGHT, new Value(WindowManager.getInstance().getMessageWindow().getWindow().getHeight()), true);
        defineProperty(PROP_NAME, new Value("Message"), true);

        defineComputedGetterProperty(PartModel.PROP_LEFT, (DispatchComputedGetter) (model, propertyName) -> new Value(WindowManager.getInstance().getMessageWindow().getWindow().getLocation().x));
        defineComputedSetterProperty(PartModel.PROP_LEFT, (DispatchComputedSetter) (model, propertyName, value) -> WindowManager.getInstance().getMessageWindow().getWindow().setLocation(value.integerValue(), WindowManager.getInstance().getMessageWindow().getWindow().getY()));

        defineComputedGetterProperty(PartModel.PROP_TOP, (DispatchComputedGetter) (model, propertyName) -> new Value(WindowManager.getInstance().getMessageWindow().getWindow().getLocation().y));
        defineComputedSetterProperty(PartModel.PROP_TOP, (DispatchComputedSetter) (model, propertyName, value) -> WindowManager.getInstance().getMessageWindow().getWindow().setLocation(WindowManager.getInstance().getMessageWindow().getWindow().getX(), value.integerValue()));

        defineComputedGetterProperty(PROP_VISIBLE, (DispatchComputedGetter) (model, propertyName) -> new Value(WindowManager.getInstance().getMessageWindow().isVisible()));
        defineComputedSetterProperty(PROP_VISIBLE, (DispatchComputedSetter) (model, propertyName, value) -> WindowManager.getInstance().getMessageWindow().setVisible(value.booleanValue()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectableTextModel getSelectableTextModel() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(Range selection) {
        JTextComponent messageBox = WindowManager.getInstance().getMessageWindow().getTextComponent();
        messageBox.setSelectionStart(selection.start);
        messageBox.setSelectionEnd(selection.end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Range getSelection() {
        JTextComponent messageBox = WindowManager.getInstance().getMessageWindow().getTextComponent();
        return Range.ofMarkAndDot(messageBox.getSelectionStart(), messageBox.getSelectionEnd());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RunOnDispatch
    public String getText() {
        JTextComponent messageBox = WindowManager.getInstance().getMessageWindow().getTextComponent();
        return messageBox.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewDidUpdateSelection(Range selection) {
        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHyperTalkAddress() {
        return "the message";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PartSpecifier getPartSpecifier() {
        return new PartMessageSpecifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void relinkParentPartModel(PartModel parentPartModel) {
        // Nothing to do
    }
}
