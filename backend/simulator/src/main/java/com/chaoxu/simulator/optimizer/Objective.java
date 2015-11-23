package com.chaoxu.simulator.optimizer;

import java.util.Map;

import com.chaoxu.simulator.evaluator.EvaluateResult;

public class Objective {
    private String waitNorm;
    private double overTimeWeight;

    public Objective(String waitNorm, double overTimeWeight) {
        this.waitNorm = waitNorm;
        this.overTimeWeight = overTimeWeight;
    }

    public double value(EvaluateResult result) {
        double ret = 0;
        if (waitNorm.equals("l1")) {
            for (int x : result.wait.values()) {
                ret += x;
            }
        } else {
            for (int x : result.wait.values()) {
                ret += x*x;
            }
            ret = Math.sqrt(ret * result.wait.size());
        }

        int totalOverTime = 0;
        for (int ot : result.overTime.values()) {
            totalOverTime += ot;
        }

        ret += overTimeWeight * totalOverTime;
        return ret;
    }
}
