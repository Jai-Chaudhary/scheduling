package com.chaoxu.configparser;

import java.util.List;
import java.util.Map;

import com.chaoxu.library.DiscreteDistribution;

public class Config {
    public List<PatientClass> patientClasses;
    public Map<String, List<String>> sites;
    public Horizon horizon;
    public int seed;
    public OptimizerConfig optimizer;
}

class PatientClass {
    public String name;
    public double percent;
    public DiscreteDistribution durationDistribution;
    public DiscreteDistribution latenessDistribution;
    public DiscreteDistribution slotOffsetDistribution;
    public Integer slot;
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
