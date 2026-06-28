package com.eciwise.todo.shared.auth;

public record AuthenticatedUser(
        String externalId,
        String email,
        String firstName,
        String lastName,
        String role
) {}
