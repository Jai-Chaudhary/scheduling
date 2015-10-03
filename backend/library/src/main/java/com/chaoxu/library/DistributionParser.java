package com.chaoxu.library;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.LogNormalDistribution;

class DistributionParser {
    public static DiscreteDistribution parse(String name) {
        String str = name.replaceAll("\\s", "");    // remove all spaces

        DiscreteDistribution ret;
        Scanner in = new Scanner(str);
        in.useDelimiter("[\\(,\\)]");
        in.next();

        if (str.startsWith("lognorm"))
            ret = lognorm(in.nextDouble(), in.nextDouble(), in.nextInt(), in.nextInt());
        else if (str.startsWith("uniform"))
            ret = uniform(in.nextInt(), in.nextInt());
        else
            throw new IllegalArgumentException();

        ret.name = name;

        return ret;
    }

    /**
     * Return a discretized lognorm distribution.
     */
    private static DiscreteDistribution lognorm(double mu, double sigma,
            int lb, int ub) {
        LogNormalDistribution dist = new LogNormalDistribution(mu, sigma);

        List<Double> pmf = new ArrayList<>();
        for (int i = lb; i <= ub; i++)
            pmf.add(dist.probability(i, i+1));

        return new DiscreteDistribution(lb, pmf);
    }

    /**
     * Return a discrete uniform distribution
     */
    private static DiscreteDistribution uniform(int lb, int ub) {
        return new DiscreteDistribution(lb,
                IntStream.range(0, ub - lb + 1).mapToObj(x -> 1.0)
                .collect(Collectors.toList()));
    }
}




