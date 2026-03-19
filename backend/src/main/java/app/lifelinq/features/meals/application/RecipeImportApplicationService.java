package app.lifelinq.features.meals.application;

import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.meals.contract.IngredientUnitView;
import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeImportDraftIngredientView;
import app.lifelinq.features.meals.contract.RecipeImportDraftView;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeImportApplicationService {
    private static final Pattern LEADING_QUANTITY_PATTERN = Pattern.compile(
            "^(?<quantity>(?:\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[\\.,]\\d+)?|[¼½¾⅓⅔⅛⅜⅝⅞]|\\d+[¼½¾⅓⅔⅛⅜⅝⅞]))\\s*(?<rest>.+)$"
    );
    private static final Pattern ATTACHED_UNIT_PATTERN = Pattern.compile(
            "^(?<quantity>\\d+(?:[\\.,]\\d+)?)(?<unit>[\\p{L}]+)\\s+(?<rest>.+)$"
    );
    private static final Set<String> UNSUPPORTED_MEASURE_TOKENS = Set.of(
            "clove", "cloves",
            "slice", "slices",
            "skiva", "skivor",
            "klyfta", "klyftor",
            "pinch", "pinches",
            "nypa", "nypor",
            "dash", "dashes",
            "skvatt", "skvattar",
            "skvätt", "skvättar",
            "handful", "handfuls",
            "bunch", "bunches",
            "sprig", "sprigs",
            "tin", "tins",
            "can", "cans",
            "jar", "jars",
            "burk", "burkar"
    );
    private static final Set<String> BARE_COUNT_BLOCKLIST_TOKENS = Set.of(
            "x", "about", "approx", "approximately"
    );
    private static final Set<String> IGNORABLE_INGREDIENT_LINES = Set.of(
            "ingredients",
            "ingredienser",
            "serving",
            "servering",
            "to serve",
            "for serving",
            "till servering",
            "för servering",
            "att servera",
            "garnish",
            "garnering",
            "topping",
            "toppings",
            "sauce",
            "sås",
            "dressing",
            "marinade",
            "marinad"
    );

    private final EnsureGroupMemberUseCase ensureGroupMemberUseCase;
    private final RecipeImportPort recipeImportPort;

    public RecipeImportApplicationService(
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            RecipeImportPort recipeImportPort
    ) {
        if (ensureGroupMemberUseCase == null) {
            throw new IllegalArgumentException("ensureGroupMemberUseCase must not be null");
        }
        if (recipeImportPort == null) {
            throw new IllegalArgumentException("recipeImportPort must not be null");
        }
        this.ensureGroupMemberUseCase = ensureGroupMemberUseCase;
        this.recipeImportPort = recipeImportPort;
    }

    public RecipeImportDraftView importRecipeDraft(
            UUID groupId,
            UUID actorUserId,
            String sourceUrl
    ) {
        ensureMealAccess(groupId, actorUserId);
        String normalizedUrl = normalizeImportUrl(sourceUrl);
        ParsedRecipeImportData parsed = recipeImportPort.importFromUrl(normalizedUrl);
        List<RecipeImportDraftIngredientView> ingredients = normalizeIngredients(parsed.ingredientLines());
        String name = normalizeRequiredName(parsed.name());
        String normalizedSourceName = normalizeOptionalText(parsed.sourceName());
        String normalizedShortNote = normalizeOptionalText(parsed.shortNote());
        String normalizedInstructions = normalizeOptionalInstructions(parsed.instructions());

        return new RecipeImportDraftView(
                name,
                normalizedSourceName != null ? normalizedSourceName : deriveSourceName(normalizedUrl),
                normalizeImportUrl(parsed.sourceUrl() == null ? normalizedUrl : parsed.sourceUrl()),
                RecipeOriginKind.URL_IMPORT.name(),
                normalizedShortNote,
                normalizedInstructions,
                ingredients
        );
    }

    private List<RecipeImportDraftIngredientView> normalizeIngredients(List<String> ingredientLines) {
        if (ingredientLines == null) {
            throw new RecipeImportFailedException("Imported recipe is missing ingredients");
        }

        List<RecipeImportDraftIngredientView> ingredients = new ArrayList<>();
        int nextPosition = 1;
        for (String line : ingredientLines) {
            String normalizedLine = normalizeOptionalText(line);
            if (normalizedLine == null) {
                continue;
            }
            if (isIgnorableIngredientLine(normalizedLine)) {
                continue;
            }
            ingredients.add(toIngredientView(normalizedLine, nextPosition));
            nextPosition += 1;
        }

        if (ingredients.isEmpty()) {
            throw new RecipeImportFailedException("Imported recipe is missing ingredients");
        }
        return ingredients;
    }

    private RecipeImportDraftIngredientView toIngredientView(String line, int position) {
        String normalizedLine = normalizeIngredientLine(line);
        Matcher matcher = LEADING_QUANTITY_PATTERN.matcher(normalizedLine);
        if (!matcher.matches()) {
            Matcher attachedUnitMatcher = ATTACHED_UNIT_PATTERN.matcher(normalizedLine);
            if (!attachedUnitMatcher.matches()) {
                String fallbackIngredientName = stripUnsupportedMeasureToken(normalizedLine);
                if (fallbackIngredientName != null) {
                    return new RecipeImportDraftIngredientView(
                            fallbackIngredientName,
                            normalizedLine,
                            null,
                            null,
                            position
                    );
                }
                return new RecipeImportDraftIngredientView(normalizedLine, normalizedLine, null, null, position);
            }
            BigDecimal quantity = parseQuantity(attachedUnitMatcher.group("quantity"));
            UnitParseResult unitResult = parseUnit(
                    attachedUnitMatcher.group("unit"),
                    attachedUnitMatcher.group("rest")
            );
            if (quantity == null) {
                return new RecipeImportDraftIngredientView(normalizedLine, normalizedLine, null, null, position);
            }
        if (unitResult != null) {
            return new RecipeImportDraftIngredientView(
                    unitResult.ingredientName(),
                    normalizedLine,
                    quantity,
                    unitResult.unit(),
                    position
            );
        }
        return new RecipeImportDraftIngredientView(
                normalizedLine,
                normalizedLine,
                null,
                null,
                    position
            );
        }

        BigDecimal quantity = parseQuantity(matcher.group("quantity"));
        String rest = matcher.group("rest").trim();
        UnitParseResult unitResult = parseUnit(rest);

        if (quantity == null) {
            return new RecipeImportDraftIngredientView(normalizedLine, normalizedLine, null, null, position);
        }
        if (unitResult != null) {
            return new RecipeImportDraftIngredientView(
                    unitResult.ingredientName(),
                    normalizedLine,
                    quantity,
                    unitResult.unit(),
                    position
            );
        }

        UnitParseResult bareCountResult = parseBareCountIngredient(rest);
        if (bareCountResult != null) {
            return new RecipeImportDraftIngredientView(
                    bareCountResult.ingredientName(),
                    normalizedLine,
                    quantity,
                    bareCountResult.unit(),
                    position
            );
        }

        return new RecipeImportDraftIngredientView(normalizedLine, normalizedLine, null, null, position);
    }

    private BigDecimal parseQuantity(String quantityText) {
        try {
            String normalized = normalizeFractionQuantity(quantityText);
            BigDecimal unicodeFraction = parseUnicodeFractionQuantity(normalized);
            if (unicodeFraction != null) {
                return unicodeFraction;
            }
            if (normalized.contains("/")) {
                String[] parts = normalized.split("\\s+");
                if (parts.length == 2) {
                    return new BigDecimal(parts[0]).add(parseFraction(parts[1]));
                }
                return parseFraction(normalized);
            }
            return new BigDecimal(normalized.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private UnitParseResult parseUnit(String value) {
        int split = value.indexOf(' ');
        if (split <= 0 || split >= value.length() - 1) {
            return null;
        }

        return parseUnit(value.substring(0, split), value.substring(split + 1));
    }

    private UnitParseResult parseUnit(String unitToken, String ingredientValue) {
        IngredientUnitView unit = parseSupportedUnit(unitToken);

        if (unit == null) {
            return null;
        }

        String ingredientName = normalizeOptionalText(ingredientValue);
        if (ingredientName == null) {
            return null;
        }

        return new UnitParseResult(unit, ingredientName);
    }

    private UnitParseResult parseBareCountIngredient(String ingredientValue) {
        String ingredientName = normalizeOptionalText(ingredientValue);
        if (ingredientName == null) {
            return null;
        }

        String firstToken = firstToken(ingredientName);
        if (firstToken == null) {
            return null;
        }

        String normalizedFirstToken = normalizeUnitToken(firstToken);
        if (normalizedFirstToken.isEmpty()
                || BARE_COUNT_BLOCKLIST_TOKENS.contains(normalizedFirstToken)
                || hasSecondaryMeasureToken(ingredientName)
                || UNSUPPORTED_MEASURE_TOKENS.contains(normalizedFirstToken)
                || parseSupportedUnit(normalizedFirstToken) != null) {
            return null;
        }

        return new UnitParseResult(IngredientUnitView.PCS, ingredientName);
    }

    private String stripUnsupportedMeasureToken(String value) {
        String[] parts = value.split("\\s+", 3);
        if (parts.length < 2) {
            return null;
        }
        if (parts.length >= 2) {
            String shaped = stripUnsupportedMeasureToken(parts[0], value.substring(parts[0].length()).trim());
            if (shaped != null) {
                return shaped;
            }
        }
        if (parts.length == 3 && isLooseFractionToken(parts[0])) {
            return stripUnsupportedMeasureToken(parts[1], parts[2]);
        }
        return null;
    }

    private String stripUnsupportedMeasureToken(String unitToken, String ingredientValue) {
        String normalizedUnitToken = normalizeUnitToken(unitToken);
        if (!UNSUPPORTED_MEASURE_TOKENS.contains(normalizedUnitToken)
                && parseSupportedKitchenMeasure(normalizedUnitToken) == null) {
            return null;
        }

        String normalizedIngredientValue = normalizeOptionalText(ingredientValue);
        if (normalizedIngredientValue == null) {
            return null;
        }

        String withoutOf = normalizedIngredientValue.replaceFirst("^(?i)of\\s+", "");
        String shaped = normalizeOptionalText(withoutOf);
        return shaped;
    }

    private IngredientUnitView parseSupportedUnit(String unitToken) {
        String normalizedUnitToken = normalizeUnitToken(unitToken);
        return switch (normalizedUnitToken) {
            case "pcs", "pc", "piece", "pieces", "st" -> IngredientUnitView.PCS;
            case "pack", "packs", "pkt", "package", "packages" -> IngredientUnitView.PACK;
            case "kg", "kilo", "kilos", "kilogram", "kilograms" -> IngredientUnitView.KG;
            case "hg", "hectogram", "hectograms" -> IngredientUnitView.HG;
            case "g", "gram", "grams" -> IngredientUnitView.G;
            case "l", "liter", "liters", "litre", "litres" -> IngredientUnitView.L;
            case "dl", "deciliter", "deciliters", "decilitre", "decilitres" -> IngredientUnitView.DL;
            case "ml", "milliliter", "milliliters", "millilitre", "millilitres" -> IngredientUnitView.ML;
            default -> parseSupportedKitchenMeasure(normalizedUnitToken);
        };
    }

    private IngredientUnitView parseSupportedKitchenMeasure(String normalizedUnitToken) {
        return switch (normalizedUnitToken) {
            case "tbsp", "tbsps", "tblsp", "tblsps", "tbl", "tbls", "tbs", "tablespoon", "tablespoons",
                    "msk", "matsk", "matsked", "matskedar" -> IngredientUnitView.TBSP;
            case "tsp", "tsps", "teasp", "teasps", "teaspoon", "teaspoons",
                    "tsk", "tesk", "tesked", "teskedar" -> IngredientUnitView.TSP;
            case "krm", "kryddmått" -> IngredientUnitView.KRM;
            default -> null;
        };
    }

    private String normalizeUnitToken(String unitToken) {
        return unitToken
                .toLowerCase(Locale.ROOT)
                .replace(".", "")
                .replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "")
                .trim();
    }

    private boolean isIgnorableIngredientLine(String value) {
        String normalizedLine = normalizeOptionalText(value);
        if (normalizedLine == null) {
            return true;
        }

        String lowercaseLine = normalizedLine.toLowerCase(Locale.ROOT);
        if (IGNORABLE_INGREDIENT_LINES.contains(lowercaseLine)) {
            return true;
        }

        if (LEADING_QUANTITY_PATTERN.matcher(normalizedLine).matches()
                || ATTACHED_UNIT_PATTERN.matcher(normalizedLine).matches()) {
            return false;
        }

        return normalizedLine.endsWith(":");
    }

    private boolean hasSecondaryMeasureToken(String ingredientName) {
        String[] tokens = ingredientName.split("\\s+");
        if (tokens.length < 2) {
            return false;
        }

        String normalizedSecondToken = normalizeUnitToken(tokens[1]);
        if (normalizedSecondToken.isEmpty()) {
            return false;
        }

        return UNSUPPORTED_MEASURE_TOKENS.contains(normalizedSecondToken)
                || parseSupportedUnit(normalizedSecondToken) != null;
    }

    private boolean isLooseFractionToken(String token) {
        return switch (token) {
            case "¼", "½", "¾", "⅓", "⅔", "⅛", "⅜", "⅝", "⅞" -> true;
            default -> false;
        };
    }

    private BigDecimal parseUnicodeFractionQuantity(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (isLooseFractionToken(value)) {
            return unicodeFractionValue(value);
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && isLooseFractionToken(trimmed.substring(trimmed.length() - 1))) {
            String wholePart = trimmed.substring(0, trimmed.length() - 1);
            if (wholePart.chars().allMatch(Character::isDigit)) {
                return new BigDecimal(wholePart).add(
                        unicodeFractionValue(trimmed.substring(trimmed.length() - 1))
                );
            }
        }
        return null;
    }

    private BigDecimal unicodeFractionValue(String token) {
        return switch (token) {
            case "¼" -> new BigDecimal("0.25");
            case "½" -> new BigDecimal("0.5");
            case "¾" -> new BigDecimal("0.75");
            case "⅓" -> new BigDecimal("0.3333333333");
            case "⅔" -> new BigDecimal("0.6666666667");
            case "⅛" -> new BigDecimal("0.125");
            case "⅜" -> new BigDecimal("0.375");
            case "⅝" -> new BigDecimal("0.625");
            case "⅞" -> new BigDecimal("0.875");
            default -> null;
        };
    }

    private String normalizeRequiredName(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new RecipeImportFailedException("Imported recipe is missing a name");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeIngredientLine(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized
                .replace('\u00A0', ' ')
                .replaceFirst("^[\\-•*]+\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeFractionQuantity(String value) {
        return value
                .replace("½", " 1/2")
                .replace("¼", " 1/4")
                .replace("¾", " 3/4")
                .replace("⅓", " 1/3")
                .replace("⅔", " 2/3")
                .replace("⅛", " 1/8")
                .replace("⅜", " 3/8")
                .replace("⅝", " 5/8")
                .replace("⅞", " 7/8")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private BigDecimal parseFraction(String value) {
        String[] parts = value.split("/");
        if (parts.length != 2) {
            return null;
        }
        BigDecimal numerator = new BigDecimal(parts[0]);
        BigDecimal denominator = new BigDecimal(parts[1]);
        if (BigDecimal.ZERO.compareTo(denominator) == 0) {
            return null;
        }
        return numerator.divide(denominator, 4, java.math.RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private String normalizeOptionalInstructions(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String firstToken(String value) {
        int split = value.indexOf(' ');
        if (split < 0) {
            return value;
        }
        return value.substring(0, split);
    }

    private String normalizeImportUrl(String value) {
        if (value == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("url must not be blank");
        }
        URI uri = URI.create(normalized);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("url must use http or https");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("url must include a host");
        }
        return uri.toString();
    }

    private String deriveSourceName(String sourceUrl) {
        URI uri = URI.create(sourceUrl);
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return null;
        }
        String normalizedHost = host.startsWith("www.") ? host.substring(4) : host;
        return normalizedHost;
    }

    private void ensureMealAccess(UUID groupId, UUID actorUserId) {
        try {
            ensureGroupMemberUseCase.execute(groupId, actorUserId);
        } catch (AccessDeniedException ex) {
            throw new MealsAccessDeniedException(ex.getMessage());
        }
    }

    private record UnitParseResult(IngredientUnitView unit, String ingredientName) {
    }
}
