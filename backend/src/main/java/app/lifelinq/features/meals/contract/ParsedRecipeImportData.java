package app.lifelinq.features.meals.contract;

import java.util.List;

public record ParsedRecipeImportData(
        String name,
        String sourceName,
        String sourceUrl,
        String shortNote,
        String instructions,
        List<String> ingredientLines
) {}
