package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class BeginEvent extends Event {
    private Patient p;
    private Runner r;
    private String m;

    public BeginEvent(Patient p, String m, Runner r) {
        super(r.state.time);
        this.p = p;
        this.r = r;
        this.m = m;
    }

    public void invoke() {
        p.stat.begin = time;
        p.machine = m;
        r.waitingRoom.get(p.site).remove(p);
        if (r.curPatient.get(p.site).get(m) != null) {
            throw new RuntimeException("begin patient on occupied machine!");
        }
        r.curPatient.get(p.site).put(m, p);

        r.eventPatients.add(p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p + " " + p.site + " " + m;
    }

    // Here we don't need to order BeginEvent with same
    // time because for each site we always pick the
    // patient with earliest appointment time
}
