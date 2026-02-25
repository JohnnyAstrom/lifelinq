package app.lifelinq.features.documents.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, UUID> {
    @EntityGraph(attributePaths = "tags")
    List<DocumentEntity> findByGroupIdOrderByCreatedAtDescIdAsc(UUID groupId);

    @EntityGraph(attributePaths = "tags")
    @Query("""
            select d
            from DocumentEntity d
            where d.groupId = :groupId
              and (
                  lower(d.title) like lower(concat('%', :query, '%'))
                  or lower(coalesce(d.notes, '')) like lower(concat('%', :query, '%'))
              )
            order by d.createdAt desc, d.id asc
            """)
    List<DocumentEntity> searchByGroupIdAndText(@Param("groupId") UUID groupId, @Param("query") String query);

    @Modifying
    @Transactional
    long deleteByIdAndGroupId(UUID id, UUID groupId);
}
