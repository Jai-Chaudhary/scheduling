package com.chaoxu.simulator;

import java.util.List;
import java.util.ArrayList;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.RandomBits;

import com.chaoxu.simulator.runner.Event;
import com.chaoxu.simulator.runner.Runner;

public class Simulator {

    /**
     * Simulate state until no more event or stopTime,
     * whichever is earlier. Return whether stoped because
     * of stopTime.
     */
    public static boolean simulate(State state,
            Integer stopTime, boolean debug) {
        Runner runner = new Runner(state, debug);
        return runner.run(stopTime);
    }

    /**
     * Tick time of state to the next event. Useful
     * in advancing time to the first event for
     * frontend use
     */
    public static void simulateTick(State state) {
        Runner runner = new Runner(state, false);
        Event e = runner.nextEvent();
        if (e != null) {
            state.time = e.time;
        }
    }

    /**
     * Simulate state with conditional expectation of duration
     * and lateness. This will be used as canonical simulation
     * in the front end.
     */
    public static State simulateWithMedian(State state) {
        State s = state.copy();

        for (Patient p : s.patients) {
            if (p.stat.completion == null) {
                p.secret.duration = (int)p.durationDistribution.median(
                        p.stat.begin == null ? 0 : s.time - p.stat.begin);
            }
            if (p.stat.arrival == null) {
                p.secret.lateness = (int)p.latenessDistribution.median(
                        s.time - p.appointment);
            }
        }
        simulate(s, null, false);

        return s;
    }

    /**
     * Simulate state with conditional sampling of duration
     * and lateness. This is useful in evaluation of schedules.
     */
    public static State simulateWithSampling(
            State state, RandomBits bits) {
        State s = state.copy();

        for (Patient p : s.patients) {
            if (p.stat.completion == null) {
                p.secret.duration = p.durationDistribution.sample(
                        p.stat.begin == null ? 0 : s.time - p.stat.begin,
                        bits.duration.get(p.name));
            }
            if (p.stat.arrival == null) {
                p.secret.lateness = p.latenessDistribution.sample(
                        s.time - p.appointment,
                        bits.lateness.get(p.name));
            }
        }
        simulate(s, null, false);

        return s;
    }
}
