package hypercard.parts.model;

import hypercard.parts.buttons.ButtonStyle;
import hypertalk.ast.common.PartType;
import hypertalk.ast.common.Value;

import java.awt.*;

public class ButtonModel extends AbstractPartModel {

    public static final String PROP_STYLE = "style";
    public static final String PROP_FAMILY = "family";
    public static final String PROP_HILITE = "hilite";
    public static final String PROP_AUTOHILIGHT = "autohilite";

    public static final String PROP_TITLE = "title";
    public static final String PROP_SHOWTITLE = "showtitle";
    public static final String PROP_VISIBLE = "visible";
    public static final String PROP_ENABLED = "enabled";

    private ButtonModel() {
        super(PartType.BUTTON);
    }

    public static ButtonModel newButtonModel(Integer id, Rectangle geometry) {
        ButtonModel partModel = new ButtonModel();

        partModel.defineProperty(PROP_SCRIPT, new Value(), false);
        partModel.defineProperty(PROP_ID, new Value(id), true);
        partModel.defineProperty(PROP_NAME, new Value("Button " + id), false);
        partModel.defineProperty(PROP_TITLE, new Value("Button"), false);
        partModel.defineProperty(PROP_LEFT, new Value(geometry.x), false);
        partModel.defineProperty(PROP_TOP, new Value(geometry.y), false);
        partModel.defineProperty(PROP_WIDTH, new Value(geometry.width), false);
        partModel.defineProperty(PROP_HEIGHT, new Value(geometry.height), false);
        partModel.defineProperty(PROP_SHOWTITLE, new Value(true), false);
        partModel.defineProperty(PROP_VISIBLE, new Value(true), false);
        partModel.defineProperty(PROP_ENABLED, new Value(true), false);
        partModel.defineProperty(PROP_STYLE, new Value(ButtonStyle.DEFAULT.getName()), false);
        partModel.defineProperty(PROP_FAMILY, new Value(), false);
        partModel.defineProperty(PROP_HILITE, new Value(false), false);
        partModel.defineProperty(PROP_AUTOHILIGHT, new Value(true), false);

        return partModel;
    }
}