package com.defano.wyldcard.effect;

import com.defano.hypertalk.ast.model.specifier.VisualEffectSpecifier;
import com.defano.jsegue.AnimatedSegue;
import com.defano.jsegue.SegueAnimationObserver;
import com.defano.jsegue.SegueCompletionObserver;
import com.defano.wyldcard.runtime.ExecutionContext;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

/**
 * The "screen curtain" represents a drawable layer that obscures the card with a fixed or animated graphic (for the
 * purposes of 'lock screen' and visual effects). This class provides a facade for managing the screen curtain.
 */
public class CurtainManager implements SegueAnimationObserver, SegueCompletionObserver {

    private final Set<CurtainObserver> curtainObservers = new HashSet<>();
    private ScheduledFuture animationResult;
    private AnimatedSegue activeEffect;

    public synchronized void lockScreen(ExecutionContext context) {
        // Nothing to do when trying to lock a locked screen
        if (!isScreenLocked()) {
            start(VisualEffectFactory.createScreenLock(context));
        }
    }

    public synchronized void unlockScreen(ExecutionContext context, VisualEffectSpecifier effectSpecifier) {
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
        if (animationResult != null) {
            try {
                animationResult.get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new IllegalStateException("An error occurred rendering visual effect", e);
            } catch (CancellationException ce) {
                // Nothing to do
            }
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
        this.activeEffect = effect;
        this.activeEffect.addAnimationObserver(this);
        this.activeEffect.addCompletionObserver(this);
        this.animationResult = this.activeEffect.start();
    }

    private void finish(boolean raiseCurtain) {
        activeEffect.stop();
        activeEffect.removeAnimationObserver(this);
        activeEffect.removeCompletionObserver(this);
        activeEffect = null;

        if (raiseCurtain) {
            fireOnCurtainUpdated(null);
            animationResult.cancel(false);
        }
    }

    public boolean isScreenLocked() {
        return animationResult != null && !animationResult.isDone();
    }

    private void fireOnCurtainUpdated(BufferedImage screenCurtain) {
        SwingUtilities.invokeLater(() -> {
            for (CurtainObserver thisObserver : curtainObservers.toArray(new CurtainObserver[0])) {
                thisObserver.onCurtainUpdated(screenCurtain);
            }
        });
    }
}
