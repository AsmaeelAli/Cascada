package com.cascada.core.features.task.states;

import com.cascada.core.domain.exception.InvalidTaskStateException;
import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.domain.enums.TaskStatus;
import java.time.LocalDateTime;

public class InProgressState implements TaskState {

    @Override
    public TaskState markInProgress(TaskEntity task) {
        throw new InvalidTaskStateException("Task is already IN_PROGRESS");
    }

    @Override
    public TaskState markDone(TaskEntity task) {
        task.applyCompletion();
        return new DoneState();
    }

    @Override
    public TaskState cancel(TaskEntity task) {
        return new CanceledState();
    }

    @Override
    public TaskState markOverdue(TaskEntity task) {
        return new OverdueState();
    }

    @Override
    public TaskState postpone(TaskEntity task, LocalDateTime newDueDate) {
        task.applyPostponement(newDueDate);
        return this;
    }

    @Override
    public TaskStatus status() { return TaskStatus.IN_PROGRESS; }
}