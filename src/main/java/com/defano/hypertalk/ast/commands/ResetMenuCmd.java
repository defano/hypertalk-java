package com.defano.hypertalk.ast.commands;

import com.defano.hypercard.menu.HyperCardMenuBar;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import org.antlr.v4.runtime.ParserRuleContext;

public class ResetMenuCmd extends Command {

    public ResetMenuCmd(ParserRuleContext context) {
        super(context, "reset");
    }

    @Override
    public void onExecute() throws HtException {
        HyperCardMenuBar.instance.reset();
    }
}
