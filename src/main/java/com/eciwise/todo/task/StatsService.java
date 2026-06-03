package com.eciwise.todo.task;

import com.eciwise.todo.task.dto.StatsResponse;
import com.eciwise.todo.user.AppUser;
import com.eciwise.todo.user.CurrentUserService;
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
public class StatsService {

    private final TaskRepository taskRepository;
    private final TaskCompletionHistoryRepository historyRepository;
    private final CurrentUserService currentUserService;

    public StatsService(TaskRepository taskRepository,
                        TaskCompletionHistoryRepository historyRepository,
                        CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public StatsResponse forUser() {
        AppUser user = currentUserService.getOrCreate();
        Long uid = user.getId();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        long total = taskRepository.countByOwner_Id(uid);
        long pending = taskRepository.countByOwner_IdAndStatus(uid, TaskStatus.PENDING);
        long inProgress = taskRepository.countByOwner_IdAndStatus(uid, TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByOwner_IdAndStatus(uid, TaskStatus.DONE);

        Instant weekStart = today.minusDays(6).atStartOfDay(zone).toInstant();
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
        Instant now = Instant.now();
        long completedThisWeek = historyRepository
                .findByOwner_IdAndCompletedAtBetween(uid, weekStart, now).size();
        long completedThisMonth = historyRepository
                .findByOwner_IdAndCompletedAtBetween(uid, monthStart, now).size();

        long totalCompleted = historyRepository.countByOwner_Id(uid);
        double completionRate = total == 0 ? 0.0 : (double) done / total;

        long overdue = taskRepository.findByOwner_IdAndScheduledDateBeforeAndStatusNot(
                uid, today, TaskStatus.DONE).size();
        long dueToday = taskRepository.findByOwner_IdAndScheduledDate(uid, today).size();

        List<TaskCompletionHistory> history = historyRepository.findByOwner_IdOrderByCompletedAtDesc(uid);
        int streak = currentStreak(history, today, zone);
        Map<Importance, Long> byImportance = completedByImportance(history);

        return new StatsResponse(
                total, pending, inProgress, done,
                completedThisWeek, completedThisMonth,
                completionRate, streak, overdue, dueToday, byImportance);
    }

    /** Dias consecutivos (terminando hoy o ayer) con al menos una tarea completada. */
    private int currentStreak(List<TaskCompletionHistory> history, LocalDate today, ZoneId zone) {
        Set<LocalDate> daysWithCompletion = new HashSet<>();
        for (TaskCompletionHistory h : history) {
            daysWithCompletion.add(h.getCompletedAt().atZone(zone).toLocalDate());
        }
        if (daysWithCompletion.isEmpty()) {
            return 0;
        }
        // La racha vale si la ultima actividad fue hoy o ayer.
        LocalDate cursor = today;
        if (!daysWithCompletion.contains(cursor)) {
            cursor = today.minusDays(1);
            if (!daysWithCompletion.contains(cursor)) {
                return 0;
            }
        }
        int streak = 0;
        while (daysWithCompletion.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private Map<Importance, Long> completedByImportance(List<TaskCompletionHistory> history) {
        Map<Importance, Long> map = new EnumMap<>(Importance.class);
        for (Importance i : Importance.values()) {
            map.put(i, 0L);
        }
        for (TaskCompletionHistory h : history) {
            map.merge(h.getImportance(), 1L, Long::sum);
        }
        return map;
    }
}
