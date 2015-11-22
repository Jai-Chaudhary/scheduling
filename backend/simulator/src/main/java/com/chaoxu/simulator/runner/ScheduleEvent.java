package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class ScheduleEvent extends Event {
    private Patient p;
    private Runner r;

    public ScheduleEvent(Patient p, Runner r) {
        super(p.secret.schedule);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        r.eventPatients.remove(p);

        p.stat.schedule = time;

        r.eventPatients.add(p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }
}
