package com.eciwise.todo.task.infrastructure.out.persistence;

import com.eciwise.todo.task.domain.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementJpaRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByOwner_IdOrderByAwardedAtDesc(Long ownerId);
}
