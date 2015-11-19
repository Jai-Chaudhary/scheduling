package com.chaoxu.library;

public class OptimizerSetting {
    // whether optimization is enabled
    // originally I thought to add an argument
    // for runner to turn on/off optimization.
    // However then I need to pass this argument
    // from Client to Simulator, then to Runner.
    // Also, it's okay to think about this option
    // as current state of the system. As a result,
    // it's really easy for frontend to change this
    // behavior.
    public boolean active;
    // how many minutes in advance
    // to notify patients of potential change
    public int advanceTime;
    public ObjectiveSetting objective;
    public double confidenceLevel;
    public double patientConfidenceLevel;
    public int numSamples;

    public OptimizerSetting copy() {
        OptimizerSetting o = new OptimizerSetting();
        o.active = active;
        o.advanceTime = advanceTime;
        o.objective = objective;
        o.confidenceLevel = confidenceLevel;
        o.patientConfidenceLevel = patientConfidenceLevel;
        o.numSamples = numSamples;
        return o;
    }
}
