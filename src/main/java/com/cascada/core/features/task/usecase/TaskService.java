package com.cascada.core.features.task.usecase;

import com.cascada.core.common.event.EventBus;
import com.cascada.core.domain.enums.Priority;
import com.cascada.core.domain.exception.EntityNotFoundException;
import com.cascada.core.features.task.data.TaskRepository;
import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.features.task.event.TaskEvent;
import com.cascada.core.features.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository;
    private final EventBus eventBus;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.eventBus = EventBus.getInstance();
    }

    public TaskEntity createTask(UserEntity owner, String title, LocalDateTime dueDate, Priority priority) {
        TaskEntity task = new TaskEntity(owner, title, dueDate, priority);
        taskRepository.save(task);
        eventBus.publish(new TaskEvent.Created(task));
        return task;
    }

    public TaskEntity broadcastTask(String title, LocalDateTime dueDate, Priority priority) {
        TaskEntity task = TaskEntity.createBroadcast(title, dueDate, priority);
        taskRepository.save(task);
        eventBus.publish(new TaskEvent.Broadcasted(task));
        return task;
    }

    public synchronized TaskEntity claimTask(Long taskId, UserEntity claimer) {
        TaskEntity task = getTaskOrThrow(taskId);
        task.assignOwner(claimer);
        taskRepository.save(task);
        eventBus.publish(new TaskEvent.Claimed(task, claimer));
        return task;
    }

    public TaskEntity completeTask(Long taskId) {
        TaskEntity task = getTaskOrThrow(taskId);
        task.markDone();
        taskRepository.save(task);
        eventBus.publish(new TaskEvent.Completed(task));
        return task;
    }

    public TaskEntity cancelTask(Long taskId) {
        TaskEntity task = getTaskOrThrow(taskId);
        task.cancel();
        taskRepository.save(task);
        eventBus.publish(new TaskEvent.Cancelled(task));
        return task;
    }

    public TaskEntity postponeTask(Long taskId, LocalDateTime newDueDate) {
        TaskEntity task = getTaskOrThrow(taskId);
        task.postpone(newDueDate);
        taskRepository.save(task);
        return task;
    }

    public List<TaskEntity> getUnclaimedTasks() {
        return taskRepository.findUnclaimed();
    }

    public List<TaskEntity> getTasksByOwner(Long ownerId) {
        return taskRepository.findByOwnerId(ownerId);
    }

    private TaskEntity getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task", taskId));
    }
}