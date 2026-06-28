package com.eciwise.todo.user.application.port.out;

import com.eciwise.todo.user.domain.model.AppUser;

import java.util.Optional;

public interface UserPort {
    Optional<AppUser> findByExternalId(String externalId);
    AppUser save(AppUser user);
}
