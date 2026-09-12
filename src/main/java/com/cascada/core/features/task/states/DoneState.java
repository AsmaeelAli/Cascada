package com.cascada.core.features.task.states;

import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.domain.enums.TaskStatus;
import com.cascada.core.domain.exception.InvalidTaskStateException;
import java.time.LocalDateTime;

public class DoneState implements TaskState {

    @Override
    public TaskState markInProgress(TaskEntity task) {
        throw new InvalidTaskStateException("Cannot reopen a task that is DONE");
    }

    @Override
    public TaskState markDone(TaskEntity task) {
        throw new InvalidTaskStateException("Task is already DONE");
    }

    @Override
    public TaskState cancel(TaskEntity task) {
        throw new InvalidTaskStateException("Cannot cancel a task that is already DONE");
    }

    @Override
    public TaskState markOverdue(TaskEntity task) {
        throw new InvalidTaskStateException("Cannot mark a DONE task as OVERDUE");
    }

    @Override
    public TaskState postpone(TaskEntity task, LocalDateTime newDueDate) {
        throw new InvalidTaskStateException("Cannot postpone a task that is already DONE");
    }

    @Override
    public TaskStatus status() { return TaskStatus.DONE; }
}