package app.lifelinq.features.meals.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeDraftJpaRepository extends JpaRepository<RecipeDraftEntity, UUID> {
    @EntityGraph(attributePaths = "ingredients")
    Optional<RecipeDraftEntity> findByIdAndGroupId(UUID id, UUID groupId);

    @Modifying
    @Query("delete from RecipeDraftEntity recipeDraft where recipeDraft.id = :draftId and recipeDraft.groupId = :groupId")
    void deleteByIdAndGroupId(@Param("draftId") UUID draftId, @Param("groupId") UUID groupId);
}
