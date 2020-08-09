package com.defano.wyldcard.sound;

import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.ast.model.enums.SpeakingVoice;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.google.inject.Singleton;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class WyldCardSpeechPlaybackManager extends ThreadPoolExecutor implements SpeechPlaybackManager {

    private static final Logger LOG = LoggerFactory.getLogger(WyldCardSpeechPlaybackManager.class);

    private LocalMaryInterface mary;
    private String theSpeech = "done";

    public WyldCardSpeechPlaybackManager() {
        super(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        try {
            this.mary = new LocalMaryInterface();
        } catch (MaryConfigurationException e) {
            this.mary = null;
        }
    }

    @Override
    public Value getTheSpeech() {
        if (getActiveCount() == 0 && getQueue().isEmpty()) {
            return new Value("done");
        }

        return new Value(theSpeech);
    }

    @Override
    public void speak(String text, SpeakingVoice voice) throws HtException {
        if (mary == null) {
            throw new HtSemanticException("Sorry, speaking is not supported on this system.");
        }

        submit(() -> {
            try {
                CountDownLatch latch = new CountDownLatch(1);
                theSpeech = text;

                mary.setVoice(voice.getVoiceId());
                mary.setStreamingAudio(true);

                AudioInputStream audio = mary.generateAudio(text);

                Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        latch.countDown();
                    }
                });
                clip.open(audio);
                clip.start();

                latch.await();

            } catch (SynthesisException | IOException | LineUnavailableException e) {
                LOG.error("An error occurred trying to speak text.", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
