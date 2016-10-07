package org.dsh.metrics;

public interface EventListener {
    public void onEvent(Event e);
    public int eventsBuffered();
}
