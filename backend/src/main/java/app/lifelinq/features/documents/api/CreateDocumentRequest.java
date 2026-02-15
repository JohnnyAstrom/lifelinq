package app.lifelinq.features.documents.api;

import java.time.LocalDate;
import java.util.List;

public final class CreateDocumentRequest {
    private String title;
    private String notes;
    private LocalDate date;
    private String category;
    private List<String> tags;
    private String externalLink;

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getExternalLink() {
        return externalLink;
    }
}
