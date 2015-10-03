package com.chaoxu.simulator.optimizer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.RandomBits;
import com.chaoxu.simulator.Evaluator;

class Result {
    public SummaryStatistics stat;
    public int site;
}

public class Optimizer {
    private static final double confidenceLevel = 2.575;

    public static Result bestSite(State state,
            Patient patient, List<RandomBits> lBits) {
        Objective objective = Objective.objFactory(state.objective);

        int originalSite = patient.site;
        Map<Integer, SummaryStatistics> objDiff = new HashMap<>();

        for (int site = 0; site < state.sites; site++) {
            if (site != originalSite) {
                SummaryStatistics stat = new SummaryStatistics();
                for (RandomBits bits : lBits) {
                    double obj = objective.value(Evaluator.evaluate(state, bits));
                    patient.site = site;
                    double newObj = objective.value(Evaluator.evaluate(state, bits));
                    patient.site = originalSite;
                    stat.addValue(newObj - obj);
                }
                objDiff.put(site, stat);
            }
        }

        Result best = new Result();
        best.site = patient.originalSite;
        best.stat = new SummaryStatistics(){{
            addValue(0);
        }};

        for (int site : objDiff.keySet()) {
            SummaryStatistics stat = objDiff.get(site);
            double mean = stat.getMean();
            double delta = stat.getStandardDeviation() * confidenceLevel / Math.sqrt(stat.getN());
            if (mean + delta >= 0) continue;

            if (mean < best.stat.getMean()) {
                best.site = site;
                best.stat = stat;
            }
        }

        return best;
    }

    public static int optimize(State state, Patient patient,
            List<RandomBits> lBits) {
        Result result = bestSite(state, patient, lBits);

        if (result.site != patient.site) {
            SummaryStatistics stat = result.stat;

            System.out.println();
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
