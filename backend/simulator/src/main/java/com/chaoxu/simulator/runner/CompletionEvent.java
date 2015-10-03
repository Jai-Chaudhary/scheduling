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
        assert r.curPatient.get(p.site) == p;
        r.curPatient.put(p.site, null);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }
}
