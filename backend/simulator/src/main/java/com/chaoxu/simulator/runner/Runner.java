package com.chaoxu.simulator.runner;

import java.util.PriorityQueue;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;

import com.chaoxu.library.Patient;
import com.chaoxu.library.State;
import com.chaoxu.library.RandomBits;
import com.chaoxu.simulator.optimizer.Optimizer;

/**
 * Runner is to run simulation. For the design,
 * we didn't use EventQueue because it makes the current
 * state less interpretable, also it's harder to advance
 * state of simulation tick by tick, which is needed
 * for frontend.
 *
 * Runner contains all data structure to accelerate
 * simulation. So all events have reference to runner
 * so that they can make necessary changes. An alternative
 * is to put all these structures inside State, but that
 * future complicated the communication problem. Essentially,
 * everything here are just supporting structures, they are
 * not the most basic states. Thus it makes sense to
 * seperate states and the structure to support states.
 */
public class Runner {
    // set of patients to divert
    TreeSet<Patient> patientsToOptimize;
    // set of patients to arrive
    TreeSet<Patient> patientsToArrive;
    // patients in waiting room for each site
    Map<Integer, TreeSet<Patient>> waitingRoom;
    // current patient under processing of each site
    Map<Integer, Patient> curPatient =
        new HashMap<>();

    State state;
    List<RandomBits> lBits;

    private boolean debug;

    public Runner(State state, List<RandomBits> lBits, boolean debug) {

        this.state = state;
        this.lBits = lBits;
        this.debug = debug;

        patientsToOptimize = new TreeSet<>(
                (Patient p, Patient q) -> {
                    int ret = Integer.compare(
                            p.appointment - state.advanceTime,
                            q.appointment - state.advanceTime);
                    return ret != 0 ? ret : Integer.compare(p.id, q.id);
                });
        patientsToArrive = new TreeSet<>(
                (Patient p, Patient q) -> {
                    int ret = Integer.compare(
                            p.appointment + p.lateness,
                            q.appointment + q.lateness);
                    return ret != 0 ? ret : Integer.compare(p.id, q.id);
                });
        curPatient = new HashMap<>();
        waitingRoom = new HashMap<>();
        for (int s = 0; s < state.sites; s++) {
            waitingRoom.put(s, new TreeSet<Patient>(
                        (Patient p, Patient q) -> {
                            int ret = Integer.compare(
                                    p.appointment,
                                    q.appointment);
                            return ret != 0 ? ret : Integer.compare(p.id, q.id);
                        }));
        }

        for (Patient p : state.patients) {
            switch(p.status()) {
                case ToOptimize:
                    patientsToOptimize.add(p);
                    break;
                case ToArrive:
                    patientsToArrive.add(p);
                    break;
                case ToBegin:
                    waitingRoom.get(p.site).add(p);
                    break;
                case ToComplete:
                    assert curPatient.get(p.site) == null;
                    curPatient.put(p.site, p);
                    break;
            }
        }
    }

    /**
     * Return whether stoped because of stopTime.
     */
    public boolean run(Integer stopTime) {
        while(true) {
            Event e = nextEvent();
            if (e == null) {
                return false;
            }
            if (stopTime != null && e.time > stopTime) {
                state.time = stopTime;
                return true;
            }

            state.time = e.time;
            if (debug) {
                System.err.println(e);
            }
            e.invoke();
        }
    }

    private boolean isBusy(int s) {
        return curPatient.get(s) != null;
    }

    public Event nextEvent() {
        Event ret = null;

        // BeginEvent
        for (int s = 0; s < state.sites; s++) {
            if (!isBusy(s) && !waitingRoom.get(s).isEmpty()) {
                return new BeginEvent(waitingRoom.get(s).first(), this);
            }
        }

        // OptimizationEvent
        if (!patientsToOptimize.isEmpty()) {
            ret = minEvent(ret, new OptimizationEvent(
                        patientsToOptimize.first(), this));
        }
        // ArrivalEvent
        if (!patientsToArrive.isEmpty()) {
            ret = minEvent(ret, new ArrivalEvent(
                        patientsToArrive.first(), this));
        }
        // CompletionEvent
        for (int s = 0; s < state.sites; s++) {
            if (curPatient.get(s) != null) {
                ret = minEvent(ret, new CompletionEvent(
                            curPatient.get(s), this));
            }
        }

        return ret;
    }

    private Event minEvent(Event a, Event b) {
        if (a == null) return b;
        if (b == null) return a;
        if (a.compareTo(b) <= 0) {
            return a;
        }
        return b;
    }
}
