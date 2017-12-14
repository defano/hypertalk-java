package com.defano.hypertalk.ast.functions;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.menu.HyperCardMenuBar;
import com.defano.hypercard.parts.bkgnd.BackgroundModel;
import com.defano.hypercard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.common.Countable;
import com.defano.hypertalk.ast.common.Owner;
import com.defano.hypertalk.ast.common.PartType;
import com.defano.hypertalk.ast.common.Value;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Implementation of a HyperTalk function that counts the number of elements in a given container.
 */
public class NumberOfFunc extends Expression {

    public final Countable itemType;
    public final Expression expression;

    public NumberOfFunc(ParserRuleContext context, Countable itemType) {
        super(context);
        this.itemType = itemType;
        this.expression = null;
    }

    public NumberOfFunc(ParserRuleContext context, Countable itemType, Expression expression) {
        super(context);
        this.itemType = itemType;
        this.expression = expression;
    }

    public Value onEvaluate() throws HtException {
        switch (itemType) {
            case CHAR:
                return new Value(expression.evaluate().charCount());
            case WORD:
                return new Value(expression.evaluate().wordCount());
            case LINE:
            case MENU_ITEMS:
                return new Value(expression.evaluate().lineCount());
            case ITEM:
                return new Value(expression.evaluate().itemCount());
            case CARD_PARTS:
                return new Value(ExecutionContext.getContext().getCurrentCard().getCardModel().getPartCount(null, Owner.CARD));
            case BKGND_PARTS:
                return new Value(ExecutionContext.getContext().getCurrentCard().getCardModel().getPartCount(null, Owner.BACKGROUND));
            case CARD_BUTTONS:
                return new Value(ExecutionContext.getContext().getCurrentCard().getCardModel().getPartCount(PartType.BUTTON, Owner.CARD));
            case BKGND_BUTTONS:
                return new Value(ExecutionContext.getContext().getCurrentCard().getCardModel().getPartCount(PartType.BUTTON, Owner.BACKGROUND));
            case CARD_FIELDS:
                return new Value(ExecutionContext.getContext().getCurrentCard().getCardModel().getPartCount(PartType.FIELD, Owner.CARD));
            case BKGND_FIELDS:
                return new Value(ExecutionContext.getContext().getCurrentCard().getCardModel().getPartCount(PartType.FIELD, Owner.BACKGROUND));
            case MENUS:
                return new Value(HyperCardMenuBar.instance.getMenuCount());
            case CARDS:
                return new Value(HyperCard.getInstance().getStack().getCardCountProvider().get());
            case MARKED_CARDS:
                return new Value(HyperCard.getInstance().getStack().getStackModel().getMarkedCards().size());
            case BKGNDS:
                return new Value(HyperCard.getInstance().getStack().getStackModel().getBackgroundCount());
            case CARDS_IN_BKGND:
                BackgroundModel model = expression.partFactor(BackgroundModel.class, new HtSemanticException("No such background."));
                return new Value(HyperCard.getInstance().getStack().getStackModel().getCardsInBackground(model.getId()).size());
            default:
                throw new RuntimeException("Bug! Unimplemented countable item type: " + itemType);
        }
    }
}
