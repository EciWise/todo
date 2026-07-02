package com.eciwise.todo.user.application.service;

import com.eciwise.todo.shared.auth.AuthenticatedUser;
import com.eciwise.todo.user.application.port.out.UserPort;
import com.eciwise.todo.user.domain.model.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final UserPort userPort;

    public CurrentUserService(UserPort userPort) {
        this.userPort = userPort;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppUser getOrCreate() {
        AuthenticatedUser principal = currentPrincipal();
        return userPort.findByExternalId(principal.externalId())
                .map(existing -> syncSnapshot(existing, principal))
                .orElseGet(() -> userPort.save(AppUser.builder()
                        .externalId(principal.externalId())
                        .email(principal.email())
                        .firstName(principal.firstName())
                        .lastName(principal.lastName())
                        .role(principal.role())
                        .build()));
    }

    private AppUser syncSnapshot(AppUser existing, AuthenticatedUser principal) {
        boolean changed = false;
        if (!eq(existing.getEmail(), principal.email())) { existing.setEmail(principal.email()); changed = true; }
        if (!eq(existing.getFirstName(), principal.firstName())) { existing.setFirstName(principal.firstName()); changed = true; }
        if (!eq(existing.getLastName(), principal.lastName())) { existing.setLastName(principal.lastName()); changed = true; }
        if (!eq(existing.getRole(), principal.role())) { existing.setRole(principal.role()); changed = true; }
        return changed ? userPort.save(existing) : existing;
    }

    private AuthenticatedUser currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser p)) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto");
        }
        return p;
    }

    private boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
