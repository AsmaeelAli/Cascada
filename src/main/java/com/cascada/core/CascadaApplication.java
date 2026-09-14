package com.cascada.core;

import com.cascada.core.cli.CascadaCLI;
import com.cascada.core.common.event.EventSubscriptionConfig;
import com.cascada.core.domain.enums.Priority;
import com.cascada.core.features.reminder.data.ReminderRepository;
import com.cascada.core.features.reminder.usecase.ReminderDispatcher;
import com.cascada.core.features.task.data.TaskRepository;
import com.cascada.core.features.task.usecase.TaskAnalyticsService;
import com.cascada.core.features.task.usecase.TaskService;
import com.cascada.core.features.user.data.UserRepository;
import com.cascada.core.features.user.entity.UserEntity;
import com.cascada.core.features.user.usecase.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;

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

        UserEntity op1 = userService.registerUser("asmaeel","asmaeel@gmail.com");
        UserEntity op2 = userService.registerUser("mohanad","mohanad@gmail.com");
        UserEntity op3 = userService.registerUser("yousef","yousef@gmail.com");
        UserEntity op4 = userService.registerUser("ali","ali@gmail.com");

        taskService.createTask(op1,"java", LocalDateTime.now().plusDays(5), Priority.MEDIUM);
        taskService.createTask(op2,"kotlin", LocalDateTime.now().plusDays(7), Priority.MEDIUM);
        taskService.createTask(op3,"c++", LocalDateTime.now().plusDays(3), Priority.MEDIUM);
        taskService.createTask(op4,"docker", LocalDateTime.now().plusDays(9), Priority.MEDIUM);

        CascadaCLI cli = new CascadaCLI(userService, taskService, analyticsService, reminderDispatcher);
        cli.start();

        em.close();
        emf.close();
    }
}