package com.chaoxu.library;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Statistics {
    private List<Double> values = new ArrayList<>();
    private double sum = 0;

    public void addValue(double v) {
        values.add(v);
        sum += v;
    }

    public double getCumPct(double v) {
        int cnt = 0;
        for (double x : values)
            if (x < v) cnt += 1;
        return (double) cnt / values.size();
    }

    public double getMean() {
        return sum / values.size();
    }

    public double getQuantile(double q) {
        Collections.sort(values);
        return values.get((int)(values.size() * q));
    }

    public double getMedian() {
        return getQuantile(0.5);
    }
}
