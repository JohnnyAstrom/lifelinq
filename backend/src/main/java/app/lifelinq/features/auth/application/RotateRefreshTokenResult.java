package app.lifelinq.features.auth.application;

import java.util.UUID;

record RotateRefreshTokenResult(UUID userId, String refreshToken) {
}
