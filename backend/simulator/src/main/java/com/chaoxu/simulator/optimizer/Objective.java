package com.chaoxu.simulator.optimizer;

import java.util.Map;

public abstract class Objective {
    public abstract double value(Map<String, Integer> waitingTime);

    public static Objective objFactory(String s) {
        if (s.equals("l1")) {
            return new L1Obj();
        }
        if (s.equals("l2")) {
            return new L2Obj();
        }
        throw new IllegalArgumentException();
    }
}
