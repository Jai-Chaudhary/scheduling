package com.chaoxu.configparser;

import java.util.List;
import java.util.Map;

import com.chaoxu.library.DiscreteDistribution;
import com.chaoxu.library.State;
import com.chaoxu.library.OptimizerSetting;
import com.chaoxu.library.SiteConfig;

public class Config {
    public int seed;
    public Map<String, SiteConfig> sites;
    public OptimizerSetting optimizer;
    public PatientConfig patient;
}

class PatientClass {
    public String name;
    public double percent;
    public DiscreteDistribution durationDistribution;
    public DiscreteDistribution latenessDistribution;
    public DiscreteDistribution slotOffsetDistribution;
    public Integer slot;
}

class PatientConfig {
    public double volunteerProbability;
    public double cancelProbability;
    public double SDAOPRate;
    public List<PatientClass> classes;
}
