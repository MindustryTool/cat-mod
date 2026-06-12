package org.mindustrytool.libs.signal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the reactive signal system.
 */
class SignalTest {

    /** Helper: ThreadTarget that runs synchronously on the calling thread. */
    private static final ThreadTarget SYNC = ThreadTarget.of((java.util.function.Consumer<Runnable>) Runnable::run);

    @Test
    void effectRerunsWhenSignalChanges() {
        var signal = Signal.of(0);
        var runs = new int[]{0};

        var effect = new Effect(() -> {
            signal.get(); // track
            runs[0]++;
        }, SYNC);

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
        // This simulates what happens in EffectHost.add() called inside a children() Effect:
        // An outer effect runs, and inside it, creates an inner effect (like component.style()).
        // The inner effect must subscribe to its signals independently.

        var outerTrigger = Signal.of(0);
        var innerSignal = Signal.of("hello");
        var innerRuns = new int[]{0};

        // Outer effect: when it runs, it creates a new inner Effect
        // (simulates children() effect creating component.style() effects)
        Effect[] innerRef = {null};

        var outerEffect = new Effect(() -> {
            outerTrigger.get(); // outer tracks outerTrigger

            // Inner effect created inside outer — simulates component.style()
            if (innerRef[0] != null) innerRef[0].dispose();
            innerRef[0] = new Effect(() -> {
                innerSignal.get(); // inner tracks innerSignal
                innerRuns[0]++;
            }, SYNC);
        }, SYNC);

        assertEquals(1, innerRuns[0], "inner effect should run once on creation");

        // Changing innerSignal should trigger the inner effect (not outer)
        innerSignal.set("world");
        assertEquals(2, innerRuns[0], "inner effect should re-run when innerSignal changes");

        outerEffect.dispose();
        if (innerRef[0] != null) innerRef[0].dispose();
    }

    @Test
    void signalHasSubscribersAfterEffectCreation() {
        var signal = Signal.of(false);

        // Simulate: Effect created (like CustomComponent.style()) that reads signal
        var effect = new Effect(() -> {
            var val = signal.get(); // subscribe
        }, SYNC);

        // Signal MUST have 1 subscriber now
        assertEquals(1, signal.subscribers.size(),
            "signal must have 1 subscriber after effect creation");

        signal.set(true); // trigger re-run

        // After re-run, signal must still have 1 subscriber (re-subscribed)
        assertEquals(1, signal.subscribers.size(),
            "signal must still have 1 subscriber after effect re-run");

        effect.dispose();
        assertEquals(0, signal.subscribers.size(),
            "signal must have 0 subscribers after dispose");
    }

    @Test
    void signalHasSubscribersWhenEffectCreatedInsideAnotherEffect() {
        // This is the exact scenario in DemoUI:
        // children() effect runs → inside it, toggle().style() creates an Effect
        // that reads borderEnabled. borderEnabled must have 1 subscriber.

        var borderEnabled = Signal.of(false);
        var childrenRuns = new int[]{0};

        // Simulate children() effect
        var toggleEffectRef = new Effect[]{null};
        var childrenEffect = new Effect(() -> {
            childrenRuns[0]++;

            // Simulate toggle().style() — creates inner effect that reads borderEnabled
            if (toggleEffectRef[0] != null) toggleEffectRef[0].dispose();
            toggleEffectRef[0] = new Effect(() -> {
                var val = borderEnabled.get(); // subscribe to borderEnabled
            }, SYNC);
        }, SYNC);

        // borderEnabled must now have the toggle's effect as subscriber
        assertEquals(1, borderEnabled.subscribers.size(),
            "borderEnabled must have 1 subscriber (toggle's effect)");

        // children effect must NOT have subscribed to borderEnabled
        // (it didn't call borderEnabled.get() directly)
        // This depends on STACK isolation being correct

        // Changing borderEnabled → toggle's effect should re-run (not children)
        int childrenRunsBefore = childrenRuns[0];
        borderEnabled.set(true);

        assertEquals(childrenRunsBefore, childrenRuns[0],
            "children effect must NOT re-run when borderEnabled changes");

        childrenEffect.dispose();
        if (toggleEffectRef[0] != null) toggleEffectRef[0].dispose();
    }
}
