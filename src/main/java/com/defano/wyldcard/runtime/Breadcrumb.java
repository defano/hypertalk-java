package com.defano.wyldcard.runtime;

import com.defano.wyldcard.parts.PartException;
import com.defano.wyldcard.parts.model.PartModel;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.defano.hypertalk.ast.model.specifiers.PartSpecifier;
import com.defano.hypertalk.utils.Range;
import org.antlr.v4.runtime.Token;

public class Breadcrumb {

    private final Token token;
    private final PartSpecifier part;

    public Breadcrumb (Token token, PartSpecifier part) {
        this.token = token;
        this.part = part;
    }

    public Breadcrumb (Token token) {
        this.token = token;
        this.part = null;
    }

    public Range getCharRange() {
        int start = token.getStartIndex();
        int end = token.getStopIndex();

        if (end > start) {
            return new Range(start, end + 1);
        }

        return null;
    }

    public Token getToken() {
        return token;
    }

    public PartSpecifier getPart() {
        return part;
    }

    @Override
    public String toString() {
        String breadcrumb = "";

        if ( token != null) {
            breadcrumb += "line " + token.getLine() + ", column " + token.getCharPositionInLine();
        }

        if (part != null) {
            breadcrumb += " of " + part.getHyperTalkIdentifier(new ExecutionContext());
        }

        return breadcrumb;
    }

    public PartModel getPartModel(ExecutionContext context) {
        PartModel partModel = null;

        if (getPart() != null) {
            try {
                partModel = context.getPart(getPart());
            } catch (PartException e) {
                // Nothing to do
            }
        }

        return partModel;
    }

}
