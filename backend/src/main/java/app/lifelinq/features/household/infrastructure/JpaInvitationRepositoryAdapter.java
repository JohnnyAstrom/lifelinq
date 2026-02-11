package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JpaInvitationRepositoryAdapter implements InvitationRepository {
    private final InvitationJpaRepository invitationJpaRepository;
    private final InvitationMapper mapper;

    public JpaInvitationRepositoryAdapter(InvitationJpaRepository invitationJpaRepository, InvitationMapper mapper) {
        if (invitationJpaRepository == null) {
            throw new IllegalArgumentException("invitationJpaRepository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.invitationJpaRepository = invitationJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Invitation invitation) {
        InvitationEntity entity = mapper.toEntity(invitation);
        invitationJpaRepository.save(entity);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return invitationJpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public boolean existsByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return invitationJpaRepository.existsByToken(token);
    }

    @Override
    public List<Invitation> findPending() {
        List<Invitation> result = new ArrayList<>();
        for (InvitationEntity entity : invitationJpaRepository.findByStatus(InvitationStatus.PENDING)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }
}
