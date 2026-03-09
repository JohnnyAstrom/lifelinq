package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.GroupMembershipReadPort;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.group.application.GroupInvitationMailSender;
import app.lifelinq.features.group.application.InvitationTokenGenerator;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.MembershipRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

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
    public InvitationTokenGenerator invitationTokenGenerator() {
        return new InMemoryInvitationTokenGenerator();
    }

    @Bean
    @Profile("dev")
    public GroupInvitationMailSender groupInvitationMailSender(
            JavaMailSender mailSender,
            @Value("${lifelinq.auth.mail.from:}") String configuredFrom,
            @Value("${spring.mail.username:}") String smtpUsername
    ) {
        String fromAddress = configuredFrom != null && !configuredFrom.isBlank() ? configuredFrom : smtpUsername;
        return new SmtpGroupInvitationMailSender(mailSender, fromAddress);
    }

    @Bean
    @Profile("!dev")
    public GroupInvitationMailSender productionGroupInvitationMailSender() {
        return new FailFastGroupInvitationMailSender();
    }

    @Bean
    public GroupAccountDeletionGovernancePort groupAccountDeletionGovernancePort(
            MembershipRepository membershipRepository,
            GroupRepository groupRepository
    ) {
        return new UserAccountDeletionGovernanceAdapter(membershipRepository, groupRepository);
    }

    @Bean
    public UserGroupMembershipLookup userGroupMembershipLookup(
            MembershipRepository membershipRepository,
            GroupRepository groupRepository
    ) {
        return new UserGroupMembershipLookupAdapter(membershipRepository, groupRepository);
    }

    @Bean
    public GroupMembershipReadPort groupMembershipReadPort(
            MembershipRepository membershipRepository
    ) {
        return new GroupMembershipReadAdapter(membershipRepository);
    }
}
