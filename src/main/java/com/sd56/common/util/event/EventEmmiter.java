package com.sd56.common.util.event;

import com.sd56.common.util.LockedResource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class EventEmmiter {
    private final LockedResource<Map<String, Queue<EventListener>>, ?> listeners;

    public EventEmmiter() {
        this.listeners = new LockedResource<>(new HashMap<>());
    }

    public void once(String eventName, EventListener listener) {
        this.addListener(eventName, new EventListener() {
            @Override
            public void onEvent(Event event) {
                listener.onEvent(event);
                removeListener(eventName, this);
            }
        });
    }

    public void addListener(String eventName, EventListener listener) {
        this.listeners.autoExec((lr) -> {
            if (!lr.getResource().containsKey(eventName)) {
                lr.getResource().put(eventName, new LinkedList<>());
            }

            lr.getResource().get(eventName).add(listener);

            return null;
        });
    }

    public void removeListener(String eventName, EventListener listener) {
        this.listeners.autoExec((lr) -> {
            if (!lr.getResource().containsKey(eventName)) return null;

            lr.getResource().get(eventName).remove(listener);
            return null;
        });
    }

    public void emit(Event event) {
        this.listeners.autoExec((lr) -> {
            for (EventListener listener : lr.getResource().get(event.name())) {
                listener.onEvent(event);
            }
            return null;
        });
    }
}
