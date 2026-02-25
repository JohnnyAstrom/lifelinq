package app.lifelinq.features.group.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface InvitationRepository {
    void save(Invitation invitation);

    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findById(UUID id);

    boolean existsByToken(String token);

    List<Invitation> findActive();

    Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail);
}
