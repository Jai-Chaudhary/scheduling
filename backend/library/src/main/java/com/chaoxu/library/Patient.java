package com.chaoxu.library;

public class Patient {
    // available to optimizer
    public String name;
    public String clazz;
    public int appointment;
    public int slot;
    public DiscreteDistribution durationDistribution;
    public DiscreteDistribution latenessDistribution;
    public boolean volunteer;
    public int seed;

    // status, available to optimizer
    public String site;
    public String machine;
    public Status status;
    public boolean optimized;

    public Stat stat;
    public Secret secret;

    public Patient copy() {
        Patient p = new Patient();

        p.name = name;
        p.clazz = clazz;
        p.appointment = appointment;
        p.slot = slot;
        p.durationDistribution = durationDistribution;
        p.latenessDistribution = latenessDistribution;
        p.volunteer = volunteer;
        p.seed = seed;

        p.site = site;
        p.machine = machine;
        p.status = status;
        p.optimized = optimized;

        p.stat = stat.copy();

        return p;
    }

    @Override
    public String toString() {
        return name;
    }

    public enum Status {
        Init,
        Scheduled,
        Canceled,
        Arrived,
        InProgress,
        Completed
    }

    public class Stat {
        public Integer schedule;
        public Integer arrival;
        public Integer begin;
        public Integer completion;
        public Integer cancel;

        public Double originalWait;
        public Double divertedWait;

        public String originalSite;

        public Stat copy() {
            Stat s = new Stat();

            s.schedule = schedule;
            s.arrival = arrival;
            s.begin = begin;
            s.completion = completion;
            s.cancel = cancel;

            s.originalWait = originalWait;
            s.divertedWait = divertedWait;

            s.originalSite = originalSite;

            return s;
        }
    }

    public class Secret {
        public Integer schedule;
        public Integer duration;
        public Integer lateness;
        public Integer cancel;
    }
}
