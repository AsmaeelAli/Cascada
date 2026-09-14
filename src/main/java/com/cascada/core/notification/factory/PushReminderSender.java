package com.cascada.core.notification.factory;

import com.cascada.core.features.reminder.entity.ReminderEntity;

public class PushReminderSender implements ReminderSender {
    @Override
    public void send(ReminderEntity reminder) {
        System.out.println("[PUSH] Reminder sent for task: " + reminder.getTask().getTitle());
        reminder.markSent();
    }
}