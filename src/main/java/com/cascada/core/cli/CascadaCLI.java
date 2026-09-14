package com.cascada.core.cli;

import com.cascada.core.domain.enums.Priority;
import com.cascada.core.domain.exception.GlobalException;
import com.cascada.core.features.reminder.usecase.ReminderDispatcher;
import com.cascada.core.features.task.entity.TaskEntity;
import com.cascada.core.features.task.usecase.TaskAnalyticsService;
import com.cascada.core.features.task.usecase.TaskService;
import com.cascada.core.features.user.entity.UserEntity;
import com.cascada.core.features.user.usecase.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class CascadaCLI {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService;
    private final TaskService taskService;
    private final TaskAnalyticsService analyticsService;
    private final ReminderDispatcher reminderDispatcher;

    private UserEntity currentUser;

    public CascadaCLI(UserService userService, TaskService taskService,
                      TaskAnalyticsService analyticsService, ReminderDispatcher reminderDispatcher) {
        this.userService = userService;
        this.taskService = taskService;
        this.analyticsService = analyticsService;
        this.reminderDispatcher = reminderDispatcher;
    }

    public void start() {
        printBanner();

        if (!loginOrRegister()) {
            System.out.println("Exiting Cascada. Goodbye!");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> createTask();
                    case 2 -> broadcastTask();
                    case 3 -> viewUnclaimedTasks();
                    case 4 -> claimTask();
                    case 5 -> viewMyTasks();
                    case 6 -> completeTask();
                    case 7 -> cancelTask();
                    case 8 -> postponeTask();
                    case 9 -> viewDueSoonReport();
                    case 10 -> viewAnalytics();
                    case 11 -> reminderDispatcher.dispatchDueReminders();
                    case 0 -> running = false;
                    default -> System.out.println(">> Invalid option, try again.");
                }
            } catch (GlobalException e) {
                System.out.println(">> Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println(">> Unexpected error: " + e.getMessage());
            }

            System.out.println();
        }

        System.out.println("Session ended. See you soon, " + currentUser.getName() + "!");
    }

    private boolean loginOrRegister() {
        System.out.println("1. Register new user");
        System.out.println("0. Exit");
        int choice = readInt("Choose: ");

        if (choice == 1) {
            String name = readString("Name: ");
            String email = readString("Email: ");
            try {
                currentUser = userService.registerUser(name, email);
                System.out.println(">> Welcome, " + currentUser.getName() + "! (User ID: " + currentUser.getId() + ")");
                return true;
            } catch (GlobalException e) {
                System.out.println(">> " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    private void createTask() {
        String title = readString("Task title: ");
        Priority priority = readPriority();
        int days = readInt("Due in how many days? ");
        LocalDateTime dueDate = LocalDateTime.now().plusDays(days);

        TaskEntity task = taskService.createTask(currentUser, title, dueDate, priority);
        System.out.println(">> Task created: [" + task.getId() + "] " + task.getTitle());
    }

    private void broadcastTask() {
        String title = readString("Task title (broadcast): ");
        Priority priority = readPriority();
        int days = readInt("Due in how many days? ");
        LocalDateTime dueDate = LocalDateTime.now().plusDays(days);

        TaskEntity task = taskService.broadcastTask(title, dueDate, priority);
        System.out.println(">> Task broadcasted to everyone: [" + task.getId() + "] " + task.getTitle());
    }

    private void viewUnclaimedTasks() {
        List<TaskEntity> tasks = taskService.getUnclaimedTasks();
        if (tasks.isEmpty()) {
            System.out.println(">> No unclaimed tasks right now.");
            return;
        }
        System.out.println("--- Unclaimed Tasks ---");
        tasks.forEach(t -> System.out.println("[" + t.getId() + "] " + t.getTitle()
                + " | Priority: " + t.getPriority() + " | Due: " + t.getDueDate()));
    }

    private void claimTask() {
        Long taskId = readLong("Task ID to claim: ");
        TaskEntity task = taskService.claimTask(taskId, currentUser);
        System.out.println(">> You claimed: " + task.getTitle());
    }

    private void viewMyTasks() {
        List<TaskEntity> tasks = taskService.getTasksByOwner(currentUser.getId());
        if (tasks.isEmpty()) {
            System.out.println(">> You have no tasks yet.");
            return;
        }
        System.out.println("--- My Tasks ---");
        tasks.forEach(this::printTaskLine);
    }

    private void completeTask() {
        Long taskId = readLong("Task ID to complete: ");
        TaskEntity task = taskService.completeTask(taskId);
        System.out.println(">> Marked as DONE: " + task.getTitle());
    }

    private void cancelTask() {
        Long taskId = readLong("Task ID to cancel: ");
        TaskEntity task = taskService.cancelTask(taskId);
        System.out.println(">> Cancelled: " + task.getTitle());
    }

    private void postponeTask() {
        Long taskId = readLong("Task ID to postpone: ");
        int days = readInt("Postpone by how many days? ");
        TaskEntity task = taskService.postponeTask(taskId, LocalDateTime.now().plusDays(days));
        System.out.println(">> Postponed to: " + task.getDueDate());
    }

    private void viewDueSoonReport() {
        List<TaskEntity> report = analyticsService.getDueSoonReport();
        if (report.isEmpty()) {
            System.out.println(">> No active tasks to report.");
            return;
        }
        System.out.println("--- Due Soon Report (sorted) ---");
        report.forEach(this::printTaskLine);
    }

    private void viewAnalytics() {
        System.out.println("--- Analytics ---");
        System.out.println("Completed this week per user: " + analyticsService.completedTasksPerUserThisWeek());
        System.out.println("Overdue count by priority: " + analyticsService.overdueTaskCountByPriority());
        System.out.printf("Average completion time: %.2f hours%n", analyticsService.averageCompletionTimeInHours());
        System.out.println("Cancelled tasks by user: " + analyticsService.cancelledTasksByUser());
    }

    private void printTaskLine(TaskEntity t) {
        System.out.println("[" + t.getId() + "] " + t.getTitle()
                + " | " + t.getPriority() + " | " + t.getStatus() + " | Due: " + t.getDueDate());
    }

    private void printBanner() {
        System.out.println("==================================");
        System.out.println("        CASCADA TASK MANAGER");
        System.out.println("==================================");
    }

    private void printMainMenu() {
        System.out.println("--------------------------------");
        System.out.println("1.  Create task (for me)");
        System.out.println("2.  Broadcast task (anyone can claim)");
        System.out.println("3.  View unclaimed tasks");
        System.out.println("4.  Claim a task");
        System.out.println("5.  View my tasks");
        System.out.println("6.  Complete a task");
        System.out.println("7.  Cancel a task");
        System.out.println("8.  Postpone a task");
        System.out.println("9.  Due Soon report (sorted)");
        System.out.println("10. View analytics");
        System.out.println("11. Dispatch due reminders (concurrent)");
        System.out.println("0.  Exit");
        System.out.println("--------------------------------");
    }

    private Priority readPriority() {
        System.out.println("Priority: 1=LOW 2=MEDIUM 3=HIGH");
        int p = readInt("Choose: ");
        return switch (p) {
            case 1 -> Priority.LOW;
            case 2 -> Priority.MEDIUM;
            case 3 -> Priority.HIGH;
            default -> Priority.MEDIUM;
        };
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private Long readLong(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextLong()) {
            System.out.print("Please enter a valid ID: ");
            scanner.next();
        }
        long value = scanner.nextLong();
        scanner.nextLine();
        return value;
    }
}