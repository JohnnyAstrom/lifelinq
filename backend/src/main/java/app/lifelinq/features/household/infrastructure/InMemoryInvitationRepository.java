package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Invitation> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        for (Invitation invitation : invitationsByToken.values()) {
            if (id.equals(invitation.getId())) {
                return Optional.of(invitation);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return invitationsByToken.containsKey(token);
    }

    @Override
    public List<Invitation> findActive() {
        List<Invitation> result = new ArrayList<>();
        for (Invitation invitation : invitationsByToken.values()) {
            if (invitation.getStatus() == InvitationStatus.ACTIVE) {
                result.add(invitation);
            }
        }
        return result;
    }

    @Override
    public Optional<Invitation> findActiveByHouseholdIdAndInviteeEmail(UUID householdId, String inviteeEmail) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (inviteeEmail == null || inviteeEmail.isBlank()) {
            throw new IllegalArgumentException("inviteeEmail must not be blank");
        }
        for (Invitation invitation : invitationsByToken.values()) {
            if (invitation.getStatus() == InvitationStatus.ACTIVE
                    && householdId.equals(invitation.getHouseholdId())
                    && inviteeEmail.equals(invitation.getInviteeEmail())) {
                return Optional.of(invitation);
            }
        }
        return Optional.empty();
    }
}
