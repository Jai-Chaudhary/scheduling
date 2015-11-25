package com.chaoxu.simulator.evaluator;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.RandomBits;
import com.chaoxu.library.Statistics;
import com.chaoxu.library.Horizon;
import com.chaoxu.simulator.Simulator;
import com.chaoxu.simulator.optimizer.Diversion;

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

    public static Map<String, Integer> getOverTime(State state) {
        Map<String, Integer> actualBegin = new HashMap<>();
        Map<String, Integer> actualEnd = new HashMap<>();

        for (Patient p : state.patients)
            if (p.status() == Patient.Status.Completed) {
                if (actualBegin.get(p.site) == null || actualBegin.get(p.site) > p.stat.arrival) {
                    actualBegin.put(p.site, p.stat.arrival);
                }

                if (actualEnd.get(p.site) == null || actualEnd.get(p.site) < p.stat.completion) {
                    actualEnd.put(p.site, p.stat.completion);
                }
            }

        Map<String, Integer> overTime = new HashMap<>();
        for (String s : state.sites.keySet()) {
            Horizon horizon = state.sites.get(s).horizon;
            int ot = 0;
            if (actualBegin.get(s) != null && actualBegin.get(s) < horizon.begin) {
                ot += horizon.begin - actualBegin.get(s);
            }
            if (actualEnd.get(s) != null && actualEnd.get(s) > horizon.end) {
                ot += actualEnd.get(s) - horizon.end;
            }
            overTime.put(s, ot);
        }
        return overTime;
    }

    private static EvaluateResult evaluateOne(
            State state, RandomBits bits) {
        Map<String, Integer> waitingTime = new HashMap<>();

        State newState = Simulator.simulateWithSampling(state, bits);
        for (Patient p : newState.patients) {
            waitingTime.put(p.name, p.getWaitingTime());
        }

        EvaluateResult res = new EvaluateResult();
        res.wait = waitingTime;
        res.overTime = getOverTime(newState);

        return res;
    }

    public static List<EvaluateResult> evaluate(State state) {
        State s = state.copy();
        List<RandomBits> lBits = generatelBits(s);
        List<EvaluateResult> ret = lBits.parallelStream()
            .map(bit -> evaluateOne(s, bit))
            .collect(Collectors.toList());
        return ret;
    }

    private static EvaluateMetric getMetric(List<EvaluateResult> results,
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

    public static EvaluateMetric getMetric(Diversion diversion,
            State state) {
        Patient patient = diversion.patient;
        String originalSite = patient.site;
        patient.site = diversion.site;
        EvaluateMetric res = getMetric(state);
        patient.site = originalSite;

        return res;
    }
}
