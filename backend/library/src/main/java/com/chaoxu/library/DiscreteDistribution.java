package com.chaoxu.library;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Discrete distribution. Support is [base, base + pmf.size() - 1].
 *
 * We implement this because RealDistribution in Apache doesn't
 * support convolution.
 */
public class DiscreteDistribution {
    String name;
    private int base;

    // cdf[i] is the probability of <= i
    private List<Double> cdf = new ArrayList<>();
    // partialSum[i] is sum of k * pmf[k] for k >= i
    private List<Double> partialSum = new ArrayList<>();

    @JsonCreator
    public static DiscreteDistribution parse(String name) {
        return DistributionParser.parse(name);
    }
    /**
     * Constructor. We will normalize pmf and generate cdf.
     * suggestedMean is mainly used as slot size when
     * making appointments.
     */
    public DiscreteDistribution(int base, List<Double> pmf) {
        this.base = base;

        double sum = pmf.stream().mapToDouble(x -> x).sum();
        pmf = pmf.stream().mapToDouble(x -> x / sum)
            .boxed().collect(Collectors.toList());

        double tot = 0;
        for (double x : pmf) {
            tot += x;
            cdf.add(tot);
        }

        double psum = 0;
        for (int i = pmf.size() - 1; i >= 0; i--) {
            psum += pmf.get(i) * i;
            partialSum.add(psum);
        }
        Collections.reverse(partialSum);
    }

    /**
     * Sample condition on >= lb using u as the
     * uniform random variable.
     *
     * By making u an explicit input, we control
     * all randomness.
     */
    public int sample(int lb, double u) {
        int x = Math.max(lb - base ,0);
        double y = x == 0 ? 0 : cdf.get(x-1);

        double q = u * (1-y) + y;
        int i = Collections.binarySearch(cdf, q);
        if (i < 0) i = -(i+1);
        return base + i;
    }

    public int sample(double u) {
        return sample(base, u);
    }

    /**
     * Return the conditional mean of >= lb
     */
    public double expectation(int lb) {
        int x = Math.max(lb - base, 0);
        return partialSum.get(x) / (1 - (x == 0 ? 0 : cdf.get(x-1))) + base;
    }

    public double expectation() {
        return expectation(base);
    }

    @JsonValue
    public String toString() {
        return name;
    }
}
