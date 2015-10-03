package com.chaoxu.configparser;

import java.util.List;

public class Config {
    public List<PatientClassConfig> patientClasses;
    public int sites;
    public Horizon horizon;
    public int seed;
    public OptimizerConfig optimizer;
    public int numSamples;
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
}

class Horizon {
    public int begin;
    public int end;
}
