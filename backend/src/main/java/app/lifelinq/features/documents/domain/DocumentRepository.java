package app.lifelinq.features.documents.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    void save(DocumentItem item);

    List<DocumentItem> findByGroupId(UUID groupId, Optional<String> q);

    boolean deleteByIdAndGroupId(UUID id, UUID groupId);
}
