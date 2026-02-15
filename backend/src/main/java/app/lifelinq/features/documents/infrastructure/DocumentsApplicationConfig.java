package app.lifelinq.features.documents.infrastructure;

import app.lifelinq.features.documents.application.DocumentsApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentsApplicationConfig {

    @Bean
    public DocumentsApplicationService documentsApplicationService() {
        return DocumentsApplicationService.create();
    }
}
