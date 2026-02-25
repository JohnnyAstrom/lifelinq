package app.lifelinq.features.user.contract;

import java.util.UUID;

public interface UserAccountDeletion {
    void deleteAccount(UUID userId);
}
