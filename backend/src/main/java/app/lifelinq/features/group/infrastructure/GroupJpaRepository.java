package app.lifelinq.features.group.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupJpaRepository extends JpaRepository<GroupEntity, UUID> {
}
