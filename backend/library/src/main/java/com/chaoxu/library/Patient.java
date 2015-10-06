package com.chaoxu.library;

public class Patient {
    private static int nextId = 0;

    // available to optimizer
    public String name;
    public String clazz;
    public int appointment;
    public String originalSite;
    public DiscreteDistribution durationDistribution;
    public DiscreteDistribution latenessDistribution;
    public int id;

    // status, available to optimizer
    public String site;
    public String machine;
    public Integer arrival;
    public Integer begin;
    public Integer completion;

    // status related to optimizer
    public Integer optimized;   // time this patient is optimized
    public boolean volunteer;

    // not available to optimizer
    public int duration;
    public int lateness;

    public Patient() {
        id = nextId++;
    }

    public Patient(Patient p) {
        name = p.name;
        clazz = p.clazz;
        appointment = p.appointment;
        originalSite = p.originalSite;
        durationDistribution = p.durationDistribution;
        latenessDistribution = p.latenessDistribution;
        id = p.id;

        site = p.site;
        machine = p.machine;
        arrival = p.arrival;
        begin = p.begin;
        completion = p.completion;

        optimized = p.optimized;
        volunteer = p.volunteer;

        duration = p.duration;
        lateness = p.lateness;
    }

    @Override
    public String toString() {
        return name;
    }

    public PatientStatus status() {
        if (completion != null) return PatientStatus.Done;
        if (begin != null) return PatientStatus.ToComplete;
        if (arrival != null) return PatientStatus.ToBegin;
        if (optimized != null) return PatientStatus.ToArrive;
        return PatientStatus.ToOptimize;
    }

    public enum PatientStatus {
        ToOptimize,
        ToArrive,
        ToBegin,
        ToComplete,
        Done
    }
}
