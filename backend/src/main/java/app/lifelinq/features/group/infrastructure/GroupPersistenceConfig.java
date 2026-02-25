package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.MembershipRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("persistence")
public class GroupPersistenceConfig {

    @Bean
    public GroupMapper groupMapper() {
        return new GroupMapper();
    }

    @Bean
    public MembershipMapper membershipMapper() {
        return new MembershipMapper();
    }

    @Bean
    public InvitationMapper invitationMapper() {
        return new InvitationMapper();
    }

    @Bean
    public GroupRepository groupRepository(
            GroupJpaRepository groupJpaRepository,
            GroupMapper groupMapper
    ) {
        return new JpaGroupRepositoryAdapter(groupJpaRepository, groupMapper);
    }

    @Bean
    public MembershipRepository membershipRepository(
            MembershipJpaRepository membershipJpaRepository,
            MembershipMapper membershipMapper
    ) {
        return new JpaMembershipRepositoryAdapter(membershipJpaRepository, membershipMapper);
    }

    @Bean
    public InvitationRepository invitationRepository(
            InvitationJpaRepository invitationJpaRepository,
            InvitationMapper invitationMapper
    ) {
        return new JpaInvitationRepositoryAdapter(invitationJpaRepository, invitationMapper);
    }

    @Bean
    public GroupAccountDeletionGovernancePort groupAccountDeletionGovernancePort(
            MembershipRepository membershipRepository,
            GroupRepository groupRepository
    ) {
        return new UserAccountDeletionGovernanceAdapter(membershipRepository, groupRepository);
    }
}
