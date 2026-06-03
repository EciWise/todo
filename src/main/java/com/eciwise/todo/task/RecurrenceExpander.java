package com.eciwise.todo.task;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Expande una tarea recurrente en las fechas concretas (ocurrencias) que caen
 * dentro de un rango [from, to]. Las ocurrencias no se persisten; se calculan al
 * vuelo a partir de la regla simple (freq + intervalo + fin).
 */
@Component
public class RecurrenceExpander {

    /** Tope de seguridad para evitar bucles infinitos con recurrencias indefinidas. */
    private static final int MAX_OCCURRENCES = 1000;

    public List<LocalDate> expand(Task task, LocalDate from, LocalDate to) {
        List<LocalDate> result = new ArrayList<>();
        if (task.getScheduledDate() == null || task.getRecurrenceFreq() == RecurrenceFreq.NONE) {
            if (task.getScheduledDate() != null
                    && !task.getScheduledDate().isBefore(from)
                    && !task.getScheduledDate().isAfter(to)) {
                result.add(task.getScheduledDate());
            }
            return result;
        }

        int interval = Math.max(1, task.getRecurrenceInterval());
        LocalDate cursor = task.getScheduledDate();
        int emitted = 0;

        for (int guard = 0; guard < MAX_OCCURRENCES; guard++) {
            if (reachedEnd(task, cursor, emitted)) {
                break;
            }
            if (cursor.isAfter(to)) {
                break;
            }
            if (!cursor.isBefore(from)) {
                result.add(cursor);
            }
            emitted++;
            cursor = advance(cursor, task.getRecurrenceFreq(), interval);
        }
        return result;
    }

    private boolean reachedEnd(Task task, LocalDate cursor, int emitted) {
        return switch (task.getRecurrenceEndType()) {
            case NEVER -> false;
            case ON_DATE -> task.getRecurrenceEndDate() != null && cursor.isAfter(task.getRecurrenceEndDate());
            case AFTER_COUNT -> task.getRecurrenceCount() != null && emitted >= task.getRecurrenceCount();
        };
    }

    private LocalDate advance(LocalDate date, RecurrenceFreq freq, int interval) {
        return switch (freq) {
            case DAILY -> date.plusDays(interval);
            case WEEKLY -> date.plusWeeks(interval);
            case MONTHLY -> date.plusMonths(interval);
            case NONE -> date.plusDays(1); // no deberia ocurrir
        };
    }
}
