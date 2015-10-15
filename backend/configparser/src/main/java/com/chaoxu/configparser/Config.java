package com.chaoxu.configparser;

import java.util.List;
import java.util.Map;

public class Config {
    public List<PatientClassConfig> patientClasses;
    public Map<String, List<String>> sites;
    public Horizon horizon;
    public int seed;
    public OptimizerConfig optimizer;
}

class PatientClassConfig {
    public String name;
    public double percent;
    public String durationDistribution;
    public String slotOffsetDistribution;
    public String latenessDistribution;
}

class OptimizerConfig {
    public boolean active;
    public int advanceTime;
    public double volunteerProbability;
    public String objective;
    public double confidenceLevel;
    public double patientConfidenceLevel;
    public int numSamples;
}

class Horizon {
    public int begin;
    public int end;
}
