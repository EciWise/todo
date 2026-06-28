package com.eciwise.todo.user.infrastructure.persistence;

import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserJpaRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByExternalId(String externalId);
}
