package com.chaoxu.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.number.OrderingComparison.lessThan;
import org.junit.Test;
import org.junit.Before;

import java.util.Arrays;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.stream.IntStream;

public class DiscreteDistributionTest {

    RandomGenerator rng = new MersenneTwister(100);

    @Test
    public void testSample() {
        DiscreteDistribution dist = new DiscreteDistribution(
                3, Arrays.asList(new Double[]{.2, .3, .5}));
        int c3 = 0, c4 = 0, c5 = 0;
        for (int i = 0; i < 1000; i++) {
            int s = dist.sample(0, rng.nextDouble());
            if (s == 3) c3++;
            else if (s == 4) c4++;
            else c5++;
        }

        assertThat(Math.abs(c3 - 200), lessThan(10));
        assertThat(Math.abs(c4 - 300), lessThan(10));
        assertThat(Math.abs(c5 - 500), lessThan(10));
    }

    @Test
    public void testSample1() {
        DiscreteDistribution dist = new DiscreteDistribution(
                3, Arrays.asList(new Double[]{.2, .3, .5}));
        int c3 = 0, c4 = 0, c5 = 0;
        for (int i = 0; i < 1000; i++) {
            int s = dist.sample(4, rng.nextDouble());
            if (s == 3) c3++;
            else if (s == 4) c4++;
            else c5++;
        }

        assertEquals(c3, 0);
        assertThat(Math.abs(c4 - 375), lessThan(10));
        assertThat(Math.abs(c5 - 625), lessThan(10));
    }

    @Test
    public void testUniform() {
        DiscreteDistribution dist = DistributionParser.parse("uniform(3,5)");

        int c3 = 0, c4 = 0, c5 = 0;
        for (int i = 0; i < 900; i++) {
            int s = dist.sample(0, rng.nextDouble());
            if (s == 3) c3++;
            else if (s == 4) c4++;
            else c5++;
        }

        assertThat(Math.abs(c3 - 300), lessThan(10));
        assertThat(Math.abs(c4 - 300), lessThan(10));
        assertThat(Math.abs(c5 - 300), lessThan(10));

        assertEquals(dist.expectation(3), 4, 1e-6);
        assertEquals(dist.expectation(4), 4.5, 1e-6);
        assertEquals(dist.expectation(5), 5, 1e-6);
    }

    @Test
    public void testLognorm() {
        DiscreteDistribution dist = DistributionParser.parse("lognorm(4.03, .3, 30, 120)");

        assertThat(Math.abs(dist.expectation(120) - 120), lessThan(1e-6));
        assertThat(Math.abs(dist.expectation(30) - 58.529), lessThan(0.01));

        double avg = IntStream.range(0, 1000)
            .map(x -> dist.sample(0, rng.nextDouble())).average().getAsDouble();

        assertThat(Math.abs(avg - dist.expectation(30)), lessThan(.5));

        assertEquals(dist.expectation(30), dist.expectation(0), 1e-6);
    }
}
