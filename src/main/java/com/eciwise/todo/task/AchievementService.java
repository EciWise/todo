package com.eciwise.todo.task;

import com.eciwise.todo.task.dto.AchievementResponse;
import com.eciwise.todo.user.AppUser;
import com.eciwise.todo.user.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Otorga felicitaciones cada 5 tareas completadas y, de forma independiente,
 * cada 5 tareas planificadas. Son dos eventos distintos.
 */
@Service
public class AchievementService {

    private static final int MILESTONE_STEP = 5;

    private final AchievementRepository achievementRepository;
    private final TaskCompletionHistoryRepository historyRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public AchievementService(AchievementRepository achievementRepository,
                              TaskCompletionHistoryRepository historyRepository,
                              TaskRepository taskRepository,
                              CurrentUserService currentUserService,
                              TaskMapper mapper) {
        this.achievementRepository = achievementRepository;
        this.historyRepository = historyRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    /** Llamado tras completar una tarea. Devuelve la felicitacion si toco hito. */
    Optional<Achievement> onTaskCompleted(AppUser user) {
        long count = historyRepository.countByOwner_Id(user.getId());
        return awardIfMilestone(user, AchievementType.TASKS_COMPLETED, count);
    }

    /** Llamado cuando una tarea pasa a estar planificada (tiene fecha). */
    Optional<Achievement> onTaskPlanned(AppUser user) {
        long count = taskRepository.countByOwner_IdAndPlannedNotifiedTrue(user.getId());
        return awardIfMilestone(user, AchievementType.TASKS_PLANNED, count);
    }

    private Optional<Achievement> awardIfMilestone(AppUser user, AchievementType type, long count) {
        if (count > 0 && count % MILESTONE_STEP == 0) {
            Achievement achievement = achievementRepository.save(Achievement.builder()
                    .owner(user)
                    .type(type)
                    .milestone((int) count)
                    .build());
            return Optional.of(achievement);
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return achievementRepository.findByOwner_IdOrderByAwardedAtDesc(user.getId()).stream()
                .map(mapper::toAchievementResponse)
                .toList();
    }
}
