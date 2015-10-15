package com.chaoxu.simulator.runner;

import com.chaoxu.library.Patient;
import com.chaoxu.simulator.optimizer.Optimizer;

public class OptimizationEvent extends Event {
    private Patient p;
    private Runner r;

    public OptimizationEvent(Patient p, Runner r) {
        super(p.appointment - r.state.advanceTime);
        this.p = p;
        this.r = r;
    }

    private boolean active() {
        return p.volunteer && r.state.optimization;
    }

    public void invoke() {
        p.optimized = time;
        if (active()) {
            String newSite = Optimizer.optimize(r.state, p);
            p.site = newSite;
        }
        r.patientsToOptimize.remove(p);
        r.patientsToArrive.add(p);
    }

    @Override
    public String toString() {
        return super.toString() + " " + p + " active: " + active();
    }
}
