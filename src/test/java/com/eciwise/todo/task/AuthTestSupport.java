package com.eciwise.todo.task;

import com.eciwise.todo.shared.auth.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

final class AuthTestSupport {
    private AuthTestSupport() {}

    static void authenticate(String externalId, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(
                externalId, externalId + "@test.com", "Nombre", "Apellido", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role))));
    }
}
