package com.eciwise.todo.task.application.service;

import com.eciwise.todo.task.domain.model.RecurrenceFreq;
import com.eciwise.todo.task.domain.model.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecurrenceExpander {

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
            if (reachedEnd(task, cursor, emitted)) break;
            if (cursor.isAfter(to)) break;
            if (!cursor.isBefore(from)) result.add(cursor);
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
            case NONE -> date.plusDays(1);
        };
    }
}
