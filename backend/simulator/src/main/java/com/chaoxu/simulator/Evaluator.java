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
        RandomGenerator rng = new MersenneTwister(state.bitSeed);

        List<RandomBits> lBits = new ArrayList<>();
        for (int i = 0; i < state.numSamples; i++) {
            RandomBits bits = new RandomBits();
            for (Patient p : state.patients) {
                bits.duration.put(p.name, rng.nextDouble());
                bits.lateness.put(p.name, rng.nextDouble());
            }
            lBits.add(bits);
        }

        return lBits;
    }

    private static Map<String, Double> evaluateOne(
            State state, RandomBits bits) {
        Map<String, Double> waitingTime = new HashMap<>();

        State newState = Simulator.simulateWithSampling(state, bits);
        for (Patient p : newState.patients) {
            waitingTime.put(p.name, (double)(p.begin - p.arrival));
        }

        return waitingTime;
    }

    public static List<Map<String, Double>> evaluate(State state) {
        List<Map<String, Double>> ret = new ArrayList<>();
        List<RandomBits> lBits = generatelBits(state);
        for (RandomBits bit : lBits) {
            ret.add(evaluateOne(state, bit));
        }
        return ret;
    }

    public static Map<String, Double> perPatientMean(State state) {

        Map<String, SummaryStatistics> stat = new HashMap<>();
        for (Patient p : state.patients) {
            stat.put(p.name, new SummaryStatistics());
        }

        List<Map<String, Double>> waits = evaluate(state);
        for (Map<String, Double> w : waits) {
            for (String name : w.keySet()) {
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
