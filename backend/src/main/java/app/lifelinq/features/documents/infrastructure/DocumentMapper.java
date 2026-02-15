package app.lifelinq.features.documents.infrastructure;

import app.lifelinq.features.documents.domain.DocumentItem;
import java.util.ArrayList;

final class DocumentMapper {

    DocumentEntity toEntity(DocumentItem item) {
        return new DocumentEntity(
                item.getId(),
                item.getHouseholdId(),
                item.getCreatedByUserId(),
                item.getTitle(),
                item.getNotes(),
                item.getDate(),
                item.getCategory(),
                item.getTags(),
                item.getExternalLink(),
                item.getCreatedAt()
        );
    }

    DocumentItem toDomain(DocumentEntity entity) {
        return new DocumentItem(
                entity.getId(),
                entity.getHouseholdId(),
                entity.getCreatedByUserId(),
                entity.getTitle(),
                entity.getNotes(),
                entity.getDate(),
                entity.getCategory(),
                entity.getTags() == null ? new ArrayList<>() : entity.getTags(),
                entity.getExternalLink(),
                entity.getCreatedAt()
        );
    }
}
