package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JpaMembershipRepositoryAdapter implements MembershipRepository {
    private final MembershipJpaRepository membershipJpaRepository;
    private final MembershipMapper mapper;

    public JpaMembershipRepositoryAdapter(MembershipJpaRepository membershipJpaRepository, MembershipMapper mapper) {
        if (membershipJpaRepository == null) {
            throw new IllegalArgumentException("membershipJpaRepository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.membershipJpaRepository = membershipJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Membership membership) {
        MembershipEntity entity = mapper.toEntity(membership);
        membershipJpaRepository.save(entity);
    }

    @Override
    public List<Membership> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<Membership> result = new ArrayList<>();
        for (MembershipEntity entity : membershipJpaRepository.findByIdGroupId(groupId)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }

    @Override
    public List<UUID> findGroupIdsByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<UUID> result = new ArrayList<>();
        for (MembershipEntity entity : membershipJpaRepository.findByIdUserId(userId)) {
            UUID groupId = entity.getId().getGroupId();
            if (!result.contains(groupId)) {
                result.add(groupId);
            }
        }
        return result;
    }

    @Override
    public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return membershipJpaRepository.deleteByIdGroupIdAndIdUserId(groupId, userId) > 0;
    }
}
