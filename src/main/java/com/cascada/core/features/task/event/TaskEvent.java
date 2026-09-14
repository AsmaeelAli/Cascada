package com.cascada.core.features.task.event;

import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.features.user.entity.UserEntity;

public sealed interface TaskEvent {

    record Created(TaskEntity task) implements TaskEvent {}
    record Broadcasted(TaskEntity task) implements TaskEvent {}
    record Claimed(TaskEntity task, UserEntity claimedBy) implements TaskEvent {}
    record Completed(TaskEntity task) implements TaskEvent {}
    record Cancelled(TaskEntity task) implements TaskEvent {}
}