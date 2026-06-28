package com.eciwise.todo.task.infrastructure.in.rest;

import com.eciwise.todo.task.application.dto.AchievementResponse;
import com.eciwise.todo.task.application.port.in.AchievementUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementUseCase achievementUseCase;

    public AchievementController(AchievementUseCase achievementUseCase) { this.achievementUseCase = achievementUseCase; }

    @GetMapping
    public List<AchievementResponse> findAll() { return achievementUseCase.listForUser(); }
}
