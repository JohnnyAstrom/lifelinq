package app.lifelinq.features.documents.infrastructure;

import app.lifelinq.features.documents.application.DocumentsApplicationService;
import app.lifelinq.features.documents.domain.DocumentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentsApplicationConfig {

    @Bean
    public DocumentsApplicationService documentsApplicationService(
            DocumentRepository documentRepository
    ) {
        return DocumentsApplicationService.create(documentRepository);
    }
}
