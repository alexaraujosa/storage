package com.sd56.common.util.event;

@FunctionalInterface
public interface EventListener {
    void onEvent(Event event);
}
