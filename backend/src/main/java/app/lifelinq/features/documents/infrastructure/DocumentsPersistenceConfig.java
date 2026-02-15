package app.lifelinq.features.documents.infrastructure;

import app.lifelinq.features.documents.domain.DocumentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentsPersistenceConfig {

    @Bean
    DocumentMapper documentMapper() {
        return new DocumentMapper();
    }

    @Bean
    public DocumentRepository documentRepository(
            DocumentJpaRepository repository,
            DocumentMapper mapper
    ) {
        return new JpaDocumentRepositoryAdapter(repository, mapper);
    }
}
