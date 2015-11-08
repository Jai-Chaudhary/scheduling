package com.chaoxu.library;

public class PatientStat {
    public String name;
    public String clazz;
    public int appointment;
    public int slot;
    public boolean volunteer;

    public String originalSite;
    public String site;
    public String machine;
    public Patient.Status status;

    public Integer schedule;
    public Integer arrival;
    public Integer begin;
    public Integer completion;
    public Integer cancel;

    public boolean optimized;
    public Double originalWait;
    public Double divertedWait;

    public PatientStat(Patient p) {
        name = p.name;
        clazz = p.clazz;
        appointment = p.appointment;
        slot = p.slot;
        volunteer = p.volunteer;

        originalSite = p.stat.originalSite;
        site = p.site;
        machine = p.machine;
        status = p.status;

        schedule = p.stat.schedule;
        arrival = p.stat.arrival;
        begin = p.stat.begin;
        completion = p.stat.completion;
        cancel = p.stat.cancel;

        optimized = p.optimized;
        originalWait = p.stat.originalWait;
        divertedWait = p.stat.divertedWait;
    }
}
