package com.chaoxu.simulator.runner;

import java.util.List;

import com.chaoxu.library.Patient;
import com.chaoxu.library.State;
import com.chaoxu.library.RandomBits;

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
            int newSite = Optimizer.optimize(r.state, p, r.lBits);
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
