package com.defano.hypertalk.ast.statements.commands;

import com.defano.hypertalk.ast.statements.Command;
import com.defano.wyldcard.NavigationManager;
import com.defano.wyldcard.runtime.context.ExecutionContext;
import com.google.inject.Inject;
import org.antlr.v4.runtime.ParserRuleContext;

public class PopCardCmd extends Command {

    @Inject
    private NavigationManager navigationManager;

    public PopCardCmd(ParserRuleContext context) {
        super(context, "pop");
    }

    @Override
    protected void onExecute(ExecutionContext context) {
        navigationManager.goBack(context, context.getCurrentStack());
    }
}
