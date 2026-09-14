package com.cascada.core.common.sorting;

import com.cascada.core.features.task.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;

public final class TaskDueSoonSorter {

    private TaskDueSoonSorter() {}

    public static List<TaskEntity> sort(List<TaskEntity> tasks) {
        if (tasks.size() <= 1) {
            return new ArrayList<>(tasks);
        }

        int mid = tasks.size() / 2;
        List<TaskEntity> left = sort(tasks.subList(0, mid));
        List<TaskEntity> right = sort(tasks.subList(mid, tasks.size()));

        return merge(left, right);
    }

    private static List<TaskEntity> merge(List<TaskEntity> left, List<TaskEntity> right) {
        List<TaskEntity> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (isInOrder(left.get(i), right.get(j))) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }

        return result;
    }

    private static boolean isInOrder(TaskEntity a, TaskEntity b) {
        if (a.getDueDate().isBefore(b.getDueDate())) {
            return true;
        }
        if (a.getDueDate().isEqual(b.getDueDate())) {
            return a.getPriority().getWeight() >= b.getPriority().getWeight();
        }
        return false;
    }
}
