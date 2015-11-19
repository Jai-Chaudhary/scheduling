package com.chaoxu.configparser;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.distribution.PoissonDistribution;

import com.chaoxu.library.State;
import com.chaoxu.library.Patient;
import com.chaoxu.library.DiscreteDistribution;
import com.chaoxu.library.SiteConfig;
import com.chaoxu.library.Horizon;
import com.chaoxu.library.Util;
import com.chaoxu.library.RandomBits;

class PatientGenerator {
    private DiscreteDistribution pcDist;
    private List<PatientClass> patientClasses;
    private RandomGenerator rng;

    public PatientGenerator(List<PatientClass> patientClasses,
            RandomGenerator rng) {
        List<Double> percent = new ArrayList<>();
        for (PatientClass pc : patientClasses) percent.add(pc.percent);
        pcDist = new DiscreteDistribution(0, percent);

        this.patientClasses = patientClasses;
        this.rng = rng;
    }

    // missing name, appointment, volunteer, cancel secret, schedule secret
    public Patient nextPatient(String site) {
        int pIndex = pcDist.sample(rng.nextDouble());
        PatientClass pc = patientClasses.get(pIndex);

        Patient p = new Patient();
        p.clazz = pc.name;

        int slot;
        if (pc.slot != null) {
            slot = pc.slot;
        } else {
            slot = pc.slotOffsetDistribution.sample(rng.nextDouble()) +
                (int)pc.durationDistribution.expectation();
        }
        p.slot = slot;

        p.durationDistribution = pc.durationDistribution;
        p.latenessDistribution = pc.latenessDistribution;
        p.seed = rng.nextInt();

        p.site = site;
        p.status = Patient.Status.Init;
        p.optimized = false;

        p.stat.originalSite = site;

        p.secret.duration = pc.durationDistribution.sample(rng.nextDouble());
        p.secret.lateness = pc.latenessDistribution.sample(rng.nextDouble());

        return p;
    }
}

public class ConfigParser {

    public static State parse(Config config) {
        RandomGenerator rng = new MersenneTwister(config.seed);

        State state = new State();
        state.time = 0;
        state.sites = config.sites;
        state.optimizer = config.optimizer;

        state.patients = buildPatients(config, rng);
        return state;
    }

    private static List<Patient> buildPatients(
            Config config,
            RandomGenerator rng) {
        PatientGenerator pg = new PatientGenerator(config.patient.classes, rng);

        List<Patient> ret = new ArrayList<>();

        // generate regular patients
        for (String s : config.sites.keySet()) {
            Horizon horizon = config.sites.get(s).horizon;
            for (String m : config.sites.get(s).machines) {
                int curTime = horizon.begin;
                while (curTime < horizon.end) {
                    Patient p = pg.nextPatient(s);
                    p.name = String.format("P%s-%s-%s", s, m, Util.toTime(curTime));
                    p.appointment = curTime;
                    if (rng.nextDouble() < config.patient.volunteerProbability) {
                        p.volunteer = true;
                    }
                    if (rng.nextDouble() < config.patient.cancelProbability) {
                        // TODO magic cancel offset number
                        p.secret.cancel = p.appointment - 120;
                    }
                    p.secret.schedule = 0;
                    ret.add(p);

                    curTime += p.slot;
                }
            }
        }

        // generate SDAOP
        for (String s : config.sites.keySet()) {
            Horizon horizon = config.sites.get(s).horizon;
            PoissonDistribution pd = new PoissonDistribution(rng,
                    config.patient.SDAOPRate * (horizon.end - horizon.begin) / 60,
                    PoissonDistribution.DEFAULT_EPSILON,
                    PoissonDistribution.DEFAULT_MAX_ITERATIONS);
            for (String m : config.sites.get(s).machines) {
                int num = pd.sample();

                for (int i = 0; i < num; i++) {
                    Patient p = pg.nextPatient(s);
                    p.name = String.format("SDAOP%s-%s-%d", s, m, i);
                    p.appointment = (int)(rng.nextDouble()
                        * (horizon.end - horizon.begin)
                        + horizon.begin);
                    p.volunteer = false;
                    p.secret.schedule = null;
                    p.secret.lateness = 0;
                    p.secret.cancel = null;
                    ret.add(p);
                }
            }
        }

        return ret;
    }
}
