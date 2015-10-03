package com.chaoxu.simulator.runner;

import com.chaoxu.library.Util;

/**
 * Base event class. time is used to order events. When
 * constructing an event, make sure its time is at least
 * the current world time.
 *
 * All subclass needs to override invoke to specify concrete behaviors.
 */
public abstract class Event implements Comparable<Event>{
    public int time;

    public Event(int time) {
        this.time = time;
    }

    // Compare events with time
    @Override
    public int compareTo(Event e) {
        return Integer.compare(time, e.time);
    }

    /**
     * invoke specifies event's behavior. When event is pulled
     * from message queue, invoke will be called. After that,
     * this event will be discarded. Thus, all behaviors are
     * specified with this function.
     */
    public abstract void invoke();

    @Override
    public String toString() {
        return String.format("%s %s", Util.toTime(time), getClass().getSimpleName());
    }
}
