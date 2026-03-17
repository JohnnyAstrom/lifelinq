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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeImportApplicationService {
    private static final Pattern LEADING_QUANTITY_PATTERN = Pattern.compile(
            "^(?<quantity>\\d+(?:[\\.,]\\d+)?)\\s*(?<rest>.+)$"
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
            ingredients.add(toIngredientView(normalizedLine, nextPosition));
            nextPosition += 1;
        }

        if (ingredients.isEmpty()) {
            throw new RecipeImportFailedException("Imported recipe is missing ingredients");
        }
        return ingredients;
    }

    private RecipeImportDraftIngredientView toIngredientView(String line, int position) {
        Matcher matcher = LEADING_QUANTITY_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return new RecipeImportDraftIngredientView(line, null, null, position);
        }

        BigDecimal quantity = parseQuantity(matcher.group("quantity"));
        String rest = matcher.group("rest").trim();
        UnitParseResult unitResult = parseUnit(rest);

        if (quantity == null || unitResult == null) {
            return new RecipeImportDraftIngredientView(line, null, null, position);
        }

        return new RecipeImportDraftIngredientView(
                unitResult.ingredientName(),
                quantity,
                unitResult.unit(),
                position
        );
    }

    private BigDecimal parseQuantity(String quantityText) {
        try {
            return new BigDecimal(quantityText.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private UnitParseResult parseUnit(String value) {
        int split = value.indexOf(' ');
        if (split <= 0 || split >= value.length() - 1) {
            return null;
        }

        String unitToken = value.substring(0, split).toLowerCase(Locale.ROOT);
        IngredientUnitView unit = switch (unitToken) {
            case "pcs", "pc", "st" -> IngredientUnitView.PCS;
            case "pack", "pkt" -> IngredientUnitView.PACK;
            case "kg" -> IngredientUnitView.KG;
            case "hg" -> IngredientUnitView.HG;
            case "g", "gram" -> IngredientUnitView.G;
            case "l" -> IngredientUnitView.L;
            case "dl" -> IngredientUnitView.DL;
            case "ml" -> IngredientUnitView.ML;
            default -> null;
        };

        if (unit == null) {
            return null;
        }

        String ingredientName = normalizeOptionalText(value.substring(split + 1));
        if (ingredientName == null) {
            return null;
        }

        return new UnitParseResult(unit, ingredientName);
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
