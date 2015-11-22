package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class ArrivalEvent extends Event {
    private Patient p;
    private Runner r;

    public ArrivalEvent(Patient p, Runner r) {
        super(p.appointment + p.secret.lateness);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        r.eventPatients.remove(p);

        p.stat.arrival = time;

        r.waitingRoom.get(p.site).add(p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }
}
