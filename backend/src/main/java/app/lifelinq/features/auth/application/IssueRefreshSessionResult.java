package app.lifelinq.features.auth.application;

import java.util.UUID;

record IssueRefreshSessionResult(UUID sessionId, String refreshToken) {
}

