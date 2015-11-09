package com.chaoxu.simulator;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.RandomBits;
import com.chaoxu.library.Statistics;

public class Evaluator {
    static private List<RandomBits> generatelBits(State state) {

        List<RandomBits> lBits = new ArrayList<>();
        for (int i = 0; i < state.optimizer.numSamples; i++) {
            lBits.add(new RandomBits());
        }

        for (Patient p : state.patients) {
            RandomGenerator rng = new MersenneTwister(p.seed);
            for (int i = 0; i < state.optimizer.numSamples; i++) {
                lBits.get(i).duration.put(p.name, rng.nextDouble());
                lBits.get(i).lateness.put(p.name, rng.nextDouble());
            }
        }

        return lBits;
    }

    private static Map<String, Integer> evaluateOne(
            State state, RandomBits bits) {
        Map<String, Integer> waitingTime = new HashMap<>();

        State newState = Simulator.simulateWithSampling(state, bits);
        for (Patient p : newState.patients) {
            waitingTime.put(p.name,
                        Math.max(p.stat.begin - Math.max(p.stat.arrival, p.appointment),0));
        }

        return waitingTime;
    }

    // NEVER evaluate the original state, always do it on a copy
    public static List<Map<String, Integer>> evaluate(State state) {
        State s = state.copy();
        List<Map<String, Integer>> ret = new ArrayList<>();
        List<RandomBits> lBits = generatelBits(s);
        for (RandomBits bit : lBits) {
            ret.add(evaluateOne(s, bit));
        }
        return ret;
    }

    public static Map<String, Integer> perPatientMedian(State state) {
        List<Map<String, Integer>> waits = evaluate(state);
        Map<String, Statistics> stat = new HashMap<>();
        for (Map<String, Integer> w : waits) {
            for (String name : w.keySet()) {
                if (!stat.containsKey(name)) {
                    stat.put(name, new Statistics());
                }
                stat.get(name).addValue(w.get(name));
            }
        }

        Map<String, Integer> ret = new HashMap<>();
        for (String name : stat.keySet()) {
            ret.put(name, (int)stat.get(name).getMedian());
        }
        return ret;
    }
}
