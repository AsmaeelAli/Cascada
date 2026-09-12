package com.cascada.core.manual;

import com.cascada.core.domain.enums.Priority;
import com.cascada.core.domain.exception.InvalidTaskStateException;
import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.features.user.entity.UserEntity;

import java.time.LocalDateTime;

public class TaskLifecycleTest {

    public static void main(String[] args) {
        UserEntity owner = new UserEntity("Test Owner", "owner@example.com");

        System.out.println("=== Test 1: Normal lifecycle TODO -> IN_PROGRESS -> DONE ===");
        TaskEntity task = new TaskEntity(owner, "Sample Task", LocalDateTime.now().plusDays(1), Priority.MEDIUM);
        System.out.println("Initial status: " + task.getStatus());

        task.markInProgress();
        System.out.println("After markInProgress: " + task.getStatus());

        task.markDone();
        System.out.println("After markDone: " + task.getStatus());

        System.out.println("\n=== Test 2: Cannot complete an already DONE task ===");
        try {
            task.markDone();
        } catch (InvalidTaskStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\n=== Test 3: Postpone increases reminder count ===");
        TaskEntity task2 = new TaskEntity(owner, "Postpone Test", LocalDateTime.now().plusDays(1), Priority.LOW);
        System.out.println("Reminders before postpone: " + task2.getReminders().size());

        task2.postpone(LocalDateTime.now().plusDays(3));
        System.out.println("Reminders after 1st postpone: " + task2.getReminders().size());

        task2.postpone(LocalDateTime.now().plusDays(5));
        System.out.println("Reminders after 2nd postpone: " + task2.getReminders().size());

        System.out.println("\n=== Test 4: Cannot postpone a CANCELLED task ===");
        TaskEntity task3 = new TaskEntity(owner, "Cancel Test", LocalDateTime.now().plusDays(1), Priority.HIGH);
        task3.cancel();
        try {
            task3.postpone(LocalDateTime.now().plusDays(2));
        } catch (InvalidTaskStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }
    }
}