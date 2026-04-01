package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.IngredientUnitView;
import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeKind;
import app.lifelinq.features.meals.contract.RecipeAssetIntakePort;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import app.lifelinq.features.meals.contract.RecipeImportDraftIngredientView;
import app.lifelinq.features.meals.contract.RecipeImportDraftView;
import app.lifelinq.features.meals.contract.RecipeImportPort;
import app.lifelinq.features.meals.domain.Ingredient;
import app.lifelinq.features.meals.domain.IngredientUnit;
import app.lifelinq.features.meals.domain.RecipeDraftState;
import app.lifelinq.features.meals.domain.RecipeInstructions;
import app.lifelinq.features.meals.domain.RecipeOriginKind;
import app.lifelinq.features.meals.domain.RecipeProvenance;
import app.lifelinq.features.meals.domain.RecipeSource;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RecipeImportDraftSupport {
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
    private static final Set<String> INGREDIENT_SECTION_HEADINGS = Set.of(
            "ingredients",
            "ingredienser"
    );
    private static final Set<String> INSTRUCTION_SECTION_HEADINGS = Set.of(
            "instructions",
            "instruction",
            "directions",
            "method",
            "preparation",
            "prep",
            "gör så här",
            "gor sa har",
            "gör såhär",
            "gor sahar",
            "tillagning"
    );
    private static final Pattern SERVINGS_LINE_PATTERN = Pattern.compile(
            "^(?:(?:servings?|yield|portioner?|serverar))\\s*[:\\-]?\\s*(?<value>.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private RecipeImportDraftSupport() {
    }

    static RecipeDraftSeed importFromUrl(RecipeImportPort recipeImportPort, String sourceUrl) {
        if (recipeImportPort == null) {
            throw new IllegalArgumentException("recipeImportPort must not be null");
        }
        String normalizedUrl = normalizeImportUrl(sourceUrl);
        ParsedRecipeImportData parsed = recipeImportPort.importFromUrl(normalizedUrl);
        List<Ingredient> ingredients = normalizeIngredients(parsed.ingredientLines());
        String name = normalizeRequiredName(parsed.name());
        String normalizedSourceName = normalizeOptionalText(parsed.sourceName());
        String normalizedServings = normalizeOptionalText(parsed.servings());
        String normalizedShortNote = normalizeOptionalText(parsed.shortNote());
        RecipeInstructions instructions = new RecipeInstructions(parsed.instructions());
        RecipeSource source = new RecipeSource(
                normalizedSourceName != null ? normalizedSourceName : deriveSourceName(normalizedUrl),
                normalizeImportUrl(parsed.sourceUrl() == null ? normalizedUrl : parsed.sourceUrl())
        );
        RecipeProvenance provenance = new RecipeProvenance(RecipeOriginKind.URL_IMPORT, source.sourceUrl());
        return new RecipeDraftSeed(
                name,
                source,
                provenance,
                normalizedServings,
                normalizedShortNote,
                instructions,
                ingredients,
                RecipeDraftState.DRAFT_NEEDS_REVIEW
        );
    }

    static RecipeDraftSeed importFromText(String text) {
        List<List<String>> blocks = normalizePastedTextBlocks(text);
        if (blocks.isEmpty() || blocks.get(0).isEmpty()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        List<String> titleBlock = blocks.get(0);
        String name = normalizeRequiredName(titleBlock.get(0));
        List<List<String>> remainingBlocks = new ArrayList<>();
        if (titleBlock.size() > 1) {
            remainingBlocks.add(List.copyOf(titleBlock.subList(1, titleBlock.size())));
        }
        if (blocks.size() > 1) {
            remainingBlocks.addAll(blocks.subList(1, blocks.size()));
        }

        PastedTextSections sections = extractPastedTextSections(remainingBlocks);
        List<Ingredient> ingredients = normalizeIngredientsAllowEmpty(sections.ingredientLines());
        if (!isReviewablePastedText(name, sections, ingredients)) {
            throw new RecipeImportFailedException("Paste a little more of the recipe so you have something useful to review");
        }
        RecipeProvenance provenance = new RecipeProvenance(RecipeOriginKind.PASTED_TEXT, null);
        return new RecipeDraftSeed(
                name,
                new RecipeSource(null, null),
                provenance,
                sections.servings(),
                null,
                new RecipeInstructions(sections.instructions()),
                ingredients,
                RecipeDraftState.DRAFT_NEEDS_REVIEW
        );
    }

    static RecipeDraftSeed importFromAsset(RecipeAssetIntakePort recipeAssetIntakePort, RecipeAssetIntakeReference reference) {
        if (recipeAssetIntakePort == null) {
            throw new IllegalArgumentException("recipeAssetIntakePort must not be null");
        }
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }

        ParsedRecipeImportData parsed = recipeAssetIntakePort.extract(reference);
        String parsedName = normalizeOptionalText(parsed.name());
        String name = parsedName != null ? parsedName : deriveAssetRecipeName(reference);
        if (name == null) {
            throw new RecipeImportFailedException("Imported asset is missing recipe content");
        }

        List<Ingredient> ingredients = reference.kind() == RecipeAssetIntakeKind.IMAGE
                ? normalizeIngredientsAllowEmpty(parsed.ingredientLines(), true)
                : normalizeIngredientsAllowEmpty(parsed.ingredientLines());
        String normalizedServings = normalizeOptionalText(parsed.servings());
        String normalizedShortNote = normalizeOptionalText(parsed.shortNote());
        RecipeInstructions instructions = new RecipeInstructions(parsed.instructions());
        RecipeSource source = new RecipeSource(
                firstNonBlank(parsed.sourceName(), reference.effectiveSourceLabel()),
                normalizeOptionalText(parsed.sourceUrl())
        );
        if (!isReviewableAssetImport(name, ingredients, normalizedShortNote, instructions.body())) {
            throw new RecipeImportFailedException("Imported asset is missing recipe content");
        }
        RecipeProvenance provenance = new RecipeProvenance(toAssetOriginKind(reference.kind()), null);
        return new RecipeDraftSeed(
                name,
                source,
                provenance,
                normalizedServings,
                normalizedShortNote,
                instructions,
                ingredients,
                RecipeDraftState.DRAFT_NEEDS_REVIEW
        );
    }

    static RecipeImportDraftView toLegacyView(RecipeDraftSeed seed) {
        List<RecipeImportDraftIngredientView> ingredients = new ArrayList<>();
        for (Ingredient ingredient : seed.ingredients()) {
            ingredients.add(new RecipeImportDraftIngredientView(
                    ingredient.getName(),
                    ingredient.getRawText(),
                    ingredient.getQuantity(),
                    toViewUnit(ingredient.getUnit()),
                    ingredient.getPosition()
            ));
        }
        return new RecipeImportDraftView(
                seed.name(),
                seed.source().sourceName(),
                seed.source().sourceUrl(),
                seed.provenance().originKind().name(),
                seed.servings(),
                seed.shortNote(),
                seed.instructions().body(),
                ingredients
        );
    }

    private static IngredientUnitView toViewUnit(IngredientUnit unit) {
        return unit == null ? null : IngredientUnitView.valueOf(unit.name());
    }

    private static List<Ingredient> normalizeIngredients(List<String> ingredientLines) {
        if (ingredientLines == null) {
            throw new RecipeImportFailedException("Imported recipe is missing ingredients");
        }

        List<Ingredient> ingredients = new ArrayList<>();
        int nextPosition = 1;
        for (String line : ingredientLines) {
            String normalizedLine = normalizeOptionalText(line);
            if (normalizedLine == null || isIgnorableIngredientLine(normalizedLine)) {
                continue;
            }
            ingredients.add(toIngredient(normalizedLine, nextPosition));
            nextPosition += 1;
        }

        if (ingredients.isEmpty()) {
            throw new RecipeImportFailedException("Imported recipe is missing ingredients");
        }
        return ingredients;
    }

    private static List<Ingredient> normalizeIngredientsAllowEmpty(List<String> ingredientLines) {
        return normalizeIngredientsAllowEmpty(ingredientLines, false);
    }

    private static List<Ingredient> normalizeIngredientsAllowEmpty(
            List<String> ingredientLines,
            boolean tolerateInvalidStructuredQuantity
    ) {
        if (ingredientLines == null || ingredientLines.isEmpty()) {
            return List.of();
        }

        List<Ingredient> ingredients = new ArrayList<>();
        int nextPosition = 1;
        for (String line : ingredientLines) {
            String normalizedLine = normalizeOptionalText(line);
            if (normalizedLine == null || isIgnorableIngredientLine(normalizedLine)) {
                continue;
            }
            ingredients.add(toIngredient(normalizedLine, nextPosition, tolerateInvalidStructuredQuantity));
            nextPosition += 1;
        }
        return List.copyOf(ingredients);
    }

    private static Ingredient toIngredient(String line, int position) {
        return toIngredient(line, position, false);
    }

    private static Ingredient toIngredient(String line, int position, boolean tolerateInvalidStructuredQuantity) {
        String normalizedLine = normalizeIngredientLine(line);
        Matcher matcher = LEADING_QUANTITY_PATTERN.matcher(normalizedLine);
        if (!matcher.matches()) {
            Matcher attachedUnitMatcher = ATTACHED_UNIT_PATTERN.matcher(normalizedLine);
            if (!attachedUnitMatcher.matches()) {
                String fallbackIngredientName = stripUnsupportedMeasureToken(normalizedLine);
                if (fallbackIngredientName != null) {
                    return new Ingredient(UUID.randomUUID(), fallbackIngredientName, normalizedLine, null, null, position);
                }
                return new Ingredient(UUID.randomUUID(), normalizedLine, normalizedLine, null, null, position);
            }
            BigDecimal quantity = parseQuantity(attachedUnitMatcher.group("quantity"));
            UnitParseResult unitResult = parseUnit(
                    attachedUnitMatcher.group("unit"),
                    attachedUnitMatcher.group("rest")
            );
            if (quantity == null) {
                return new Ingredient(UUID.randomUUID(), normalizedLine, normalizedLine, null, null, position);
            }
            if (unitResult != null) {
                if (tolerateInvalidStructuredQuantity && quantity.signum() <= 0) {
                    return createRawIngredientFallback(unitResult.ingredientName(), normalizedLine, position);
                }
                return new Ingredient(
                        UUID.randomUUID(),
                        unitResult.ingredientName(),
                        normalizedLine,
                        quantity,
                        unitResult.unit(),
                        position
                );
            }
            return new Ingredient(UUID.randomUUID(), normalizedLine, normalizedLine, null, null, position);
        }

        BigDecimal quantity = parseQuantity(matcher.group("quantity"));
        String rest = matcher.group("rest").trim();
        UnitParseResult unitResult = parseUnit(rest);

        if (quantity == null) {
            return new Ingredient(UUID.randomUUID(), normalizedLine, normalizedLine, null, null, position);
        }
        if (unitResult != null) {
            if (tolerateInvalidStructuredQuantity && quantity.signum() <= 0) {
                return createRawIngredientFallback(unitResult.ingredientName(), normalizedLine, position);
            }
            return new Ingredient(
                    UUID.randomUUID(),
                    unitResult.ingredientName(),
                    normalizedLine,
                    quantity,
                    unitResult.unit(),
                    position
            );
        }

        UnitParseResult bareCountResult = parseBareCountIngredient(rest);
        if (bareCountResult != null) {
            if (tolerateInvalidStructuredQuantity && quantity.signum() <= 0) {
                return createRawIngredientFallback(bareCountResult.ingredientName(), normalizedLine, position);
            }
            return new Ingredient(
                    UUID.randomUUID(),
                    bareCountResult.ingredientName(),
                    normalizedLine,
                    quantity,
                    bareCountResult.unit(),
                    position
            );
        }

        return new Ingredient(UUID.randomUUID(), normalizedLine, normalizedLine, null, null, position);
    }

    private static Ingredient createRawIngredientFallback(String candidateName, String rawText, int position) {
        String normalizedName = normalizeOptionalText(candidateName);
        String normalizedRawText = normalizeOptionalText(rawText);
        String safeName = normalizedName != null ? normalizedName : normalizedRawText;
        return new Ingredient(UUID.randomUUID(), safeName, normalizedRawText, null, null, position);
    }

    private static boolean isReviewableAssetImport(
            String name,
            List<Ingredient> ingredients,
            String shortNote,
            String instructions
    ) {
        if (normalizeOptionalText(name) == null) {
            return false;
        }
        if (ingredients != null && !ingredients.isEmpty()) {
            return true;
        }
        if (normalizeOptionalText(shortNote) != null) {
            return true;
        }
        return normalizeOptionalText(instructions) != null;
    }

    private static PastedTextSections extractPastedTextSections(List<List<String>> blocks) {
        List<String> ingredientLines = new ArrayList<>();
        List<String> instructionLines = new ArrayList<>();
        List<List<String>> unsectionedBlocks = new ArrayList<>();
        String servings = null;
        Section currentSection = Section.NONE;

        for (List<String> block : blocks) {
            List<String> currentUnsectionedBlock = new ArrayList<>();
            for (String line : block) {
                String normalizedLine = normalizeOptionalText(line);
                if (normalizedLine == null) {
                    continue;
                }

                String extractedServings = extractServings(normalizedLine);
                if (servings == null && extractedServings != null) {
                    servings = extractedServings;
                    continue;
                }
                if (isIngredientSectionHeading(normalizedLine)) {
                    currentSection = Section.INGREDIENTS;
                    continue;
                }
                if (isInstructionSectionHeading(normalizedLine)) {
                    currentSection = Section.INSTRUCTIONS;
                    continue;
                }

                switch (currentSection) {
                    case INGREDIENTS -> ingredientLines.add(normalizedLine);
                    case INSTRUCTIONS -> instructionLines.add(normalizedLine);
                    case NONE -> currentUnsectionedBlock.add(normalizedLine);
                }
            }
            if (!currentUnsectionedBlock.isEmpty()) {
                unsectionedBlocks.add(List.copyOf(currentUnsectionedBlock));
            }
        }

        if (ingredientLines.isEmpty() && !unsectionedBlocks.isEmpty()) {
            if (unsectionedBlocks.size() >= 2) {
                ingredientLines.addAll(extractLikelyIngredientLines(unsectionedBlocks.get(0)));
                for (int index = 1; index < unsectionedBlocks.size(); index += 1) {
                    instructionLines.addAll(unsectionedBlocks.get(index));
                }
            } else {
                List<String> singleBlock = unsectionedBlocks.get(0);
                int splitIndex = 0;
                while (splitIndex < singleBlock.size() && looksLikeIngredientLine(singleBlock.get(splitIndex))) {
                    ingredientLines.add(singleBlock.get(splitIndex));
                    splitIndex += 1;
                }
                for (int index = splitIndex; index < singleBlock.size(); index += 1) {
                    instructionLines.add(singleBlock.get(index));
                }
            }
        }

        if (ingredientLines.isEmpty() && instructionLines.isEmpty() && !unsectionedBlocks.isEmpty()) {
            ingredientLines.addAll(unsectionedBlocks.get(0));
        }

        return new PastedTextSections(
                servings,
                List.copyOf(ingredientLines),
                joinInstructionLines(instructionLines)
        );
    }

    private static List<List<String>> normalizePastedTextBlocks(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        String normalizedText = text
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        List<List<String>> blocks = new ArrayList<>();
        List<String> currentBlock = new ArrayList<>();
        for (String rawLine : normalizedText.split("\n", -1)) {
            String normalizedLine = normalizeOptionalText(rawLine);
            if (normalizedLine == null) {
                if (!currentBlock.isEmpty()) {
                    blocks.add(List.copyOf(currentBlock));
                    currentBlock.clear();
                }
                continue;
            }
            currentBlock.add(normalizedLine);
        }
        if (!currentBlock.isEmpty()) {
            blocks.add(List.copyOf(currentBlock));
        }
        return List.copyOf(blocks);
    }

    private static List<String> extractLikelyIngredientLines(List<String> lines) {
        List<String> likelyLines = new ArrayList<>();
        for (String line : lines) {
            if (looksLikeIngredientLine(line)) {
                likelyLines.add(line);
            }
        }
        return likelyLines.isEmpty() ? List.copyOf(lines) : List.copyOf(likelyLines);
    }

    private static boolean looksLikeIngredientLine(String value) {
        String normalizedLine = normalizeIngredientLine(value);
        if (normalizedLine == null) {
            return false;
        }
        if (LEADING_QUANTITY_PATTERN.matcher(normalizedLine).matches()
                || ATTACHED_UNIT_PATTERN.matcher(normalizedLine).matches()) {
            return true;
        }
        if (isInstructionSectionHeading(normalizedLine) || isIngredientSectionHeading(normalizedLine)) {
            return false;
        }
        if (normalizedLine.endsWith(".") || normalizedLine.endsWith("!") || normalizedLine.endsWith("?")) {
            return false;
        }
        return normalizedLine.split("\\s+").length <= 6;
    }

    private static boolean isIngredientSectionHeading(String value) {
        return INGREDIENT_SECTION_HEADINGS.contains(normalizeSectionHeading(value));
    }

    private static boolean isInstructionSectionHeading(String value) {
        return INSTRUCTION_SECTION_HEADINGS.contains(normalizeSectionHeading(value));
    }

    private static String normalizeSectionHeading(String value) {
        return normalizeOptionalText(value == null ? null : value.replace(":", "")) == null
                ? ""
                : normalizeOptionalText(value.replace(":", "")).toLowerCase(Locale.ROOT);
    }

    private static String extractServings(String value) {
        Matcher matcher = SERVINGS_LINE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return null;
        }
        return normalizeOptionalText(matcher.group("value"));
    }

    private static String joinInstructionLines(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }
        return String.join("\n", lines);
    }

    private static boolean isReviewablePastedText(
            String name,
            PastedTextSections sections,
            List<Ingredient> ingredients
    ) {
        if (ingredients != null && !ingredients.isEmpty()) {
            return true;
        }

        String normalizedName = normalizeOptionalText(name);
        String normalizedInstructions = normalizeOptionalText(sections.instructions());
        int ingredientLineCount = sections.ingredientLines() == null ? 0 : sections.ingredientLines().size();
        int instructionLineCount = countMeaningfulLines(sections.instructions());
        int meaningfulBodyCharacters = countMeaningfulCharacters(sections.ingredientLines(), sections.instructions());

        if (normalizedName == null || normalizedName.length() < 3) {
            return false;
        }

        if (instructionLineCount >= 2) {
            return true;
        }
        if (ingredientLineCount >= 2) {
            return true;
        }
        if (normalizedInstructions != null && meaningfulBodyCharacters >= 30) {
            return true;
        }
        return meaningfulBodyCharacters >= 60;
    }

    private static int countMeaningfulLines(String value) {
        if (value == null) {
            return 0;
        }
        int count = 0;
        for (String line : value.split("\\R")) {
            String normalized = normalizeOptionalText(line);
            if (normalized != null) {
                count += 1;
            }
        }
        return count;
    }

    private static int countMeaningfulCharacters(List<String> ingredientLines, String instructions) {
        int total = 0;
        if (ingredientLines != null) {
            for (String line : ingredientLines) {
                String normalized = normalizeOptionalText(line);
                if (normalized != null) {
                    total += normalized.length();
                }
            }
        }
        if (instructions != null) {
            String normalizedInstructions = normalizeOptionalText(instructions.replace('\n', ' '));
            if (normalizedInstructions != null) {
                total += normalizedInstructions.length();
            }
        }
        return total;
    }

    private static BigDecimal parseQuantity(String quantityText) {
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

    private static UnitParseResult parseUnit(String value) {
        int split = value.indexOf(' ');
        if (split <= 0 || split >= value.length() - 1) {
            return null;
        }
        return parseUnit(value.substring(0, split), value.substring(split + 1));
    }

    private static UnitParseResult parseUnit(String unitToken, String ingredientValue) {
        IngredientUnit unit = parseSupportedUnit(unitToken);
        if (unit == null) {
            return null;
        }
        String ingredientName = normalizeOptionalText(ingredientValue);
        if (ingredientName == null) {
            return null;
        }
        return new UnitParseResult(unit, ingredientName);
    }

    private static UnitParseResult parseBareCountIngredient(String ingredientValue) {
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
        return new UnitParseResult(IngredientUnit.PCS, ingredientName);
    }

    private static String stripUnsupportedMeasureToken(String value) {
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

    private static String stripUnsupportedMeasureToken(String unitToken, String ingredientValue) {
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
        return normalizeOptionalText(withoutOf);
    }

    private static IngredientUnit parseSupportedUnit(String unitToken) {
        String normalizedUnitToken = normalizeUnitToken(unitToken);
        return switch (normalizedUnitToken) {
            case "pcs", "pc", "piece", "pieces", "st" -> IngredientUnit.PCS;
            case "pack", "packs", "pkt", "package", "packages" -> IngredientUnit.PACK;
            case "kg", "kilo", "kilos", "kilogram", "kilograms" -> IngredientUnit.KG;
            case "hg", "hectogram", "hectograms" -> IngredientUnit.HG;
            case "g", "gram", "grams" -> IngredientUnit.G;
            case "l", "liter", "liters", "litre", "litres" -> IngredientUnit.L;
            case "dl", "deciliter", "deciliters", "decilitre", "decilitres" -> IngredientUnit.DL;
            case "ml", "milliliter", "milliliters", "millilitre", "millilitres" -> IngredientUnit.ML;
            default -> parseSupportedKitchenMeasure(normalizedUnitToken);
        };
    }

    private static IngredientUnit parseSupportedKitchenMeasure(String normalizedUnitToken) {
        return switch (normalizedUnitToken) {
            case "tbsp", "tbsps", "tblsp", "tblsps", "tbl", "tbls", "tbs", "tablespoon", "tablespoons",
                    "msk", "matsk", "matsked", "matskedar" -> IngredientUnit.TBSP;
            case "tsp", "tsps", "teasp", "teasps", "teaspoon", "teaspoons",
                    "tsk", "tesk", "tesked", "teskedar" -> IngredientUnit.TSP;
            case "krm", "kryddmått" -> IngredientUnit.KRM;
            default -> null;
        };
    }

    private static String normalizeUnitToken(String unitToken) {
        return unitToken
                .toLowerCase(Locale.ROOT)
                .replace(".", "")
                .replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "")
                .trim();
    }

    private static boolean isIgnorableIngredientLine(String value) {
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

    private static boolean hasSecondaryMeasureToken(String ingredientName) {
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

    private static boolean isLooseFractionToken(String token) {
        return switch (token) {
            case "¼", "½", "¾", "⅓", "⅔", "⅛", "⅜", "⅝", "⅞" -> true;
            default -> false;
        };
    }

    private static BigDecimal parseUnicodeFractionQuantity(String value) {
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

    private static BigDecimal unicodeFractionValue(String token) {
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

    private static String normalizeRequiredName(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new RecipeImportFailedException("Imported recipe is missing a name");
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeIngredientLine(String value) {
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

    private static String normalizeFractionQuantity(String value) {
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

    private static BigDecimal parseFraction(String value) {
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

    private static String firstToken(String value) {
        int split = value.indexOf(' ');
        if (split < 0) {
            return value;
        }
        return value.substring(0, split);
    }

    private static String normalizeImportUrl(String value) {
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

    private static String deriveSourceName(String sourceUrl) {
        URI uri = URI.create(sourceUrl);
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return null;
        }
        String normalizedHost = host.startsWith("www.") ? host.substring(4) : host;
        return normalizedHost;
    }

    private static String deriveAssetRecipeName(RecipeAssetIntakeReference reference) {
        String candidate = firstNonBlank(reference.sourceLabel(), reference.originalFilename());
        if (candidate == null) {
            return null;
        }
        String strippedExtension = candidate.replaceFirst("\\.[\\p{Alnum}]{1,8}$", "");
        String normalized = normalizeOptionalText(strippedExtension);
        return normalized == null ? null : normalized;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = normalizeOptionalText(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private static RecipeOriginKind toAssetOriginKind(RecipeAssetIntakeKind kind) {
        return switch (kind) {
            case DOCUMENT -> RecipeOriginKind.DOCUMENT_IMPORT;
            case IMAGE -> RecipeOriginKind.IMAGE_IMPORT;
        };
    }

    record RecipeDraftSeed(
            String name,
            RecipeSource source,
            RecipeProvenance provenance,
            String servings,
            String shortNote,
            RecipeInstructions instructions,
            List<Ingredient> ingredients,
            RecipeDraftState state
    ) {
        RecipeDraftSeed {
            source = source == null ? new RecipeSource(null, null) : source;
            provenance = provenance == null ? new RecipeProvenance(RecipeOriginKind.MANUAL, null) : provenance;
            instructions = instructions == null ? new RecipeInstructions(null) : instructions;
            ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
            state = state == null ? RecipeDraftState.DRAFT_OPEN : state;
        }
    }

    private record UnitParseResult(IngredientUnit unit, String ingredientName) {
    }

    private record PastedTextSections(
            String servings,
            List<String> ingredientLines,
            String instructions
    ) {
    }

    private enum Section {
        NONE,
        INGREDIENTS,
        INSTRUCTIONS
    }
}
