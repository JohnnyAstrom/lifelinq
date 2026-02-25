package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class InMemoryMembershipRepository implements MembershipRepository {
    private final List<Membership> memberships = new ArrayList<>();

    @Override
    public void save(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("membership must not be null");
        }
        memberships.add(membership);
    }

    @Override
    public List<Membership> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<Membership> result = new ArrayList<>();
        for (Membership membership : memberships) {
            if (groupId.equals(membership.getGroupId())) {
                result.add(membership);
            }
        }
        return result;
    }

    @Override
    public List<Membership> findByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<Membership> result = new ArrayList<>();
        for (Membership membership : memberships) {
            if (userId.equals(membership.getUserId())) {
                result.add(membership);
            }
        }
        return result;
    }

    @Override
    public List<UUID> findGroupIdsByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        LinkedHashSet<UUID> ids = new LinkedHashSet<>();
        for (Membership membership : memberships) {
            if (userId.equals(membership.getUserId())) {
                ids.add(membership.getGroupId());
            }
        }
        return new ArrayList<>(ids);
    }

    @Override
    public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        for (int i = 0; i < memberships.size(); i++) {
            Membership membership = memberships.get(i);
            if (groupId.equals(membership.getGroupId()) && userId.equals(membership.getUserId())) {
                memberships.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        memberships.removeIf(membership -> userId.equals(membership.getUserId()));
    }
}
