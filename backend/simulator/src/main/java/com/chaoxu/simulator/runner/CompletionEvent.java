package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;

public class CompletionEvent extends Event {
    private Patient p;
    private Runner r;

    public CompletionEvent(Patient p, Runner r) {
        super(p.begin + p.duration);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        p.completion = time;
        if (r.curPatient.get(p.site).get(p.machine) != p) {
            System.err.println(p);
            System.err.println(r.curPatient.get(p.site).get(p.machine));
            throw new RuntimeException("Wrong patient on machine");
        }
        r.curPatient.get(p.site).put(p.machine, null);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p + " " + p.site + " " + p.machine;
    }
}
