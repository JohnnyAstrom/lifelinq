package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.InvitationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationJpaRepository extends JpaRepository<InvitationEntity, UUID> {
    Optional<InvitationEntity> findByToken(String token);

    boolean existsByToken(String token);

    List<InvitationEntity> findByStatus(InvitationStatus status);

    Optional<InvitationEntity> findByHouseholdIdAndInviteeEmailAndStatus(
            UUID householdId,
            String inviteeEmail,
            InvitationStatus status
    );
}
