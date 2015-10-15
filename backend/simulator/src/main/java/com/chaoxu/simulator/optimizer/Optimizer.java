package com.chaoxu.simulator.optimizer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.distribution.NormalDistribution;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.simulator.Evaluator;

class Result {
    public NormalDistribution stat;
    public NormalDistribution patientStat;
    public String site;
}

public class Optimizer {

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
        best.stat = new NormalDistribution(0,1);

        for (String site : objDiff.keySet()) {
            SummaryStatistics stat = objDiff.get(site);
            NormalDistribution norm = new NormalDistribution(
                    stat.getMean(), stat.getStandardDeviation());
            if (norm.cumulativeProbability(0) < state.confidenceLevel) continue;

            SummaryStatistics patientStat = patientDiff.get(site);
            NormalDistribution patientNorm = new NormalDistribution(
                    patientStat.getMean(), patientStat.getStandardDeviation());

            if (patientNorm.cumulativeProbability(0) < state.patientConfidenceLevel)
                continue;

            if (stat.getMean() < best.stat.getMean()) {
                best.site = site;
                best.stat = norm;
                best.patientStat = patientNorm;
            }
        }

        return best;
    }

    public static String optimize(State state, Patient patient,
            boolean debug) {
        Result result = bestSite(state, patient);

        if (debug && !result.site.equals(patient.site)) {
            NormalDistribution stat = result.stat;

            double confidenceLevel = 1.96;
            System.out.println();
            System.out.println(String.format(
                        "%s mean %f sd %f confidence %f",
                        patient, stat.getMean(), stat.getStandardDeviation(),
                        stat.cumulativeProbability(0)));

            stat = result.patientStat;
            System.out.println(String.format(
                        "%s mean %f sd %f confidence %f",
                        patient, stat.getMean(), stat.getStandardDeviation(),
                        stat.cumulativeProbability(0)));
            System.out.println();
        }

        return result.site;
    }
}
