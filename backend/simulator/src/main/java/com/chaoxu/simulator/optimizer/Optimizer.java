package com.chaoxu.simulator.optimizer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.Statistics;
import com.chaoxu.simulator.evaluator.Evaluator;
import com.chaoxu.simulator.evaluator.EvaluateResult;


public class Optimizer {

    private static double calMeanWait(List<EvaluateResult> results, String name) {
        double ret = 0;
        for (EvaluateResult res : results) ret += res.wait.get(name);
        return ret / results.size();
    }

    private static DiversionQuality getQuality(List<EvaluateResult> results,
            List<EvaluateResult> newResults,
            Objective objective, Patient patient) {
        DiversionQuality res = new DiversionQuality();

        res.originalWait = calMeanWait(results, patient.name);
        res.divertedWait = calMeanWait(newResults, patient.name);

        Statistics objStat = new Statistics();
        Statistics patientStat = new Statistics();
        for (int i = 0; i < results.size(); i++) {
            double obj = objective.value(results.get(i));
            double newObj = objective.value(newResults.get(i));
            objStat.addValue(newObj - obj);
            patientStat.addValue(newResults.get(i).wait.get(patient.name) - results.get(i).wait.get(patient.name));
        }

        res.objQuantile = objStat.getCumPct(0);
        res.patientQuantile = patientStat.getCumPct(0);

        return res;
    }

    public static Map<Diversion, DiversionQuality> getQualities(
            State state, List<Diversion> diversions) {
        Map<Diversion, DiversionQuality> res = new HashMap<>();

        List<EvaluateResult> results = Evaluator.evaluate(state);
        Objective objective = new Objective(state.optimizer.objective.waitNorm,
                state.optimizer.objective.overTimeWeight);

        for (Diversion diversion : diversions) {
            Patient patient = diversion.patient;
            String originalSite = patient.site;

            patient.site = diversion.site;
            List<EvaluateResult> newResults = Evaluator.evaluate(state);
            patient.site = originalSite;

            DiversionQuality q = getQuality(results, newResults,
                    objective, patient);
            if (q.objQuantile >= state.optimizer.confidenceLevel &&
                    q.patientQuantile >= state.optimizer.patientConfidenceLevel) {
                res.put(diversion, q);
            }
        }

        return res;
    }

    public static String optimize(State state, Patient patient,
            boolean debug) {
        List<Diversion> diversions = new ArrayList<>();
        for (String site : state.sites.keySet()) {
            if (!site.equals(patient.site)) {
                Diversion diversion = new Diversion();
                diversion.patient = patient;
                diversion.site = site;
                diversions.add(diversion);
            }
        }

        Map<Diversion, DiversionQuality> qualities = getQualities(
                state, diversions);

        String bestSite = null;
        DiversionQuality bestQuality = null;
        for (Diversion diversion : qualities.keySet()) {
            DiversionQuality q = qualities.get(diversion);
            if (bestQuality == null || bestQuality.objQuantile < q.objQuantile) {
                bestQuality = q;
                bestSite = diversion.site;
            }
        }

        if (bestQuality != null) {
            if (debug) {
                System.out.println(String.format(
                            "Diversion: %s from %s to %s, obj %f, patient %f",
                            patient, patient.site, bestSite,
                            bestQuality.objQuantile, bestQuality.patientQuantile));
            }

            patient.stat.originalWait = bestQuality.originalWait;
            patient.stat.divertedWait = bestQuality.divertedWait;
            return bestSite;
        }

        return patient.site;
    }
}
