package com.cascada.core.domain.enums;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    CANCELED,
    OVERDUE;

    public boolean isTerminal() {
        return this == DONE || this == CANCELED;
    }

    public boolean isActive() {
        return this == TODO || this == IN_PROGRESS;
    }
}