package app.lifelinq.features.meals.infrastructure;

record FetchedRecipeDocument(
        String sourceUrl,
        String html
) {}
