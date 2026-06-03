package com.eciwise.todo.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByOwner_IdOrderByNameAsc(Long ownerId);

    Optional<Tag> findByIdAndOwner_Id(Long id, Long ownerId);

    Optional<Tag> findByOwner_IdAndName(Long ownerId, String name);
}
