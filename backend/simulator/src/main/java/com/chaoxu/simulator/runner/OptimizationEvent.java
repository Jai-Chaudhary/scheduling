package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;
import com.chaoxu.simulator.optimizer.Optimizer;

public class OptimizationEvent extends Event {
    private Patient p;
    private Runner r;

    public OptimizationEvent(Patient p, Runner r) {
        super(p.appointment - r.state.optimizer.advanceTime);
        this.p = p;
        this.r = r;
    }

    public void invoke() {
        r.eventPatients.remove(p);

        p.optimized = true;
        String newSite = Optimizer.optimize(r.state, p, r.debug);
        p.site = newSite;

        r.eventPatients.add(p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p;
    }
}
