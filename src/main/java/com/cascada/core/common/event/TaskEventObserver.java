package com.cascada.core.common.event;

import com.cascada.core.features.task.event.TaskEvent;

public interface TaskEventObserver {
    void handle(TaskEvent event);
}