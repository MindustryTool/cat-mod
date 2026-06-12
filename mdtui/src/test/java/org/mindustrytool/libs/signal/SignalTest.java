package org.mindustrytool.libs.signal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SignalTest {

    @Test
    void effectRerunsWhenSignalChanges() {
        var signal = Signal.of(0);
        var runs = new int[]{0};

        var effect = new Effect(() -> {
            signal.get();
            runs[0]++;
        });

        assertEquals(1, runs[0], "effect should run once on creation");

        signal.set(1);
        assertEquals(2, runs[0], "effect should re-run after signal change");

        signal.set(2);
        assertEquals(3, runs[0], "effect should re-run again");

        effect.dispose();
        signal.set(3);
        assertEquals(3, runs[0], "disposed effect should NOT re-run");
    }

    @Test
    void effectCreatedInsideAnotherEffectStillSubscribes() {
        var outerTrigger = Signal.of(0);
        var innerSignal = Signal.of("hello");
        var innerRuns = new int[]{0};

        Effect[] innerRef = {null};

        var outerEffect = new Effect(() -> {
            outerTrigger.get();

            if (innerRef[0] != null) innerRef[0].dispose();
            innerRef[0] = new Effect(() -> {
                innerSignal.get();
                innerRuns[0]++;
            });
        });

        assertEquals(1, innerRuns[0], "inner effect should run once on creation");

        innerSignal.set("world");
        assertEquals(2, innerRuns[0], "inner effect should re-run when innerSignal changes");

        outerEffect.dispose();
        if (innerRef[0] != null) innerRef[0].dispose();
    }

    @Test
    void signalHasSubscribersAfterEffectCreation() {
        var signal = Signal.of(false);

        var effect = new Effect(() -> {
            var val = signal.get();
        });

        assertEquals(1, signal.subscribers.size(),
            "signal must have 1 subscriber after effect creation");

        signal.set(true);

        assertEquals(1, signal.subscribers.size(),
            "signal must still have 1 subscriber after effect re-run");

        effect.dispose();
        assertEquals(0, signal.subscribers.size(),
            "signal must have 0 subscribers after dispose");
    }

    @Test
    void signalHasSubscribersWhenEffectCreatedInsideAnotherEffect() {
        var borderEnabled = Signal.of(false);
        var childrenRuns = new int[]{0};

        var toggleEffectRef = new Effect[]{null};
        var childrenEffect = new Effect(() -> {
            childrenRuns[0]++;

            if (toggleEffectRef[0] != null) toggleEffectRef[0].dispose();
            toggleEffectRef[0] = new Effect(() -> {
                var val = borderEnabled.get();
            });
        });

        assertEquals(1, borderEnabled.subscribers.size(),
            "borderEnabled must have 1 subscriber (toggle's effect)");

        int childrenRunsBefore = childrenRuns[0];
        borderEnabled.set(true);

        assertEquals(childrenRunsBefore, childrenRuns[0],
            "children effect must NOT re-run when borderEnabled changes");

        childrenEffect.dispose();
        if (toggleEffectRef[0] != null) toggleEffectRef[0].dispose();
    }
}
