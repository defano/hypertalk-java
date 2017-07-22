package com.defano.hypertalk.ast.statements;

import com.defano.hypercard.gui.fx.CurtainManager;
import com.defano.hypertalk.ast.common.VisualEffectSpecifier;
import com.defano.hypertalk.exception.HtException;

public class UnlockScreenCmd extends Statement {

    private final VisualEffectSpecifier effect;

    public UnlockScreenCmd(VisualEffectSpecifier effect) {
        this.effect = effect;
    }

    @Override
    public void execute() throws HtException {
        CurtainManager.getInstance().unlockScreenWithEffect(effect);
    }
}
