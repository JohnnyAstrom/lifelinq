package app.lifelinq.features.household.domain;

import java.util.Optional;
import java.util.List;

public interface InvitationRepository {
    void save(Invitation invitation);

    Optional<Invitation> findByToken(String token);

    boolean existsByToken(String token);

    List<Invitation> findPending();
}
