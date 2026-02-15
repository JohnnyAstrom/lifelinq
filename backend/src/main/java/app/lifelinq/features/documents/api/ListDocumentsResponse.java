package app.lifelinq.features.documents.api;

import java.util.List;

public final class ListDocumentsResponse {
    private final List<DocumentItemResponse> items;

    public ListDocumentsResponse(List<DocumentItemResponse> items) {
        this.items = items;
    }

    public List<DocumentItemResponse> getItems() {
        return items;
    }
}
