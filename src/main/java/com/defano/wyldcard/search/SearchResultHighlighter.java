package com.defano.wyldcard.search;

import com.defano.hypertalk.ast.model.Owner;
import com.defano.wyldcard.WyldCard;
import com.defano.wyldcard.parts.field.FieldModel;
import com.defano.wyldcard.parts.field.FieldPart;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.wyldcard.util.ThreadUtils;

import javax.swing.*;

public interface SearchResultHighlighter {

    default void clearSearchHighlights(ExecutionContext context) {
        ThreadUtils.invokeAndWaitAsNeeded(() -> {
            for (FieldPart parts : context.getCurrentCard().getFields()) {
                parts.getHyperCardTextPane().clearSearchHilights();
            }
        });
    }

    default void highlightSearchResult(ExecutionContext context, SearchResult result) {
        ThreadUtils.invokeAndWaitAsNeeded(() -> {
            // Clear existing highlights
            clearSearchHighlights(context);

            // Search result is on a different card; go there
            if (result.getCardIndex() != context.getCurrentCard().getPartModel().getCardIndexInStack()) {
                WyldCard.getInstance().getNavigationManager().goCard(context, context.getCurrentStack(), result.getCardIndex(), false);
            }

            // Box the found text
            SwingUtilities.invokeLater(() -> {
                try {
                    FieldModel foundFieldModel = result.getLocalPartSpecifier(context).getOwner() == Owner.CARD ?
                            (FieldModel) context.getCurrentCard().getPartModel().findPart(context, result.getLocalPartSpecifier(context)) :
                            (FieldModel) context.getCurrentCard().getPartModel().getBackgroundModel().findPart(context, result.getLocalPartSpecifier(context));

                    FieldPart foundField = (FieldPart) context.getCurrentCard().getPart(foundFieldModel);

                    foundField.applySearchHilight(result.getRange());
                    foundField.getHyperCardTextPane().setCaretPosition(result.getRange().start);
                } catch (Exception e) {
                    throw new IllegalStateException("Bug! Search result refers to a bogus part.", e);
                }
            });
        });
    }
}
