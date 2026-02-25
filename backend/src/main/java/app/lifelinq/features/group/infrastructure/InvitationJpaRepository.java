package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.InvitationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationJpaRepository extends JpaRepository<InvitationEntity, UUID> {
    Optional<InvitationEntity> findByToken(String token);

    boolean existsByToken(String token);

    List<InvitationEntity> findByStatus(InvitationStatus status);

    Optional<InvitationEntity> findByGroupIdAndInviteeEmailAndStatus(
            UUID groupId,
            String inviteeEmail,
            InvitationStatus status
    );
}
