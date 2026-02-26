package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserProfileRead {
    UserProfileView getProfile(UUID userId);
}
