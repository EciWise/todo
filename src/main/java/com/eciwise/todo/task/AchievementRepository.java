package com.eciwise.todo.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByOwner_IdOrderByAwardedAtDesc(Long ownerId);
}
