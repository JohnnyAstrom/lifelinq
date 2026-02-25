package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public final class GroupPersistenceAdapter
        implements GroupRepository, MembershipRepository {

    private final InMemoryGroupRepository groupRepository;
    private final InMemoryMembershipRepository membershipRepository;

    public GroupPersistenceAdapter() {
        this.groupRepository = new InMemoryGroupRepository();
        this.membershipRepository = new InMemoryMembershipRepository();
    }

    @Override
    public void save(Group group) {
        groupRepository.save(group);
    }

    @Override
    public Optional<Group> findById(UUID id) {
        return groupRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        groupRepository.deleteById(id);
    }

    @Override
    public void save(Membership membership) {
        membershipRepository.save(membership);
    }

    @Override
    public List<Membership> findByGroupId(UUID groupId) {
        return membershipRepository.findByGroupId(groupId);
    }

    @Override
    public List<Membership> findByUserId(UUID userId) {
        return membershipRepository.findByUserId(userId);
    }

    @Override
    public List<UUID> findGroupIdsByUserId(UUID userId) {
        return membershipRepository.findGroupIdsByUserId(userId);
    }

    @Override
    public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
        return membershipRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        membershipRepository.deleteByUserId(userId);
    }
}
