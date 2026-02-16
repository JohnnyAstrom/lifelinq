package app.lifelinq.features.documents.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.documents.application.DocumentsApplicationService;
import app.lifelinq.features.documents.domain.DocumentItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentsController {
    private final DocumentsApplicationService documentsApplicationService;

    public DocumentsController(DocumentsApplicationService documentsApplicationService) {
        this.documentsApplicationService = documentsApplicationService;
    }

    @PostMapping("/documents")
    public ResponseEntity<?> create(@RequestBody CreateDocumentRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new CreateDocumentResponse(
                documentsApplicationService.createDocument(
                        context.getHouseholdId(),
                        context.getUserId(),
                        request.getTitle(),
                        request.getNotes(),
                        request.getDate(),
                        request.getCategory(),
                        request.getTags(),
                        request.getExternalLink()
                )
        ));
    }

    @GetMapping("/documents")
    public ResponseEntity<?> list(@RequestParam(name = "q", required = false) String q) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new ListDocumentsResponse(toResponseItems(
                documentsApplicationService.listDocuments(context.getHouseholdId(), Optional.ofNullable(q))
        )));
    }

    private List<DocumentItemResponse> toResponseItems(List<DocumentItem> items) {
        List<DocumentItemResponse> responses = new ArrayList<>();
        for (DocumentItem item : items) {
            responses.add(new DocumentItemResponse(
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
            ));
        }
        return responses;
    }
}
