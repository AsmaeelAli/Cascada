package com.cascada.core.features.task.states;

import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.domain.enums.TaskStatus;
import com.cascada.core.domain.exception.InvalidTaskStateException;
import java.time.LocalDateTime;

public class CanceledState implements TaskState {

    @Override
    public TaskState markInProgress(TaskEntity task) {
        throw new InvalidTaskStateException("Cannot reopen a CANCELED task");
    }

    @Override
    public TaskState markDone(TaskEntity task) {
        throw new InvalidTaskStateException("Cannot complete a CANCELED task");
    }

    @Override
    public TaskState cancel(TaskEntity task) {
        throw new InvalidTaskStateException("Task is already CANCELED");
    }

    @Override
    public TaskState markOverdue(TaskEntity task) {
        throw new InvalidTaskStateException("Cannot mark a CANCELED task as OVERDUE");
    }

    @Override
    public TaskState postpone(TaskEntity task, LocalDateTime newDueDate) {
        throw new InvalidTaskStateException("Cannot postpone a CANCELED task");
    }

    @Override
    public TaskStatus status() { return TaskStatus.CANCELED; }
}