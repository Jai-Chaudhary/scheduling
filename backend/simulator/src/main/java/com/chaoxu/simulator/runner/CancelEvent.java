package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class CancelEvent extends Event {
    private Patient p;
    private Runner r;

    public CancelEvent(Patient p, Runner r) {
        super(p.secret.cancel);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        r.eventPatients.remove(p);

        if (p.status != Patient.Status.Scheduled) {
            throw new RuntimeException("Cancel non-scheduled patients!");
        }
        p.status = Patient.Status.Canceled;
        p.stat.cancel = time;
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }
}
