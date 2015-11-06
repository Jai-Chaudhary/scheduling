package com.chaoxu.library;

public class Patient {
    // available to optimizer
    public String name;
    public String clazz;
    public int appointment;
    public int slot;
    public String originalSite;
    public DiscreteDistribution durationDistribution;
    public DiscreteDistribution latenessDistribution;

    // status, available to optimizer
    public String site;
    public String machine;
    public Integer arrival;
    public Integer begin;
    public Integer completion;

    public Double originalWait;
    public Double divertedWait;

    // status related to optimizer
    public Integer optimized;   // time this patient is optimized
    public boolean volunteer;

    // not available to optimizer
    public int duration;
    public int lateness;

    public Patient() {
    }

    public Patient(Patient p) {
        name = p.name;
        clazz = p.clazz;
        appointment = p.appointment;
        slot = p.slot;
        originalSite = p.originalSite;
        durationDistribution = p.durationDistribution;
        latenessDistribution = p.latenessDistribution;

        site = p.site;
        machine = p.machine;
        arrival = p.arrival;
        begin = p.begin;
        completion = p.completion;

        optimized = p.optimized;
        volunteer = p.volunteer;

        duration = p.duration;
        lateness = p.lateness;

        originalWait = p.originalWait;
        divertedWait = p.divertedWait;
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
