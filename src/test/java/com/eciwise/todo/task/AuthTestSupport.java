package com.eciwise.todo.task;

import com.eciwise.todo.auth.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/** Utilidad para autenticar un usuario en el SecurityContext durante los tests. */
final class AuthTestSupport {

    private AuthTestSupport() {
    }

    static void authenticate(String externalId, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(
                externalId, externalId + "@test.com", "Nombre", "Apellido", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(role))));
    }
}
