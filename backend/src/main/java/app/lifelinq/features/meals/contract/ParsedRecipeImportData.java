package app.lifelinq.features.meals.contract;

import java.util.List;

public record ParsedRecipeImportData(
        String name,
        String sourceName,
        String sourceUrl,
        String servings,
        String shortNote,
        String instructions,
        List<String> ingredientLines
) {
    public ParsedRecipeImportData(
            String name,
            String sourceName,
            String sourceUrl,
            String shortNote,
            String instructions,
            List<String> ingredientLines
    ) {
        this(name, sourceName, sourceUrl, null, shortNote, instructions, ingredientLines);
    }
}
