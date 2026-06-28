package com.eciwise.todo.task.application.service;

import com.eciwise.todo.task.application.dto.StatsResponse;
import com.eciwise.todo.task.application.port.in.StatsUseCase;
import com.eciwise.todo.task.application.port.out.CompletionHistoryPort;
import com.eciwise.todo.task.application.port.out.TaskPort;
import com.eciwise.todo.task.domain.model.Importance;
import com.eciwise.todo.task.domain.model.TaskCompletionHistory;
import com.eciwise.todo.task.domain.model.TaskStatus;
import com.eciwise.todo.user.application.service.CurrentUserService;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StatsService implements StatsUseCase {

    private final TaskPort taskPort;
    private final CompletionHistoryPort historyPort;
    private final CurrentUserService currentUserService;

    public StatsService(TaskPort taskPort, CompletionHistoryPort historyPort,
                        CurrentUserService currentUserService) {
        this.taskPort = taskPort;
        this.historyPort = historyPort;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public StatsResponse forUser() {
        AppUser user = currentUserService.getOrCreate();
        Long uid = user.getId();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        long total = taskPort.countByOwnerId(uid);
        long pending = taskPort.countByOwnerIdAndStatus(uid, TaskStatus.PENDING);
        long inProgress = taskPort.countByOwnerIdAndStatus(uid, TaskStatus.IN_PROGRESS);
        long done = taskPort.countByOwnerIdAndStatus(uid, TaskStatus.DONE);

        Instant weekStart = today.minusDays(6).atStartOfDay(zone).toInstant();
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
        Instant now = Instant.now();
        long completedThisWeek = historyPort.findByOwnerIdBetween(uid, weekStart, now).size();
        long completedThisMonth = historyPort.findByOwnerIdBetween(uid, monthStart, now).size();

        long totalCompleted = historyPort.countByOwnerId(uid);
        double completionRate = total == 0 ? 0.0 : (double) done / total;

        long overdue = taskPort.findOverdue(uid, today, TaskStatus.DONE).size();
        long dueToday = taskPort.findByOwnerIdAndDate(uid, today).size();

        List<TaskCompletionHistory> history = historyPort.findByOwnerIdOrderedByDate(uid);
        int streak = currentStreak(history, today, zone);
        Map<Importance, Long> byImportance = completedByImportance(history);

        return new StatsResponse(total, pending, inProgress, done,
                completedThisWeek, completedThisMonth, completionRate,
                streak, overdue, dueToday, byImportance);
    }

    private int currentStreak(List<TaskCompletionHistory> history, LocalDate today, ZoneId zone) {
        Set<LocalDate> days = new HashSet<>();
        for (TaskCompletionHistory h : history) {
            days.add(h.getCompletedAt().atZone(zone).toLocalDate());
        }
        if (days.isEmpty()) return 0;
        LocalDate cursor = today;
        if (!days.contains(cursor)) {
            cursor = today.minusDays(1);
            if (!days.contains(cursor)) return 0;
        }
        int streak = 0;
        while (days.contains(cursor)) { streak++; cursor = cursor.minusDays(1); }
        return streak;
    }

    private Map<Importance, Long> completedByImportance(List<TaskCompletionHistory> history) {
        Map<Importance, Long> map = new EnumMap<>(Importance.class);
        for (Importance i : Importance.values()) map.put(i, 0L);
        for (TaskCompletionHistory h : history) map.merge(h.getImportance(), 1L, Long::sum);
        return map;
    }
}
