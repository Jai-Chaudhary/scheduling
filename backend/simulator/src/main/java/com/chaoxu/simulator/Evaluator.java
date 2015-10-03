package com.chaoxu.simulator;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.RandomBits;

public class Evaluator {
    public static Map<String, Double> evaluate(
            State state, RandomBits bits) {
        Map<String, Double> waitingTime = new HashMap<>();

        State newState = Simulator.simulateWithSampling(state, bits);
        for (Patient p : newState.patients) {
            waitingTime.put(p.name, (double)(p.begin - p.arrival));
        }

        return waitingTime;
    }

    public static Map<String, Double> perPatientMean(State state,
            List<RandomBits> lBits) {

        Map<String, Double> means = new HashMap<>();
        for (Patient p : state.patients) {
            means.put(p.name, 0.0);
        }

        for (RandomBits bits : lBits) {
            Map<String, Double> waitingTime = evaluate(state, bits);
            for (String x : waitingTime.keySet()) {
                means.put(x, means.get(x) + waitingTime.get(x));
            }
        }

        for (String x : means.keySet()) {
            means.put(x, means.get(x) / lBits.size());
        }
        return means;
    }
}
