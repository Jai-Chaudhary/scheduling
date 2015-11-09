package com.chaoxu.simulator.optimizer;

import java.util.Map;

public class L1Obj extends Objective {
    public double value(Map<String, Integer> waitingTime) {
        double ret = 0;
        for (int x : waitingTime.values()) {
            ret += x;
        }
        return ret / waitingTime.size();
    }
}
