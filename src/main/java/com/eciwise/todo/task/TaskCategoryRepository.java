package com.eciwise.todo.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {

    List<TaskCategory> findByOwner_IdOrderByNameAsc(Long ownerId);

    Optional<TaskCategory> findByIdAndOwner_Id(Long id, Long ownerId);
}
