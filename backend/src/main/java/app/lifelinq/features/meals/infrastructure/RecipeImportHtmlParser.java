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
        List<String> ingredients = extractIngredientLines(structuredRecipe.get("recipeIngredient"));
        String instructions = extractInstructions(structuredRecipe.get("recipeInstructions"));
        String sourceName = firstNonBlank(
                extractSourceName(structuredRecipe),
                metadata.siteName(),
                metadata.hostLabel()
        );
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
                if (ingredientNode.isTextual()) {
                    appendNormalizedIngredientText(ingredientNode.asText(), ingredients);
                    continue;
                }
                if (ingredientNode.isObject()) {
                    String ingredient = firstNonBlank(
                            textValue(ingredientNode.get("text")),
                            textValue(ingredientNode.get("name"))
                    );
                    appendNormalizedIngredientText(ingredient, ingredients);
                }
            }
        }
        return ingredients;
    }

    private String extractInstructions(JsonNode instructionsNode) {
        if (instructionsNode == null || instructionsNode.isNull()) {
            return null;
        }
        if (instructionsNode.isTextual()) {
            return normalizeInstructions(instructionsNode.asText());
        }
        List<String> steps = new ArrayList<>();
        collectInstructionSteps(instructionsNode, steps);
        if (steps.isEmpty()) {
            return null;
        }
        return String.join("\n", steps);
    }

    private void collectInstructionSteps(JsonNode node, List<String> steps) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual()) {
            String normalized = normalizeInlineText(node.asText());
            if (normalized != null) {
                steps.add(normalized);
            }
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
                    textValue(node.get("name"))
            );
            String normalized = normalizeInlineText(text);
            if (normalized != null) {
                steps.add(normalized);
                return;
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
