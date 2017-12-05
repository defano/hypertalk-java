package com.defano.hypertalk.ast.commands;

import com.defano.hypercard.HyperCard;
import com.defano.hypercard.parts.bkgnd.BackgroundModel;
import com.defano.hypercard.parts.card.CardModel;
import com.defano.hypercard.parts.model.PartModel;
import com.defano.hypercard.runtime.Interpreter;
import com.defano.hypercard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.expressions.PartExp;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import org.antlr.v4.runtime.ParserRuleContext;

public class PushCardCmd extends Command {

    private final PartExp destinationExp;

    public PushCardCmd(ParserRuleContext context) {
        this(context, null);
    }

    public PushCardCmd(ParserRuleContext context, PartExp destinationExp) {
        super(context, "push");
        this.destinationExp = destinationExp;
    }

    @Override
    protected void onExecute() throws HtException {
        if (destinationExp == null) {
            push(HyperCard.getInstance().getDisplayedCard().getId());
        } else {

            Integer pushCardId = evaluateAsCardId(destinationExp);

            if (pushCardId == null) {
                PartExp partRef = Interpreter.dereference(destinationExp.evaluate(), PartExp.class);
                pushCardId = evaluateAsCardId(partRef);

                if (pushCardId == null) {
                    throw new HtSemanticException("Can't push that.");
                }
            }
        }
    }

    private Integer evaluateAsCardId(PartExp card) throws HtException {
        PartModel model = ExecutionContext.getContext().getPart(card.evaluateAsSpecifier());

        if (model instanceof CardModel) {
            return model.getId();
        } else if (model instanceof BackgroundModel) {
            int cardIndex = HyperCard.getInstance().getStack().getStackModel().getIndexOfBackground(model.getId());
            return HyperCard.getInstance().getStack().getStackModel().getCardModel(cardIndex).getId();
        } else {
            return null;
        }
    }

    private void push(int cardId) {
        HyperCard.getInstance().getStack().getStackModel().getBackStack().push(cardId);
    }
}
