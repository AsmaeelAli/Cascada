package com.cascada.core.features.task.usecase;

import com.cascada.core.features.task.entity.TaskEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TodayDueTasksCache {

    private final Map<LocalDate, List<TaskEntity>> cache = new HashMap<>();

    public List<TaskEntity> getDueToday(List<TaskEntity> allTasks) {
        LocalDate today = LocalDate.now();

        return cache.computeIfAbsent(today, date ->
                allTasks.stream()
                        .filter(t -> t.getDueDate().toLocalDate().isEqual(date))
                        .collect(Collectors.toList())
        );
    }

    public void invalidate() {
        cache.clear();
    }
}