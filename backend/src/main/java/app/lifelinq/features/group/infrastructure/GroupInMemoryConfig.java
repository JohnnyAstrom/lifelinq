package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.application.InvitationTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!persistence")
public class GroupInMemoryConfig {

    @Bean
    public GroupRepository groupRepository() {
        return new InMemoryGroupRepository();
    }

    @Bean
    public MembershipRepository membershipRepository() {
        return new InMemoryMembershipRepository();
    }

    @Bean
    public InvitationRepository invitationRepository() {
        return new InMemoryInvitationRepository();
    }

    @Bean
    public InvitationTokenGenerator invitationTokenGenerator() {
        return new InMemoryInvitationTokenGenerator();
    }

    @Bean
    public GroupAccountDeletionGovernancePort groupAccountDeletionGovernancePort(
            MembershipRepository membershipRepository,
            GroupRepository groupRepository
    ) {
        return new UserAccountDeletionGovernanceAdapter(membershipRepository, groupRepository);
    }

    @Bean
    public UserGroupMembershipLookup userGroupMembershipLookup(MembershipRepository membershipRepository) {
        return new UserGroupMembershipLookupAdapter(membershipRepository);
    }
}
