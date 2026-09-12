package com.cascada.core.features.task.states;

import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.domain.enums.TaskStatus;
import java.time.LocalDateTime;

public interface TaskState {

    TaskState markInProgress(TaskEntity task);
    TaskState markDone(TaskEntity task);
    TaskState cancel(TaskEntity task);
    TaskState markOverdue(TaskEntity task);
    TaskState postpone(TaskEntity task, LocalDateTime newDueDate);

    TaskStatus status();

    static TaskState of(TaskStatus status) {
        return switch (status) {
            case TODO -> new TodoState();
            case IN_PROGRESS -> new InProgressState();
            case DONE -> new DoneState();
            case CANCELED -> new CanceledState();
            case OVERDUE -> new OverdueState();
        };
    }
}