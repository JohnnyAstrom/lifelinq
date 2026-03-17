package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeImportPort;

public final class HttpRecipeImportPort implements RecipeImportPort {
    private final RecipeImportHttpFetcher fetcher;
    private final RecipeImportHtmlParser parser;

    public HttpRecipeImportPort(
            RecipeImportHttpFetcher fetcher,
            RecipeImportHtmlParser parser
    ) {
        this.fetcher = fetcher;
        this.parser = parser;
    }

    @Override
    public ParsedRecipeImportData importFromUrl(String url) {
        FetchedRecipeDocument document = fetcher.fetch(url);
        return parser.parse(document);
    }
}
