package com.cascada.core.features.reminder.usecase;

import com.cascada.core.features.reminder.data.ReminderRepository;
import com.cascada.core.features.reminder.entity.ReminderEntity;
import com.cascada.core.notification.factory.ReminderFactory;
import com.cascada.core.notification.factory.ReminderSender;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReminderDispatcher {

    private final ReminderRepository reminderRepository;

    public ReminderDispatcher(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public void dispatchDueReminders() {
        List<ReminderEntity> unsent = reminderRepository.findUnsent();

        if (unsent.isEmpty()) {
            System.out.println("No pending reminders to dispatch.");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger sentCount = new AtomicInteger(0);

        for (ReminderEntity reminder : unsent) {
            executor.submit(() -> {
                ReminderSender sender = ReminderFactory.createSender(reminder.getChannel());
                sender.send(reminder);
                sentCount.incrementAndGet();
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Dispatched " + sentCount.get() + " reminders concurrently.");
    }
}