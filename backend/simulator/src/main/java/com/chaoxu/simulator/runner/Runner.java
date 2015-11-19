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
    TreeSet<Patient> eventPatients;
    Map<String, TreeSet<Patient>> waitingRoom;
    // current patient under processing of each site
    Map<String, Map<String, Patient>> curPatient;

    State state;

    public boolean debug;

    public Runner(State state, boolean debug) {
        this.state = state;
        this.debug = debug;

        eventPatients = new TreeSet<>(
                (Patient p, Patient q) -> {
                    int ret = Integer.compare(
                            nextEvent(p).time,
                            nextEvent(q).time);
                    return ret != 0 ? ret : p.name.compareTo(q.name);
                });

        curPatient = new HashMap<>();
        for (String s : state.sites.keySet()) {
            curPatient.put(s, new HashMap<>());
            for (String m : state.sites.get(s).machines) {
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
            switch(p.status) {
                case Init:
                    eventPatients.add(p);
                    break;
                case Scheduled:
                    eventPatients.add(p);
                    break;
                case Arrived:
                    waitingRoom.get(p.site).add(p);
                    break;
                case InProgress:
                    eventPatients.add(p);
                    if (curPatient.get(p.site).get(p.machine) != null) {
                        throw new RuntimeException("Machine not idling");
                    }
                    curPatient.get(p.site).put(p.machine, p);
                    break;
                case Completed:
                    break;
                case Canceled:
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
        // BeginEvent
        for (String s : state.sites.keySet()) {
            for (String m : state.sites.get(s).machines) {
                if (!isBusy(s, m) && !waitingRoom.get(s).isEmpty()) {
                    Patient p = waitingRoom.get(s).first();
                    return new BeginEvent(p, m, this);
                }
            }
        }

        if (eventPatients.isEmpty())
            return null;

        return nextEvent(eventPatients.first());
    }

    private Event nextEvent(Patient p) {
        if (p.status == Patient.Status.Init) {
            if (p.secret.schedule != null) {
                return new ScheduleEvent(p, this);
            } else if (p.secret.lateness != null) {
                return new ArrivalEvent(p, this);
            } else {
                throw new RuntimeException("Init patient has nothing to do");
            }
        }
        if (p.status == Patient.Status.Scheduled) {
            Event e = null;
            if (p.volunteer && !p.optimized && state.optimizer.active) {
                e = new OptimizationEvent(p, this);
            }
            if (p.secret.cancel != null) {
                e = minEvent(e, new CancelEvent(p, this));
            } else if (p.secret.lateness != null) {
                e = minEvent(e, new ArrivalEvent(p, this));
            } else {
                throw new RuntimeException("Scheduled patient has nothing to do");
            }
            return e;
        }
        if (p.status == Patient.Status.Arrived) {
            throw new RuntimeException("nextEvent on Arrived patient!");
        }
        if (p.status == Patient.Status.InProgress) {
            return new CompletionEvent(p, this);
        }
        if (p.status == Patient.Status.Completed) {
            throw new RuntimeException("nextEvent on Completed patient!");
        }
        if (p.status == Patient.Status.Canceled) {
            throw new RuntimeException("nextEvent on Canceled patient!");
        }
        throw new RuntimeException("Impossible");
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
