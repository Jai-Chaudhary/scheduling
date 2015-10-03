package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class BeginEvent extends Event {
    private Patient p;
    private Runner r;

    public BeginEvent(Patient p, Runner r) {
        super(r.state.time);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        p.begin = time;
        r.waitingRoom.get(p.site).remove(p);
        assert r.curPatient.get(p.site) == null;
        r.curPatient.put(p.site, p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }

    // Here we don't need to order BeginEvent with same
    // time because for each site we always pick the
    // patient with earliest appointment time
}
