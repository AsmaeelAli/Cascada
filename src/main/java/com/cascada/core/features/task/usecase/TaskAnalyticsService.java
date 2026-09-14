package com.cascada.core.features.task.usecase;

import com.cascada.core.common.sorting.TaskDueSoonSorter;
import com.cascada.core.domain.enums.Priority;
import com.cascada.core.domain.enums.TaskStatus;
import com.cascada.core.features.reminder.entity.ReminderEntity;
import com.cascada.core.features.task.data.TaskRepository;
import com.cascada.core.features.task.entity.TaskEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public final class TaskAnalyticsService {

    private final TaskRepository taskRepository;

    public TaskAnalyticsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // 1. عدد المهام المنجزة لكل مستخدم هالأسبوع
    public Map<String, Long> completedTasksPerUserThisWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().isAfter(weekAgo))
                .collect(Collectors.groupingBy(
                        t -> t.getOwner().getName(),
                        Collectors.counting()
                ));
    }

    // 2. عدد المهام المتأخرة حسب الأولوية
    public Map<Priority, Long> overdueTaskCountByPriority() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.OVERDUE)
                .collect(Collectors.groupingBy(
                        TaskEntity::getPriority,
                        Collectors.counting()
                ));
    }

    // 3. متوسط وقت الإنجاز (بالساعات)
    public double averageCompletionTimeInHours() {
        OptionalDouble average = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE && t.getCompletedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toMinutes())
                .average();

        return average.isPresent() ? average.getAsDouble() / 60.0 : 0.0;
    }

    // 4. المهام الملغية لكل يوزر
    public Map<String, List<String>> cancelledTasksByUser() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.CANCELED)
                .collect(Collectors.groupingBy(
                        t -> t.getOwner().getName(),
                        Collectors.mapping(TaskEntity::getTitle, Collectors.toList())
                ));
    }

    // 5. Timeline كامل للـ Reminders الخاصة بمهمة معينة
    public List<String> reminderTimelineForTask(TaskEntity task) {
        return task.getReminders().stream()
                .map(r -> formatReminderPath(task, r))
                .collect(Collectors.toList());
    }

    private String formatReminderPath(TaskEntity task, ReminderEntity reminder) {
        String ownerName = task.getOwner() != null ? task.getOwner().getName() : "Unclaimed";
        return String.format("[%s] %s -> Owner: %s -> Reminder @ %s via %s (sent: %s)",
                task.getPriority(),
                task.getTitle(),
                ownerName,
                reminder.getTriggerTime(),
                reminder.getChannel(),
                reminder.isSent());
    }

    public List<TaskEntity> getDueSoonReport() {
        List<TaskEntity> activeTasks = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.TODO || t.getStatus() == TaskStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        return TaskDueSoonSorter.sort(activeTasks);
    }
}