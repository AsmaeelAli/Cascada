package com.cascada.core.notification.observer;

import com.cascada.core.common.event.TaskEventObserver;
import com.cascada.core.features.task.event.TaskEvent;

public class ReminderCreationObserver implements TaskEventObserver {

    @Override
    public void handle(TaskEvent event) {
        if (event instanceof TaskEvent.Claimed claimed) {
            System.out.println("[REMINDER] Reminder will be created for claimed task: "
                    + claimed.task().getTitle());
        }
    }
}