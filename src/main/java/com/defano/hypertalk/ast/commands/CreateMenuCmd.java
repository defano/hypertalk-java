package com.defano.hypertalk.ast.commands;

import com.defano.hypercard.menu.HyperCardMenuBar;
import com.defano.hypertalk.ast.expressions.Expression;
import com.defano.hypertalk.ast.statements.Command;
import com.defano.hypertalk.exception.HtException;
import org.antlr.v4.runtime.ParserRuleContext;

public class CreateMenuCmd extends Command {

    private final Expression menuName;

    public CreateMenuCmd(ParserRuleContext context, Expression menuName) {
        super(context, "create");
        this.menuName = menuName;
    }

    @Override
    public void onExecute() throws HtException {
        HyperCardMenuBar.instance.createMenu(menuName.evaluate().stringValue());
    }
}
