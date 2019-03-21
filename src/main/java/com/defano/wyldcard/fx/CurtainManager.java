package com.defano.wyldcard.fx;

import com.defano.hypertalk.ast.model.specifiers.VisualEffectSpecifier;
import com.defano.jsegue.AnimatedSegue;
import com.defano.jsegue.SegueAnimationObserver;
import com.defano.jsegue.SegueCompletionObserver;
import com.defano.wyldcard.runtime.context.ExecutionContext;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * The "screen curtain" represents a drawable layer that obscures the card with a fixed or animated graphic (for the
 * purposes of 'lock screen' and visual effects). This class provides a facade for managing the screen curtain.
 */
public class CurtainManager implements SegueAnimationObserver, SegueCompletionObserver {

    private final Set<CurtainObserver> curtainObservers = new HashSet<>();
    private CountDownLatch latch = new CountDownLatch(0);
    private AnimatedSegue activeEffect;

    synchronized public void lockScreen(ExecutionContext context) {
        // Nothing to do when trying to lock a locked screen
        if (!isScreenLocked()) {
            start(VisualEffectFactory.createScreenLock(context));
        }
    }

    synchronized public void unlockScreen(ExecutionContext context, VisualEffectSpecifier effectSpecifier) {
        if (isScreenLocked()) {

            // Unlock without effect
            if (effectSpecifier == null) {
                finish(true);
            }

            // Unlock with animated visual effect
            else {
                BufferedImage src = activeEffect.getSource();
                BufferedImage dest = context.getCurrentStack().getDisplayedCard().getScreenshot();

                // Stop screen locking effect
                finish(false);

                // Start unlock animation
                start(VisualEffectFactory.create(effectSpecifier, src, dest));
            }
        }

        // Nothing to do if screen is not locked
    }

    public void waitForEffectToFinish() {
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void addScreenCurtainObserver(CurtainObserver observer) {
        curtainObservers.add(observer);
    }

    public void removeScreenCurtainObserver(CurtainObserver observer) {
        curtainObservers.remove(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSegueAnimationCompleted(AnimatedSegue effect) {
        finish(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFrameRendered(AnimatedSegue segue, BufferedImage image) {
        fireOnCurtainUpdated(image);
    }

    private void start(AnimatedSegue effect) {
        if (this.latch.getCount() == 0) {
            this.latch = new CountDownLatch(1);
        }

        this.activeEffect = effect;
        this.activeEffect.addAnimationObserver(this);
        this.activeEffect.addCompletionObserver(this);
        this.activeEffect.start();
    }

    private void finish(boolean raiseCurtain) {
        activeEffect.stop();
        activeEffect.removeAnimationObserver(this);
        activeEffect.removeCompletionObserver(this);
        activeEffect = null;

        if (raiseCurtain) {
            fireOnCurtainUpdated(null);
            latch.countDown();
        }
    }

    private boolean isScreenLocked() {
        return latch.getCount() > 0;
    }

    private void fireOnCurtainUpdated(BufferedImage screenCurtain) {
        SwingUtilities.invokeLater(() -> {
            for (CurtainObserver thisObserver : curtainObservers.toArray(new CurtainObserver[0])) {
                thisObserver.onCurtainUpdated(screenCurtain);
            }
        });
    }
}
