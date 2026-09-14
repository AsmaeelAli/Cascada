package com.cascada.core.features.task.entity;

import com.cascada.core.domain.enums.Priority;
import com.cascada.core.domain.enums.TaskStatus;
import com.cascada.core.domain.exception.InvalidTaskStateException;
import com.cascada.core.features.reminder.entity.ReminderEntity;
import com.cascada.core.features.task.states.TaskState;
import com.cascada.core.features.task.states.TodoState;
import com.cascada.core.features.user.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private UserEntity owner;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Transient
    private TaskState state;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReminderEntity> reminderEntities = new ArrayList<>();

    protected TaskEntity() {}

    public TaskEntity(UserEntity owner, String title, LocalDateTime dueDate, Priority priority) {
        this.owner = owner;
        this.title = validateTitle(title);
        this.dueDate = dueDate;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.state = new TodoState();
        this.status = state.status();
        this.reminderEntities.add(ReminderEntity.createDefault(this, dueDate));
    }

    // ===== جديد: إنشاء مهمة Broadcast بدون owner محدد =====
    public static TaskEntity createBroadcast(String title, LocalDateTime dueDate, Priority priority) {
        TaskEntity task = new TaskEntity();
        task.owner = null;
        task.title = task.validateTitle(title);
        task.dueDate = dueDate;
        task.priority = priority;
        task.createdAt = LocalDateTime.now();
        task.state = new TodoState();
        task.status = task.state.status();
        return task;
    }

    @PostLoad
    private void rebuildState() {
        this.state = TaskState.of(this.status);
    }

    public void markInProgress() {
        this.state = state.markInProgress(this);
        syncStatus();
    }

    public void markDone() {
        this.state = state.markDone(this);
        syncStatus();
    }

    public void cancel() {
        this.state = state.cancel(this);
        syncStatus();
    }

    public void markOverdue() {
        this.state = state.markOverdue(this);
        syncStatus();
    }

    public void postpone(LocalDateTime newDueDate) {
        this.state = state.postpone(this, newDueDate);
        syncStatus();
    }

    // ===== جديد: تعيين صاحب لمهمة Broadcast (عملية الـ Claim) =====
    public void assignOwner(UserEntity newOwner) {
        if (this.owner != null) {
            throw new InvalidTaskStateException(
                    "Task '" + this.title + "' is already claimed by " + this.owner.getName()
            );
        }
        this.owner = newOwner;
        this.reminderEntities.add(ReminderEntity.createDefault(this, this.dueDate));
    }

    private void syncStatus() {
        this.status = state.status();
    }

    public void applyCompletion() {
        this.completedAt = LocalDateTime.now();
    }

    public void applyPostponement(LocalDateTime newDueDate) {
        this.dueDate = newDueDate;
        this.reminderEntities.add(ReminderEntity.createDefault(this, newDueDate));
    }

    public Long getId() { return id; }
    public UserEntity getOwner() { return owner; }
    public String getTitle() { return title; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Priority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public List<ReminderEntity> getReminders() {
        return Collections.unmodifiableList(reminderEntities);
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        return title;
    }
}
