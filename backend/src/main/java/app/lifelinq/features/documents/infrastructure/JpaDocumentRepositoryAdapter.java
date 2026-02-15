package app.lifelinq.features.documents.infrastructure;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JpaDocumentRepositoryAdapter implements DocumentRepository {
    private final DocumentJpaRepository repository;
    private final DocumentMapper mapper;

    public JpaDocumentRepositoryAdapter(DocumentJpaRepository repository, DocumentMapper mapper) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(DocumentItem item) {
        repository.save(mapper.toEntity(item));
    }

    @Override
    public List<DocumentItem> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<DocumentItem> items = new ArrayList<>();
        for (DocumentEntity entity : repository.findByHouseholdId(householdId)) {
            items.add(mapper.toDomain(entity));
        }
        return items;
    }
}
