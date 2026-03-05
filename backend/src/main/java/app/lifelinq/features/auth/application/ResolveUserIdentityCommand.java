package app.lifelinq.features.auth.application;

import app.lifelinq.features.auth.domain.AuthProvider;

public record ResolveUserIdentityCommand(
        LoginMethod loginMethod,
        AuthProvider provider,
        String subject,
        String email,
        boolean emailVerified
) {
    public enum LoginMethod {
        OAUTH,
        MAGIC_LINK,
        DEV
    }
}

