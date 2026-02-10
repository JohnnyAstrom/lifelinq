package app.lifelinq.features.household.domain;

import java.util.Optional;

public interface InvitationRepository {
    void save(Invitation invitation);

    Optional<Invitation> findByToken(String token);

    boolean existsByToken(String token);
}
