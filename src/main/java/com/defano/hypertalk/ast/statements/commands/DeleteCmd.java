package com.defano.hypertalk.ast.statements.commands;

import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.expressions.FactorAssociation;
import com.defano.hypertalk.ast.expressions.containers.ContainerExp;
import com.defano.hypertalk.ast.expressions.containers.MenuExp;
import com.defano.hypertalk.ast.expressions.containers.MenuItemExp;
import com.defano.hypertalk.ast.expressions.containers.PartExp;
import com.defano.hypertalk.ast.model.Preposition;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.model.specifiers.CompositePartSpecifier;
import com.defano.hypertalk.ast.model.specifiers.MenuItemSpecifier;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.menu.main.HyperCardMenuBar;
import com.defano.wyldcard.parts.PartException;
import com.defano.wyldcard.parts.card.CardModel;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import org.antlr.v4.runtime.ParserRuleContext;

public class DeleteCmd extends Command {

    private final Expression expression;

    public DeleteCmd(ParserRuleContext context, Expression expression) {
        super(context, "delete");
        this.expression = expression;
    }

    @Override
    protected void onExecute(ExecutionContext context) throws HtException {
        boolean success = expression.factor(
                context, new FactorAssociation<>(MenuItemExp.class, menuItemExp -> deleteMenuItem(context, menuItemExp)),
                new FactorAssociation<>(MenuExp.class, menuExp -> deleteMenu(context, menuExp)),
                new FactorAssociation<>(PartExp.class, part -> deletePart(context, part)),
                new FactorAssociation<>(ContainerExp.class, container -> deleteFromContainer(context, container))
        );

        if (!success) {
            throw new HtSemanticException("Can't delete that.");
        }
    }

    private void deletePart(ExecutionContext context, PartExp part) throws HtException {
        if (part.getChunk() != null) {
            deleteFromContainer(context, part);
        } else {

            try {
                PartSpecifier ps = part.evaluateAsSpecifier(context);
                PartModel p = context.getPart(ps);

                CardModel owner;
                if (ps instanceof CompositePartSpecifier) {
                    owner = WyldCard.getInstance().getActiveStack().getStackModel().findOwningCard(context, (CompositePartSpecifier) ps);
                } else {
                    owner = context.getCurrentCard().getCardModel();
                }

                owner.removePartModel(context, p);
            } catch (PartException e) {
                throw new HtSemanticException("No such " + part.toString() + " to delete", e);
            }
        }
    }

    private void deleteMenuItem(ExecutionContext context, MenuItemExp menuItemExp) throws HtException {
        MenuItemSpecifier specifier = menuItemExp.item;
        specifier.getSpecifiedMenu(context).remove(specifier.getSpecifiedItemIndex(context));
    }

    private void deleteMenu(ExecutionContext context, MenuExp menuExp) throws HtException {
        if (menuExp.getChunk() != null) {
            throw new HtSemanticException("Can't delete a chunk of a menu.");
        }

        HyperCardMenuBar.getInstance().deleteMenu(menuExp.menu.getSpecifiedMenu(context));
    }

    private void deleteFromContainer(ExecutionContext context, ContainerExp container) throws HtException {
        container.putValue(context, new Value(), Preposition.REPLACING);
    }
}
