package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.application.RecipeImportFailedException;
import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RecipeImportHtmlParser {
    private static final Pattern JSON_LD_SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(?<content>.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern META_PROPERTY_PATTERN = Pattern.compile(
            "<meta[^>]+(?:property|name)=[\"'](?<key>[^\"']+)[\"'][^>]+content=[\"'](?<value>[^\"']*)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "<title>(?<value>.*?)</title>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern SPLIT_LINE_PATTERN = Pattern.compile("\\s*[\\r\\n]+\\s*");
    private static final Pattern INSTRUCTION_PREFIX_PATTERN = Pattern.compile(
            "^(?:step\\s*\\d+[:.)-]?|\\d+[.)-])\\s*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern INLINE_NUMBERED_STEP_PATTERN = Pattern.compile(
            "(?:^|\\s)(?:step\\s*)?\\d+[.):-]\\s+",
            Pattern.CASE_INSENSITIVE
    );

    private final ObjectMapper objectMapper;

    RecipeImportHtmlParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ParsedRecipeImportData parse(FetchedRecipeDocument document) {
        JsonNode structuredRecipe = extractRecipeNode(document.html());
        if (structuredRecipe == null) {
            throw new RecipeImportFailedException("Could not find structured recipe data at that URL");
        }

        Metadata metadata = extractMetadata(document.html(), document.sourceUrl());
        String name = firstNonBlank(
                textValue(structuredRecipe.get("name")),
                metadata.title()
        );
        List<String> ingredients = extractIngredientLines(firstNonNull(
                structuredRecipe.get("recipeIngredient"),
                structuredRecipe.get("ingredients")
        ));
        String instructions = extractInstructions(firstNonNull(
                structuredRecipe.get("recipeInstructions"),
                structuredRecipe.get("instructions")
        ));
        String sourceName = firstNonBlank(
                extractSourceName(structuredRecipe),
                metadata.siteName(),
                metadata.hostLabel()
        );
        String servings = extractServings(structuredRecipe);
        String shortNote = firstNonBlank(
                textValue(structuredRecipe.get("description")),
                metadata.description()
        );

        if (ingredients.isEmpty()) {
            throw new RecipeImportFailedException("Imported recipe is missing recipe ingredients");
        }

        return new ParsedRecipeImportData(
                name,
                sourceName,
                document.sourceUrl(),
                servings,
                shortNote,
                instructions,
                ingredients
        );
    }

    private JsonNode extractRecipeNode(String html) {
        Matcher matcher = JSON_LD_SCRIPT_PATTERN.matcher(html);
        while (matcher.find()) {
            String content = matcher.group("content");
            if (content == null || content.isBlank()) {
                continue;
            }
            JsonNode node = tryReadJson(content);
            if (node == null) {
                continue;
            }
            JsonNode recipeNode = findRecipeNode(node);
            if (recipeNode != null) {
                return recipeNode;
            }
        }
        return null;
    }

    private JsonNode tryReadJson(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (IOException ex) {
            return null;
        }
    }

    private JsonNode findRecipeNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode recipe = findRecipeNode(child);
                if (recipe != null) {
                    return recipe;
                }
            }
            return null;
        }
        if (node.isObject()) {
            if (isRecipeType(node.get("@type"))) {
                return node;
            }
            JsonNode graph = node.get("@graph");
            if (graph != null) {
                JsonNode recipe = findRecipeNode(graph);
                if (recipe != null) {
                    return recipe;
                }
            }
            for (Map.Entry<String, JsonNode> entry : iterable(node.fields())) {
                JsonNode recipe = findRecipeNode(entry.getValue());
                if (recipe != null) {
                    return recipe;
                }
            }
        }
        return null;
    }

    private boolean isRecipeType(JsonNode typeNode) {
        if (typeNode == null || typeNode.isNull()) {
            return false;
        }
        if (typeNode.isTextual()) {
            return "recipe".equalsIgnoreCase(typeNode.asText());
        }
        if (typeNode.isArray()) {
            for (JsonNode item : typeNode) {
                if (item.isTextual() && "recipe".equalsIgnoreCase(item.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> extractIngredientLines(JsonNode ingredientsNode) {
        List<String> ingredients = new ArrayList<>();
        if (ingredientsNode == null || ingredientsNode.isNull()) {
            return ingredients;
        }
        if (ingredientsNode.isTextual()) {
            appendNormalizedIngredientText(ingredientsNode.asText(), ingredients);
            return ingredients;
        }
        if (ingredientsNode.isArray()) {
            for (JsonNode ingredientNode : ingredientsNode) {
                appendIngredientNode(ingredientNode, ingredients);
            }
            return ingredients;
        }
        if (ingredientsNode.isObject()) {
            appendIngredientNode(ingredientsNode, ingredients);
        }
        return ingredients;
    }

    private void appendIngredientNode(JsonNode ingredientNode, List<String> ingredients) {
        if (ingredientNode == null || ingredientNode.isNull()) {
            return;
        }
        if (ingredientNode.isTextual()) {
            appendNormalizedIngredientText(ingredientNode.asText(), ingredients);
            return;
        }
        if (ingredientNode.isArray()) {
            for (JsonNode child : ingredientNode) {
                appendIngredientNode(child, ingredients);
            }
            return;
        }
        if (ingredientNode.isObject()) {
            String ingredient = firstNonBlank(
                    textValue(ingredientNode.get("text")),
                    textValue(ingredientNode.get("name")),
                    textValue(ingredientNode.get("value"))
            );
            appendNormalizedIngredientText(ingredient, ingredients);

            JsonNode item = ingredientNode.get("item");
            if (item != null) {
                appendIngredientNode(item, ingredients);
            }

            JsonNode itemListElement = ingredientNode.get("itemListElement");
            if (itemListElement != null) {
                appendIngredientNode(itemListElement, ingredients);
            }
        }
    }

    private String extractInstructions(JsonNode instructionsNode) {
        if (instructionsNode == null || instructionsNode.isNull()) {
            return null;
        }
        if (instructionsNode.isTextual()) {
            return normalizeInstructionBlob(instructionsNode.asText());
        }
        List<String> steps = new ArrayList<>();
        collectInstructionSteps(instructionsNode, steps);
        if (steps.isEmpty()) {
            return null;
        }
        if (steps.size() == 1) {
            return steps.get(0);
        }

        List<String> numberedSteps = new ArrayList<>();
        for (int index = 0; index < steps.size(); index++) {
            numberedSteps.add((index + 1) + ". " + steps.get(index));
        }
        return String.join("\n", numberedSteps);
    }

    private void collectInstructionSteps(JsonNode node, List<String> steps) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual()) {
            appendInstructionText(node.asText(), steps);
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectInstructionSteps(child, steps);
            }
            return;
        }
        if (node.isObject()) {
            String text = firstNonBlank(
                    textValue(node.get("text")),
                    textValue(node.get("name")),
                    textValue(node.get("value"))
            );
            int before = steps.size();
            appendInstructionText(text, steps);
            if (steps.size() > before) {
                return;
            }
            JsonNode item = node.get("item");
            if (item != null) {
                collectInstructionSteps(item, steps);
                if (steps.size() > before) {
                    return;
                }
            }
            JsonNode itemListElement = node.get("itemListElement");
            if (itemListElement != null) {
                collectInstructionSteps(itemListElement, steps);
            }
        }
    }

    private String extractSourceName(JsonNode recipeNode) {
        return firstNonBlank(
                nestedName(recipeNode.get("publisher")),
                nestedName(recipeNode.get("author")),
                nestedName(recipeNode.get("creator"))
        );
    }

    private String extractServings(JsonNode recipeNode) {
        return firstNonBlank(
                nestedText(recipeNode.get("recipeYield")),
                nestedText(recipeNode.get("yield"))
        );
    }

    private String nestedText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                String nested = nestedText(item);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        if (node.isTextual()) {
            return normalizeInlineText(node.asText());
        }
        return firstNonBlank(
                textValue(node.get("text")),
                textValue(node.get("name")),
                textValue(node.get("value"))
        );
    }

    private String nestedName(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                String nested = nestedName(item);
                if (nested != null) {
                    return nested;
                }
            }
            return null;
        }
        if (node.isTextual()) {
            return normalizeInlineText(node.asText());
        }
        return normalizeInlineText(textValue(node.get("name")));
    }

    private Metadata extractMetadata(String html, String sourceUrl) {
        String title = null;
        String siteName = null;
        String description = null;

        Matcher metaMatcher = META_PROPERTY_PATTERN.matcher(html);
        while (metaMatcher.find()) {
            String key = metaMatcher.group("key");
            String value = normalizeInlineText(metaMatcher.group("value"));
            if (value == null || key == null) {
                continue;
            }
            String normalizedKey = key.toLowerCase(Locale.ROOT);
            if (title == null && (normalizedKey.equals("og:title") || normalizedKey.equals("twitter:title"))) {
                title = value;
            }
            if (siteName == null && normalizedKey.equals("og:site_name")) {
                siteName = value;
            }
            if (description == null && (
                    normalizedKey.equals("description")
                    || normalizedKey.equals("og:description")
                    || normalizedKey.equals("twitter:description")
            )) {
                description = value;
            }
        }

        if (title == null) {
            Matcher titleMatcher = TITLE_PATTERN.matcher(html);
            if (titleMatcher.find()) {
                title = normalizeInlineText(titleMatcher.group("value"));
            }
        }

        URI uri = URI.create(sourceUrl);
        String host = uri.getHost();
        String hostLabel = host == null ? null : (host.startsWith("www.") ? host.substring(4) : host);

        return new Metadata(title, siteName, description, hostLabel);
    }

    private String textValue(JsonNode node) {
        return node != null && node.isValueNode() ? node.asText() : null;
    }

    private String normalizeInstructions(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace("\r", "\n").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeInstructionBlob(String value) {
        List<String> steps = splitInstructionText(value);
        if (steps.isEmpty()) {
            return null;
        }
        if (steps.size() == 1) {
            return steps.get(0);
        }

        List<String> numberedSteps = new ArrayList<>();
        for (int index = 0; index < steps.size(); index++) {
            numberedSteps.add((index + 1) + ". " + steps.get(index));
        }
        return String.join("\n", numberedSteps);
    }

    private void appendInstructionText(String value, List<String> steps) {
        steps.addAll(splitInstructionText(value));
    }

    private List<String> splitInstructionText(String value) {
        String normalized = normalizeInstructions(value);
        if (normalized == null) {
            return List.of();
        }

        List<String> newlineSteps = splitInstructionLines(normalized);
        if (newlineSteps.size() > 1) {
            return newlineSteps;
        }

        List<String> numberedSteps = splitInlineNumberedInstructions(normalized);
        if (numberedSteps.size() > 1) {
            return numberedSteps;
        }

        String singleStep = normalizeInstructionStep(normalized);
        return singleStep == null ? List.of() : List.of(singleStep);
    }

    private List<String> splitInstructionLines(String value) {
        if (!value.contains("\n")) {
            return List.of();
        }

        List<String> steps = new ArrayList<>();
        for (String part : value.split("\\n+")) {
            String normalized = normalizeInstructionStep(part);
            if (normalized != null) {
                steps.add(normalized);
            }
        }
        return steps.size() > 1 ? steps : List.of();
    }

    private List<String> splitInlineNumberedInstructions(String value) {
        Matcher matcher = INLINE_NUMBERED_STEP_PATTERN.matcher(value);
        List<Integer> starts = new ArrayList<>();
        while (matcher.find()) {
            int start = matcher.start();
            if (start < value.length() && Character.isWhitespace(value.charAt(start))) {
                start += 1;
            }
            starts.add(start);
        }
        if (starts.size() < 2) {
            return List.of();
        }

        List<String> steps = new ArrayList<>();
        for (int index = 0; index < starts.size(); index++) {
            int start = starts.get(index);
            int end = index + 1 < starts.size() ? starts.get(index + 1) : value.length();
            String normalized = normalizeInstructionStep(value.substring(start, end));
            if (normalized != null) {
                steps.add(normalized);
            }
        }
        return steps.size() > 1 ? steps : List.of();
    }

    private String normalizeInstructionStep(String value) {
        String normalized = normalizeInlineText(value);
        if (normalized == null) {
            return null;
        }
        normalized = INSTRUCTION_PREFIX_PATTERN.matcher(normalized).replaceFirst("").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeInlineText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value
                .replaceAll("<[^>]+>", " ")
                .replace('\u00A0', ' ')
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim()
                .replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    private void appendNormalizedIngredientText(String value, List<String> ingredients) {
        if (value == null) {
            return;
        }
        String normalizedValue = value.replace('\u2022', '\n');
        for (String part : SPLIT_LINE_PATTERN.split(normalizedValue)) {
            String normalized = normalizeIngredientLine(part);
            if (normalized != null) {
                ingredients.add(normalized);
            }
        }
    }

    private String normalizeIngredientLine(String value) {
        String normalized = normalizeInlineText(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replaceFirst("^[\\-•*]+\\s*", "");
        normalized = normalized.replaceFirst("^\\d{1,2}[\\.)]\\s+", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
        return () -> iterator;
    }

    private JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalizeInlineText(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private record Metadata(
            String title,
            String siteName,
            String description,
            String hostLabel
    ) {
    }
}
