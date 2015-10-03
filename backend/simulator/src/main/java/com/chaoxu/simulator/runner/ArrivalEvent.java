package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class ArrivalEvent extends Event {
    private Patient p;
    private Runner r;

    public ArrivalEvent(Patient p, Runner r) {
        super(p.appointment + p.lateness);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        p.arrival = time;
        r.patientsToArrive.remove(p);
        r.waitingRoom.get(p.site).add(p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }
}
