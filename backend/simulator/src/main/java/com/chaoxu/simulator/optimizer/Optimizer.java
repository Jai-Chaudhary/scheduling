package com.chaoxu.simulator.optimizer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.Statistics;
import com.chaoxu.simulator.Evaluator;

class Result {
    public double objQuantile;
    public double patientQuantile;
    public double originalWait;
    public double divertedWait;
    public String site;
}

public class Optimizer {

    private static double calMeanWait(List<Map<String, Integer>> waits, String name) {
        double ret = 0;
        for (Map<String, Integer> w : waits) ret += w.get(name);
        return ret / waits.size();
    }

    public static Result bestSite(State state,
            Patient patient) {
        Objective objective = Objective.objFactory(state.optimizer.objective);
        Result result = null;

        String originalSite = patient.site;
        List<Map<String, Integer>> waits = Evaluator.evaluate(state);
        double originalWait = calMeanWait(waits, patient.name);

        for (String site : state.sites.keySet()) {
            if (!site.equals(originalSite)) {
                Statistics objStat = new Statistics();
                Statistics patientStat = new Statistics();

                patient.site = site;
                List<Map<String, Integer>> newWaits = Evaluator.evaluate(state);
                patient.site = originalSite;

                for (int i = 0; i < waits.size(); i++) {
                    double obj = objective.value(waits.get(i));
                    double newObj = objective.value(newWaits.get(i));
                    objStat.addValue(newObj - obj);
                    patientStat.addValue(newWaits.get(i).get(patient.name) - waits.get(i).get(patient.name));
                }

                double objQuantile = objStat.getCumPct(0);
                double patientQuantile = patientStat.getCumPct(0);
                if (objQuantile >= state.optimizer.confidenceLevel
                        && patientQuantile >= state.optimizer.patientConfidenceLevel) {
                    if (result == null || result.objQuantile < objQuantile) {
                        Result r = new Result();
                        r.objQuantile = objQuantile;
                        r.patientQuantile = patientQuantile;
                        r.originalWait = originalWait;
                        r.divertedWait = calMeanWait(newWaits, patient.name);
                        r.site = site;

                        result = r;
                    }
                }
            }
        }

        return result;
    }

    public static String optimize(State state, Patient patient,
            boolean debug) {
        Result result = bestSite(state, patient);

        if (result != null) {
            if (debug) {
                System.out.println(String.format(
                            "Diversion: %s from %s to %s, obj %f, patient %f",
                            patient, patient.site, result.site,
                            result.objQuantile, result.patientQuantile));
            }

            patient.stat.originalWait = result.originalWait;
            patient.stat.divertedWait = result.divertedWait;
            return result.site;
        }

        return patient.site;
    }
}
