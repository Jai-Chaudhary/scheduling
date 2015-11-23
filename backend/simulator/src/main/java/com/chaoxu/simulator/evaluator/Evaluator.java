package com.chaoxu.simulator.evaluator;

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
import com.chaoxu.library.Horizon;
import com.chaoxu.simulator.Simulator;

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

    private static EvaluateResult evaluateOne(
            State state, RandomBits bits) {
        Map<String, Integer> waitingTime = new HashMap<>();

        State newState = Simulator.simulateWithSampling(state, bits);
        for (Patient p : newState.patients) {
            waitingTime.put(p.name,
                        Math.max(p.stat.begin - Math.max(p.stat.arrival, p.appointment),0));
        }

        Map<String, Integer> actualBegin = new HashMap<>();
        Map<String, Integer> actualEnd = new HashMap<>();

        for (Patient p : newState.patients) {
            if (actualBegin.get(p.site) == null || actualBegin.get(p.site) > p.stat.arrival) {
                actualBegin.put(p.site, p.stat.arrival);
            }

            if (actualEnd.get(p.site) == null || actualEnd.get(p.site) < p.stat.completion) {
                actualEnd.put(p.site, p.stat.completion);
            }
        }
        Map<String, Integer> overTime = new HashMap<>();
        for (String s : newState.sites.keySet()) {
            Horizon horizon = newState.sites.get(s).horizon;
            int ot = 0;
            if (actualBegin.get(s) != null && actualBegin.get(s) < horizon.begin) {
                ot += horizon.begin - actualBegin.get(s);
            }
            if (actualEnd.get(s) != null && actualEnd.get(s) > horizon.end) {
                ot += actualEnd.get(s) - horizon.end;
            }
            overTime.put(s, ot);
        }

        EvaluateResult res = new EvaluateResult();
        res.wait = waitingTime;
        res.overTime = overTime;

        return res;
    }

    public static List<EvaluateResult> evaluate(State state) {
        State s = state.copy();
        List<EvaluateResult> ret = new ArrayList<>();
        List<RandomBits> lBits = generatelBits(s);
        for (RandomBits bit : lBits) {
            ret.add(evaluateOne(s, bit));
        }
        return ret;
    }

    public static EvaluateMetric getMetric(List<EvaluateResult> results,
            State state) {
        Map<String, Statistics> waitStat = new HashMap<>();
        Map<String, Statistics> overTimeStat = new HashMap();

        for (EvaluateResult res : results) {
            Map<String, Integer> w = res.wait;
            for (String name : w.keySet()) {
                if (!waitStat.containsKey(name)) {
                    waitStat.put(name, new Statistics());
                }
                waitStat.get(name).addValue(w.get(name));
            }

            Map<String, Integer> o = res.overTime;
            for (String site : o.keySet()) {
                if (!overTimeStat.containsKey(site)) {
                    overTimeStat.put(site, new Statistics());
                }
                overTimeStat.get(site).addValue(o.get(site));
            }
        }

        Map<String, Integer> wait = new HashMap<>();
        for (String name : waitStat.keySet()) {
            wait.put(name, (int)waitStat.get(name).getMedian());
        }

        Map<String, Integer> overTime = new HashMap<>();
        for (String site : overTimeStat.keySet()) {
            overTime.put(site, (int)overTimeStat.get(site).getMedian());
        }

        Map<String, Statistics> siteStat = new HashMap<>();
        for (String s : state.sites.keySet()) {
            siteStat.put(s, new Statistics());
        }
        for (Patient p : state.patients)
            if (wait.containsKey(p.name)) {
                siteStat.get(p.site).addValue(wait.get(p.name));
            }

        Map<String, Double> siteWait = new HashMap<>();
        for (String s : state.sites.keySet()) {
            siteWait.put(s, siteStat.get(s).getMean());
        }

        EvaluateMetric metric = new EvaluateMetric();
        metric.wait = wait;
        metric.overTime = overTime;
        metric.siteWait = siteWait;
        return metric;
    }

    public static EvaluateMetric getMetric(State state) {
        return getMetric(evaluate(state), state);
    }
}
