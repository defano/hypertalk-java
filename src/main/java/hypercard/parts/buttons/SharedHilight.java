package hypercard.parts.buttons;

import hypercard.parts.ButtonPart;
import hypercard.parts.buttons.styles.RadioButton;
import hypercard.parts.model.ButtonModel;
import hypertalk.ast.common.Value;

/**
 * A mixin providing resuable functionality for enabling shared hilite features (i.e., selecting radio buttons by
 * group).
 */
public interface SharedHilight {

    default void setSharedHilite(ButtonPart button, boolean hilite) {

        // Cannot unselect a radio button by pressing it; can only be unselected by family
        if (button.getComponent() instanceof RadioButton) {
            hilite = true;
        }

        button.getPartModel().setKnownProperty(ButtonModel.PROP_HILITE, new Value(hilite));

        if (isSharingHilite(button)) {

            for (ButtonPart thisButton : button.getCard().getButtons()) {

                if (thisButton.getId() == button.getId()) {
                    continue;
                }

                if (isSharingHilite(thisButton) && sharedHiliteFamily(thisButton) == sharedHiliteFamily(button)) {
                    thisButton.getPartModel().setKnownProperty(ButtonModel.PROP_HILITE, new Value(false));
                }
            }
        }
    }

    default boolean isSharingHilite(ButtonPart buttonPart) {
        return buttonPart.getPartModel().getKnownProperty(ButtonModel.PROP_FAMILY).isInteger();
    }

    default int sharedHiliteFamily(ButtonPart buttonPart) {
        return buttonPart.getPartModel().getKnownProperty(ButtonModel.PROP_FAMILY).integerValue();
    }
}
