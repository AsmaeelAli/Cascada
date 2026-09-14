package com.cascada.core;

import com.cascada.core.cli.CascadaCLI;
import com.cascada.core.common.event.EventSubscriptionConfig;
import com.cascada.core.features.reminder.data.ReminderRepository;
import com.cascada.core.features.reminder.usecase.ReminderDispatcher;
import com.cascada.core.features.task.data.TaskRepository;
import com.cascada.core.features.task.usecase.TaskAnalyticsService;
import com.cascada.core.features.task.usecase.TaskService;
import com.cascada.core.features.user.data.UserRepository;
import com.cascada.core.features.user.usecase.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CascadaApplication {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Cascada");
        EntityManager em = emf.createEntityManager();

        EventSubscriptionConfig.registerAllObservers();

        UserRepository userRepository = new UserRepository(em);
        TaskRepository taskRepository = new TaskRepository(em);
        ReminderRepository reminderRepository = new ReminderRepository(em);

        UserService userService = new UserService(userRepository);
        TaskService taskService = new TaskService(taskRepository);
        TaskAnalyticsService analyticsService = new TaskAnalyticsService(taskRepository);
        ReminderDispatcher reminderDispatcher = new ReminderDispatcher(reminderRepository);

        CascadaCLI cli = new CascadaCLI(userService, taskService, analyticsService, reminderDispatcher);
        cli.start();

        em.close();
        emf.close();
    }
}