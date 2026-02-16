package app.lifelinq.features.documents.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    void save(DocumentItem item);

    List<DocumentItem> findByHouseholdId(UUID householdId, Optional<String> q);
}
