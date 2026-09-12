package com.cascada.core.features.reminder.entity;

import com.cascada.core.domain.enums.NotificationChannel;
import com.cascada.core.features.task.entity.TaskEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @Column(nullable = false)
    private LocalDateTime triggerTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private boolean sent = false;

    protected ReminderEntity() {
        // JPA فقط
    }

    private ReminderEntity(TaskEntity task, LocalDateTime triggerTime, NotificationChannel channel) {
        this.task = task;
        this.triggerTime = triggerTime;
        this.channel = channel;
    }

    public static ReminderEntity createDefault(TaskEntity task, LocalDateTime dueDate) {
        LocalDateTime triggerTime = dueDate.minusHours(24);
        return new ReminderEntity(task, triggerTime, NotificationChannel.EMAIL);
    }

    public void markSent() {
        this.sent = true;
    }

    public Long getId() { return id; }
    public TaskEntity getTask() { return task; }
    public LocalDateTime getTriggerTime() { return triggerTime; }
    public NotificationChannel getChannel() { return channel; }
    public boolean isSent() { return sent; }
}