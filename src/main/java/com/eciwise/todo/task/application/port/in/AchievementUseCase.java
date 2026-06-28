package com.eciwise.todo.task.application.port.in;

import com.eciwise.todo.task.application.dto.AchievementResponse;
import com.eciwise.todo.task.domain.model.Achievement;
import com.eciwise.todo.user.domain.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface AchievementUseCase {
    List<AchievementResponse> listForUser();
    Optional<Achievement> onTaskCompleted(AppUser user);
    Optional<Achievement> onTaskPlanned(AppUser user);
}
