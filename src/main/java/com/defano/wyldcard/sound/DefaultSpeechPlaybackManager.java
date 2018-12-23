package com.defano.wyldcard.sound;

import com.defano.hypertalk.ast.model.SpeakingVoice;
import com.defano.hypertalk.ast.model.Value;
import com.defano.hypertalk.exception.HtException;
import com.defano.hypertalk.exception.HtSemanticException;
import com.google.inject.Singleton;
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class DefaultSpeechPlaybackManager extends ThreadPoolExecutor implements SpeechPlaybackManager {

    private LocalMaryInterface mary;
    private String theSpeech = "done";

    public DefaultSpeechPlaybackManager() {
        super(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        try {
            this.mary = new LocalMaryInterface();
        } catch (MaryConfigurationException e) {
            this.mary = null;
        }
    }

    @Override
    public Value getTheSpeech() {
        if (getActiveCount() == 0 && getQueue().size() == 0) {
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

            } catch (SynthesisException | IOException | LineUnavailableException | InterruptedException e) {
                // Nothing useful to do
                e.printStackTrace();
            }
        });
    }
}
