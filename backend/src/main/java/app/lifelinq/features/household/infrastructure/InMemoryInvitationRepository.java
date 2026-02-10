package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryInvitationRepository implements InvitationRepository {
    private final Map<String, Invitation> invitationsByToken = new HashMap<>();

    @Override
    public void save(Invitation invitation) {
        if (invitation == null) {
            throw new IllegalArgumentException("invitation must not be null");
        }
        invitationsByToken.put(invitation.getToken(), invitation);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return Optional.ofNullable(invitationsByToken.get(token));
    }

    @Override
    public boolean existsByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return invitationsByToken.containsKey(token);
    }

    @Override
    public List<Invitation> findPending() {
        List<Invitation> result = new ArrayList<>();
        for (Invitation invitation : invitationsByToken.values()) {
            if (invitation.getStatus() == InvitationStatus.PENDING) {
                result.add(invitation);
            }
        }
        return result;
    }
}
