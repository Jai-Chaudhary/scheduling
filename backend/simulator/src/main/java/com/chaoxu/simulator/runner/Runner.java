package com.chaoxu.simulator.runner;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
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
    Map<String, TreeSet<Patient>> waitingRoom;
    // current patient under processing of each site
    Map<String, Map<String, Patient>> curPatient =
        new HashMap<>();

    State state;

    private boolean debug;


    public Runner(State state, boolean debug) {
        this.state = state;
        this.debug = debug;

        patientsToOptimize = new TreeSet<>(
                (Patient p, Patient q) -> {
                    int ret = Integer.compare(
                            p.appointment - state.advanceTime,
                            q.appointment - state.advanceTime);
                    return ret != 0 ? ret : p.name.compareTo(q.name);
                });
        patientsToArrive = new TreeSet<>(
                (Patient p, Patient q) -> {
                    int ret = Integer.compare(
                            p.appointment + p.lateness,
                            q.appointment + q.lateness);
                    return ret != 0 ? ret : p.name.compareTo(q.name);
                });
        curPatient = new HashMap<>();
        for (String s : state.sites.keySet()) {
            curPatient.put(s, new HashMap<>());
            for (String m : state.sites.get(s)) {
                curPatient.get(s).put(m, null);
            }
        }

        waitingRoom = new HashMap<>();
        for (String s : state.sites.keySet()) {
            waitingRoom.put(s, new TreeSet<Patient>(
                        (Patient p, Patient q) -> {
                            int ret = Integer.compare(
                                    p.appointment,
                                    q.appointment);
                            return ret != 0 ? ret : p.name.compareTo(q.name);
                        }));
        }

        for (Patient p : state.patients) {
            switch(p.status()) {
                case ToOptimize:
                    if (p.lateness <= -state.advanceTime) {
                        p.optimized = p.appointment + p.lateness;
                        patientsToArrive.add(p);
                    } else {
                        patientsToOptimize.add(p);
                    }
                    break;
                case ToArrive:
                    patientsToArrive.add(p);
                    break;
                case ToBegin:
                    waitingRoom.get(p.site).add(p);
                    break;
                case ToComplete:
                    if (curPatient.get(p.site).get(p.machine) != null) {
                        throw new RuntimeException("Machine not idling");
                    }
                    curPatient.get(p.site).put(p.machine, p);
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

            if (debug) {
                System.err.println(e);
            }
            if (e.time < state.time) {
                throw new RuntimeException("backward event");
            }
            state.time = e.time;
            e.invoke();
        }
    }

    private boolean isBusy(String s, String m) {
        return curPatient.get(s).get(m) != null;
    }

    public Event nextEvent() {
        Event ret = null;

        // BeginEvent
        for (String s : state.sites.keySet()) {
            for (String m : state.sites.get(s)) {
                if (!isBusy(s, m) && !waitingRoom.get(s).isEmpty()) {
                    Patient p = waitingRoom.get(s).first();
                    return new BeginEvent(p, m, this);
                }
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
        for (String s : state.sites.keySet()) {
            for (String m : state.sites.get(s)) {
                if (curPatient.get(s).get(m) != null) {
                    ret = minEvent(ret, new CompletionEvent(
                                curPatient.get(s).get(m), this));
                }
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
