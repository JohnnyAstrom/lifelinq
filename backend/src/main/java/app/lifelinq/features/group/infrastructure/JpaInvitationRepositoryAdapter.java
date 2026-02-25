package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Invitation> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return invitationJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        return invitationJpaRepository.existsByToken(token);
    }

    @Override
    public List<Invitation> findActive() {
        List<Invitation> result = new ArrayList<>();
        for (InvitationEntity entity : invitationJpaRepository.findByStatus(InvitationStatus.ACTIVE)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }

    @Override
    public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (inviteeEmail == null || inviteeEmail.isBlank()) {
            throw new IllegalArgumentException("inviteeEmail must not be blank");
        }
        return invitationJpaRepository
                .findByGroupIdAndInviteeEmailAndStatus(groupId, inviteeEmail, InvitationStatus.ACTIVE)
                .map(mapper::toDomain);
    }
}
