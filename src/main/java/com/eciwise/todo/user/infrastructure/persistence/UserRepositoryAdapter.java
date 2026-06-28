package com.eciwise.todo.user.infrastructure.persistence;

import com.eciwise.todo.user.application.port.out.UserPort;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserPort {

    private final AppUserJpaRepository repository;

    public UserRepositoryAdapter(AppUserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AppUser> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId);
    }

    @Override
    public AppUser save(AppUser user) {
        return repository.save(user);
    }
}
