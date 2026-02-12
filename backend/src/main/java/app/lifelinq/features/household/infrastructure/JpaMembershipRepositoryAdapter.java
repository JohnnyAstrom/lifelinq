package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
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
    public List<Membership> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<Membership> result = new ArrayList<>();
        for (MembershipEntity entity : membershipJpaRepository.findByIdHouseholdId(householdId)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }

    @Override
    public List<UUID> findHouseholdIdsByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<UUID> result = new ArrayList<>();
        for (MembershipEntity entity : membershipJpaRepository.findByIdUserId(userId)) {
            UUID householdId = entity.getId().getHouseholdId();
            if (!result.contains(householdId)) {
                result.add(householdId);
            }
        }
        return result;
    }

    @Override
    public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return membershipJpaRepository.deleteByIdHouseholdIdAndIdUserId(householdId, userId) > 0;
    }
}
