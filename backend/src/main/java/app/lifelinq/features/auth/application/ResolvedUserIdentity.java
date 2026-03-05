package app.lifelinq.features.auth.application;

import java.util.UUID;

public record ResolvedUserIdentity(UUID userId, String normalizedEmail, boolean newUser) {
}

