package com.eciwise.todo.task;

/** Forma en que termina una recurrencia: nunca, en una fecha, o tras N ocurrencias. */
public enum RecurrenceEndType {
    NEVER,
    ON_DATE,
    AFTER_COUNT
}
