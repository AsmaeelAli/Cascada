package com.cascada.core.notification.observer;

import com.cascada.core.common.event.TaskEventObserver;
import com.cascada.core.domain.enums.Priority;
import com.cascada.core.features.task.event.TaskEvent;

public class BroadcastNotifier implements TaskEventObserver {

    @Override
    public void handle(TaskEvent event) {
        if (event instanceof TaskEvent.Broadcasted broadcasted) {
            System.out.println("[BROADCAST] New task available for anyone to claim: "
                    + broadcasted.task().getTitle());
        }

        if (event instanceof TaskEvent.Completed completed
                && completed.task().getPriority() == Priority.HIGH) {
            System.out.println("[BROADCAST] High-priority task completed: "
                    + completed.task().getTitle());
        }

        if (event instanceof TaskEvent.Cancelled cancelled
                && cancelled.task().getPriority() == Priority.HIGH) {
            System.out.println("[BROADCAST] High-priority task cancelled: "
                    + cancelled.task().getTitle());
        }
    }
}
