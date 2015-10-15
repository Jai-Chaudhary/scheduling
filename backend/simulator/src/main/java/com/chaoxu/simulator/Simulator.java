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
    public static State simulateWithExpectation(State state) {
        State s = new State(state);
        s.optimization = false;

        for (Patient p : s.patients) {
            if (p.completion == null) {
                p.duration = (int)p.durationDistribution.expectation(
                        p.begin == null ? 0 : s.time - p.begin);
            }
            if (p.arrival == null) {
                p.lateness = (int)p.latenessDistribution.expectation(
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
        State s = new State(state);
        s.optimization = false;

        for (Patient p : s.patients) {
            if (p.completion == null) {
                p.duration = p.durationDistribution.sample(
                        p.begin == null ? 0 : s.time - p.begin,
                        bits.duration.get(p.name));
            }
            if (p.arrival == null) {
                p.lateness = p.latenessDistribution.sample(
                        s.time - p.appointment,
                        bits.lateness.get(p.name));
            }
        }
        simulate(s, null, false);

        return s;
    }
}
