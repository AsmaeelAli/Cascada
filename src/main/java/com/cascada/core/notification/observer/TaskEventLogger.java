package com.cascada.core.notification.observer;

import com.cascada.core.common.event.TaskEventObserver;
import com.cascada.core.features.task.event.TaskEvent;

public class TaskEventLogger implements TaskEventObserver {

    @Override
    public void handle(TaskEvent event) {
        String message = switch (event) {
            case TaskEvent.Created e -> "Task created: " + e.task().getTitle();
            case TaskEvent.Broadcasted e -> "Task broadcasted: " + e.task().getTitle();
            case TaskEvent.Claimed e -> e.claimedBy().getName() + " claimed: " + e.task().getTitle();
            case TaskEvent.Completed e -> "Task completed: " + e.task().getTitle();
            case TaskEvent.Cancelled e -> "Task cancelled: " + e.task().getTitle();
        };
        System.out.println("[LOG] " + message);
    }
}