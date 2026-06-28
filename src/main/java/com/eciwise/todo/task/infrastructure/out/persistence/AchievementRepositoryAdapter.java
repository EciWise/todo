package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.application.port.out.AchievementPort;
import com.eciwise.todo.task.domain.model.Achievement;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AchievementRepositoryAdapter implements AchievementPort {

    private final AchievementJpaRepository repository;

    public AchievementRepositoryAdapter(AchievementJpaRepository repository) { this.repository = repository; }

    @Override public Achievement save(Achievement achievement) { return repository.save(achievement); }
    @Override public List<Achievement> findByOwnerIdOrderedByDate(Long ownerId) { return repository.findByOwner_IdOrderByAwardedAtDesc(ownerId); }
}
