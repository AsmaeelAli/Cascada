package com.cascada.core.common.event;

import com.cascada.core.features.task.event.TaskEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private static final EventBus INSTANCE = new EventBus();

    private final List<TaskEventObserver> observers = new CopyOnWriteArrayList<>();
    //لانها ثريد سيفتي تذكر CopyOnWriteArrayList

    private EventBus() {}

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public void subscribe(TaskEventObserver observer) {
        observers.add(observer);
    }

    public void publish(TaskEvent event) {
        for (TaskEventObserver observer : observers) {
            observer.handle(event);
        }
    }
}
