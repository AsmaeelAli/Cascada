package com.cascada.core.notification.factory;

import com.cascada.core.features.reminder.entity.ReminderEntity;

public interface ReminderSender {
    void send(ReminderEntity reminder);
}