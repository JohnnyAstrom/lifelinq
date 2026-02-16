package app.lifelinq.features.documents.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, UUID> {
    @EntityGraph(attributePaths = "tags")
    List<DocumentEntity> findByHouseholdIdOrderByCreatedAtDescIdAsc(UUID householdId);

    @EntityGraph(attributePaths = "tags")
    @Query("""
            select d
            from DocumentEntity d
            where d.householdId = :householdId
              and (
                  lower(d.title) like lower(concat('%', :query, '%'))
                  or lower(coalesce(d.notes, '')) like lower(concat('%', :query, '%'))
              )
            order by d.createdAt desc, d.id asc
            """)
    List<DocumentEntity> searchByHouseholdIdAndText(@Param("householdId") UUID householdId, @Param("query") String query);
}
