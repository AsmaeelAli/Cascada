package com.cascada.core.notification.factory;

import com.cascada.core.features.reminder.entity.ReminderEntity;

public class EmailReminderSender implements ReminderSender {
    @Override
    public void send(ReminderEntity reminder) {
        System.out.println("[EMAIL] Reminder sent for task: " + reminder.getTask().getTitle());
        reminder.markSent();
    }
}