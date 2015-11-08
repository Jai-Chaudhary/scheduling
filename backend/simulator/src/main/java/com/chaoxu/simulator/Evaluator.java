package com.chaoxu.simulator;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.RandomBits;

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

    private static Map<String, Double> evaluateOne(
            State state, RandomBits bits) {
        Map<String, Double> waitingTime = new HashMap<>();

        State newState = Simulator.simulateWithSampling(state, bits);
        for (Patient p : newState.patients) {
            waitingTime.put(p.name, (double)(
                        Math.max(p.stat.begin - Math.max(p.stat.arrival, p.appointment),0)));
        }

        return waitingTime;
    }

    // NEVER evaluate the original state, always do it on a copy
    public static List<Map<String, Double>> evaluate(State state) {
        State s = state.copy();
        List<Map<String, Double>> ret = new ArrayList<>();
        List<RandomBits> lBits = generatelBits(s);
        for (RandomBits bit : lBits) {
            ret.add(evaluateOne(s, bit));
        }
        return ret;
    }

    public static Map<String, Double> perPatientMean(State state) {
        Map<String, SummaryStatistics> stat = new HashMap<>();

        List<Map<String, Double>> waits = evaluate(state);
        for (Map<String, Double> w : waits) {
            for (String name : w.keySet()) {
                if (!stat.containsKey(name)) {
                    stat.put(name, new SummaryStatistics());
                }
                stat.get(name).addValue(w.get(name));
            }
        }

        Map<String, Double> ret = new HashMap<>();
        for (String name : stat.keySet()) {
            ret.put(name, stat.get(name).getMean());
        }
        return ret;
    }
}
