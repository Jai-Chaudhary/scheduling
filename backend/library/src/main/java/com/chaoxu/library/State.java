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

    // whether optimization is enabled
    // originally I thought to add an argument
    // for runner to turn on/off optimization.
    // However then I need to pass this argument
    // from Client to Simulator, then to Runner.
    // Also, it's okay to think about this option
    // as current state of the system. As a result,
    // it's really easy for frontend to change this
    // behavior.
    public boolean optimization;
    // string representation of objective
    public String objective;
    // how many minutes in advance
    // to notify patients of potential change
    public int advanceTime;

    public int bitSeed;
    public int numSamples;

    public double confidenceLevel;
    public double patientConfidenceLevel;

    public State() {
    }

    public State(State s) {
        time = s.time;
        sites = s.sites;
        patients = new ArrayList<>();
        for (Patient p : s.patients) {
            patients.add(new Patient(p));
        }

        optimization = s.optimization;
        objective = s.objective;
        advanceTime = s.advanceTime;

        bitSeed = s.bitSeed;
        numSamples = s.numSamples;

        confidenceLevel = s.confidenceLevel;
        patientConfidenceLevel = s.patientConfidenceLevel;
    }
}
