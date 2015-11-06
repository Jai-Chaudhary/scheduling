package com.chaoxu.simulator.optimizer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.simulator.Evaluator;

class Result {
    public Statistics stat;
    public Statistics patientStat;
    public String site;
}

class Statistics {
    private List<Double> values = new ArrayList<>();
    public SummaryStatistics stat = new SummaryStatistics();

    public void add(double v) {
        values.add(v);
        stat.addValue(v);
    }

    public double ecdf(double x) {
        int c = 0;
        for (double v : values)
            if (v <= x)
                c++;
        return (double) c / values.size();
    }
}

public class Optimizer {

    public static Result bestSite(State state,
            Patient patient) {
        Objective objective = Objective.objFactory(state.objective);

        String originalSite = patient.site;
        Map<String, Statistics> objDiff = new HashMap<>();
        Map<String, Statistics> patientDiff = new HashMap<>();

        double originalWait = 0;

        for (String site : state.sites.keySet()) {
            if (!site.equals(originalSite)) {
                Statistics stat = new Statistics();
                Statistics patientStat = new Statistics();

                List<Map<String, Double>> waits = Evaluator.evaluate(state);

                patient.site = site;
                List<Map<String, Double>> newWaits = Evaluator.evaluate(state);
                patient.site = originalSite;

                originalWait = 0;

                for (int i = 0; i < waits.size(); i++) {
                    double obj = objective.value(waits.get(i));
                    double newObj = objective.value(newWaits.get(i));
                    stat.add(newObj - obj);
                    patientStat.add(newWaits.get(i).get(patient.name) - waits.get(i).get(patient.name));
                    originalWait += waits.get(i).get(patient.name);
                }
                originalWait /= waits.size();

                objDiff.put(site, stat);
                patientDiff.put(site, patientStat);
            }
        }

        Result best = new Result();
        best.site = patient.originalSite;
        best.stat = new Statistics();
        best.stat.add(0);

        for (String site : objDiff.keySet()) {
            Statistics stat = objDiff.get(site);
            if (stat.ecdf(0) < state.confidenceLevel) continue;

            Statistics patientStat = patientDiff.get(site);

            if (patientStat.ecdf(0) < state.patientConfidenceLevel)
                continue;

            if (stat.stat.getMean() < best.stat.stat.getMean()) {
                best.site = site;
                best.stat = stat;
                best.patientStat = patientStat;
            }
        }

        if (!best.site.equals(patient.site)) {
            patient.originalWait = originalWait;
            patient.divertedWait = originalWait + best.patientStat.stat.getMean();
        }

        return best;
    }

    public static String optimize(State state, Patient patient,
            boolean debug) {
        Result result = bestSite(state, patient);

        if (debug && !result.site.equals(patient.site)) {
            Statistics stat = result.stat;

            double confidenceLevel = 1.96;
            System.out.println();
            System.out.println(String.format(
                        "%s mean %f sd %f confidence %f",
                        patient, stat.stat.getMean(), stat.stat.getStandardDeviation(),
                        stat.ecdf(0)));

            stat = result.patientStat;
            System.out.println(String.format(
                        "%s mean %f sd %f confidence %f",
                        patient, stat.stat.getMean(), stat.stat.getStandardDeviation(),
                        stat.ecdf(0)));
            System.out.println();
        }

        return result.site;
    }
}
