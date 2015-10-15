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
    public SummaryStatistics stat;
    public SummaryStatistics patientStat;
    public String site;
}

public class Optimizer {
    // TODO make this a configuration
    private static final double confidenceLevel = 2.575;

    public static Result bestSite(State state,
            Patient patient) {
        Objective objective = Objective.objFactory(state.objective);

        String originalSite = patient.site;
        Map<String, SummaryStatistics> objDiff = new HashMap<>();
        Map<String, SummaryStatistics> patientDiff = new HashMap<>();

        for (String site : state.sites.keySet()) {
            if (!site.equals(originalSite)) {
                SummaryStatistics stat = new SummaryStatistics();
                SummaryStatistics patientStat = new SummaryStatistics();

                List<Map<String, Double>> waits = Evaluator.evaluate(state);

                patient.site = site;
                List<Map<String, Double>> newWaits = Evaluator.evaluate(state);
                patient.site = originalSite;

                for (int i = 0; i < waits.size(); i++) {
                    double obj = objective.value(waits.get(i));
                    double newObj = objective.value(newWaits.get(i));
                    stat.addValue(newObj - obj);

                    patientStat.addValue(newWaits.get(i).get(patient.name) - waits.get(i).get(patient.name));
                }

                objDiff.put(site, stat);
                patientDiff.put(site, patientStat);
            }
        }

        Result best = new Result();
        best.site = patient.originalSite;
        best.stat = new SummaryStatistics(){{
            addValue(0);
        }};

        for (String site : objDiff.keySet()) {
            SummaryStatistics stat = objDiff.get(site);
            double mean = stat.getMean();
            double delta = stat.getStandardDeviation() * confidenceLevel / Math.sqrt(stat.getN());
            if (mean + delta >= 0) continue;

            SummaryStatistics patientStat = patientDiff.get(site);
            // make sure the patient in question is better off
            // TODO make this a config
            if (patientStat.getMean() +
                    patientStat.getStandardDeviation() * confidenceLevel
                    / Math.sqrt(patientStat.getN()) >= 0) continue;

            if (mean < best.stat.getMean()) {
                best.site = site;
                best.stat = stat;
                best.patientStat = patientStat;
            }
        }

        return best;
    }

    public static String optimize(State state, Patient patient) {
        Result result = bestSite(state, patient);

        if (!result.site.equals(patient.site)) {
            SummaryStatistics stat = result.stat;

            System.out.println();
            System.out.println(String.format(
                        "%s mean %f sd %f delta %f",
                        patient, stat.getMean(), stat.getStandardDeviation(),
                        stat.getStandardDeviation() * confidenceLevel / Math.sqrt(stat.getN())));

            stat = result.patientStat;
            System.out.println(String.format(
                        "%s mean %f sd %f delta %f",
                        patient, stat.getMean(), stat.getStandardDeviation(),
                        stat.getStandardDeviation() * confidenceLevel / Math.sqrt(stat.getN())));
            System.out.println();
        }

        /*
        for (Patient p : state.patients)
            if (p.volunteer && p.status() == Patient.PatientStatus.ToOptimize) {
                Result r = bestSite(state, p, lBits);
                SummaryStatistics stat = r.stat;
                if (r.site != p.site) {
                    System.out.println(String.format(
                                "%s mean %f sd %f delta %f",
                                p, stat.getMean(), stat.getStandardDeviation(),
                                stat.getStandardDeviation() * confidenceLevel / Math.sqrt(stat.getN())));
                }
            }
            */

        return result.site;
    }

    private static double getAvgWait(Map<String, Double> waitStat) {
        double ret = 0;
        for (String name : waitStat.keySet()) {
            ret += waitStat.get(name);
        }
        ret /= waitStat.size();
        return ret;
    }
}
