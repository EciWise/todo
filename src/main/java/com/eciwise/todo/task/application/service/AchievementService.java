package com.eciwise.todo.task.application.service;

import com.eciwise.todo.task.application.dto.AchievementResponse;
import com.eciwise.todo.task.application.mapper.TaskMapper;
import com.eciwise.todo.task.application.port.in.AchievementUseCase;
import com.eciwise.todo.task.application.port.out.AchievementPort;
import com.eciwise.todo.task.application.port.out.CompletionHistoryPort;
import com.eciwise.todo.task.application.port.out.TaskPort;
import com.eciwise.todo.task.domain.model.Achievement;
import com.eciwise.todo.task.domain.model.AchievementType;
import com.eciwise.todo.task.domain.model.TaskStatus;
import com.eciwise.todo.user.application.service.CurrentUserService;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AchievementService implements AchievementUseCase {

    private static final int MILESTONE_STEP = 5;

    private final AchievementPort achievementPort;
    private final CompletionHistoryPort historyPort;
    private final TaskPort taskPort;
    private final CurrentUserService currentUserService;
    private final TaskMapper mapper;

    public AchievementService(AchievementPort achievementPort, CompletionHistoryPort historyPort,
                               TaskPort taskPort, CurrentUserService currentUserService, TaskMapper mapper) {
        this.achievementPort = achievementPort;
        this.historyPort = historyPort;
        this.taskPort = taskPort;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Override
    public Optional<Achievement> onTaskCompleted(AppUser user) {
        long count = historyPort.countByOwnerId(user.getId());
        return awardIfMilestone(user, AchievementType.TASKS_COMPLETED, count);
    }

    @Override
    public Optional<Achievement> onTaskPlanned(AppUser user) {
        long count = taskPort.countPlannedByOwnerId(user.getId());
        return awardIfMilestone(user, AchievementType.TASKS_PLANNED, count);
    }

    private Optional<Achievement> awardIfMilestone(AppUser user, AchievementType type, long count) {
        if (count > 0 && count % MILESTONE_STEP == 0) {
            Achievement achievement = achievementPort.save(Achievement.builder()
                    .owner(user).type(type).milestone((int) count).build());
            return Optional.of(achievement);
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> listForUser() {
        AppUser user = currentUserService.getOrCreate();
        return achievementPort.findByOwnerIdOrderedByDate(user.getId()).stream()
                .map(mapper::toAchievementResponse).toList();
    }
}
