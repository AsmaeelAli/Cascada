package com.cascada.core.common.event;

import com.cascada.core.notification.observer.BroadcastNotifier;
import com.cascada.core.notification.observer.ReminderCreationObserver;
import com.cascada.core.notification.observer.TaskEventLogger;

public class EventSubscriptionConfig {

    private EventSubscriptionConfig() {}

    public static void registerAllObservers() {
        EventBus bus = EventBus.getInstance();
        bus.subscribe(new TaskEventLogger());
        bus.subscribe(new BroadcastNotifier());
        bus.subscribe(new ReminderCreationObserver());
    }
}