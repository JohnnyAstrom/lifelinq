package app.lifelinq.features.household.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface InvitationRepository {
    void save(Invitation invitation);

    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findById(UUID id);

    boolean existsByToken(String token);

    List<Invitation> findActive();

    Optional<Invitation> findActiveByHouseholdIdAndInviteeEmail(UUID householdId, String inviteeEmail);
}
