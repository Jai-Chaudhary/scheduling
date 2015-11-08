package com.chaoxu.library;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The State class contains all mutatble states
 * of the simulation. We removed the event
 * structure out of this class since events
 * are concern of simulator, not state or
 * configparser.
 */
public class State {
    // current time of the simulation
    public int time;
    // list of patients
    public List<Patient> patients;
    // number of sites
    public Map<String, List<String>> sites;
    public OptimizerSetting optimizer;

    // optimization is turned off during copying
    public State copy() {
        State s = new State();

        s.time = time;
        s.sites = sites;
        s.optimizer = optimizer.copy();
        s.optimizer.active = false;

        s.patients = new ArrayList<>();
        for (Patient p : patients) {
            if (p.status == Patient.Status.Scheduled ||
                p.status == Patient.Status.Arrived ||
                p.status == Patient.Status.InProgress ||
                p.status == Patient.Status.Completed) {
                s.patients.add(p.copy());
            }
        }

        return s;
    }
}
