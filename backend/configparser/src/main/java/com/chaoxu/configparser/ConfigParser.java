package com.chaoxu.configparser;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.DiscreteDistribution;
import com.chaoxu.library.Util;
import com.chaoxu.library.RandomBits;

public class ConfigParser {

    public static State parse(Config config) {
        RandomGenerator rng = new MersenneTwister(config.seed);

        State state = new State();
        state.time = 0;
        state.sites = config.sites;

        if (config.optimizer != null) {
            state.advanceTime = config.optimizer.advanceTime;
            state.optimization = config.optimizer.active;
        }

        state.patients = buildPatients(config.patientClasses, config, rng);
        state.objective = config.optimizer.objective;

        state.numSamples = config.optimizer.numSamples;
        state.bitSeed = rng.nextInt();

        state.confidenceLevel = config.optimizer.confidenceLevel;
        state.patientConfidenceLevel = config.optimizer.patientConfidenceLevel;

        return state;
    }

    private static List<Patient> buildPatients(
            List<PatientClass> patientClasses, Config config,
            RandomGenerator rng) {

        List<Double> percent = new ArrayList<>();
        for (PatientClass pc : patientClasses) percent.add(pc.percent);

        DiscreteDistribution pcDist = new DiscreteDistribution(0, percent);

        List<Patient> ret = new ArrayList<>();

        for (String s : config.sites.keySet()) {
            for (String m : config.sites.get(s)) {
                int curTime = config.horizon.begin;
                while (curTime < config.horizon.end) {
                    int pIndex = pcDist.sample(rng.nextDouble());
                    PatientClass pc = patientClasses.get(pIndex);

                    Patient p = new Patient();
                    p.name = String.format("P%s-%s-%s", s, m, Util.toTime(curTime));
                    p.clazz = pc.name;
                    p.appointment = curTime;
                    p.originalSite = s;
                    p.site = s;
                    p.durationDistribution = pc.durationDistribution;
                    p.latenessDistribution = pc.latenessDistribution;

                    p.duration = pc.durationDistribution.sample(rng.nextDouble());
                    p.lateness = pc.latenessDistribution.sample(rng.nextDouble());

                    p.volunteer = false;

                    if (config.optimizer != null &&
                            rng.nextDouble() < config.optimizer.volunteerProbability) {
                        // patient volunteer
                        p.volunteer = true;
                            }
                    ret.add(p);

                    // notice double is implicitly converted to int with += operator
                    curTime += pc.durationDistribution.expectation()
                        + pc.slotOffsetDistribution.sample(rng.nextDouble());
                }
            }
        }

        return ret;
    }
}
