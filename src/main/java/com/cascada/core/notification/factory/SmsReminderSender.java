package com.cascada.core.notification.factory;

import com.cascada.core.features.reminder.entity.ReminderEntity;

public class SmsReminderSender implements ReminderSender {
    @Override
    public void send(ReminderEntity reminder) {
        System.out.println("[SMS] Reminder sent for task: " + reminder.getTask().getTitle());
        reminder.markSent();
    }
}