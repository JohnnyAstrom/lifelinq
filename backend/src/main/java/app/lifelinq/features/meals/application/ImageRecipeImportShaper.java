package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

final class ImageRecipeImportShaper {
    private static final Pattern LEADING_QUANTITY_PATTERN = Pattern.compile(
            "^(?:ca\\s+)?(?:\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[\\.,]\\d+)?|[¼½¾⅓⅔⅛⅜⅝⅞]|\\d+[¼½¾⅓⅔⅛⅜⅝⅞])\\s+.+$"
    );
    private static final Pattern ATTACHED_UNIT_PATTERN = Pattern.compile(
            "^(?:ca\\s+)?\\d+(?:[\\.,]\\d+)?[\\p{L}]+\\s+.+$"
    );
    private static final Pattern STEP_PREFIX_PATTERN = Pattern.compile(
            "^(?:step\\s+)?\\d+[\\).:-]?\\s+.+$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern SIMPLE_SERVINGS_PATTERN = Pattern.compile(
            "^(?<value>\\d+(?:[\\.,]\\d+)?)\\s*(?:portioner?|port|servings?)\\.?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "^(?:ca\\s+)?\\d+(?:[\\.,]\\d+)?\\s*(?:min|mins?|tim|timmar?|h|hr|hrs|hour|hours)\\.?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern INGREDIENT_COUNT_PATTERN = Pattern.compile(
            "^\\d+\\s*(?:ingredienser|ingredients?)\\.?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern INGREDIENT_COUNT_AND_DURATION_PATTERN = Pattern.compile(
            "^\\d+\\s*(?:ingredienser|ingredients?)\\s+\\d+(?:[\\.,]\\d+)?\\s*(?:min|mins?|tim|timmar?|h|hr|hrs|hour|hours)\\.?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern SHORT_ALPHA_NOISE_PATTERN = Pattern.compile(
            "^[\\p{L}]{1,2}$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Set<String> INGREDIENT_HEADINGS = Set.of(
            "ingredients",
            "ingredienser",
            "du behöver",
            "det här behöver du"
    );
    private static final Set<String> INSTRUCTION_HEADINGS = Set.of(
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
            "tillagning",
            "gör så"
    );
    private static final Set<String> NOTE_HEADINGS = Set.of(
            "tips",
            "tip",
            "tips & variationer",
            "variation",
            "variationer",
            "servering",
            "till servering",
            "att servera"
    );
    private static final Set<String> ACTION_VERBS = Set.of(
            "add", "arrange", "bake", "blend", "boil", "brown", "brush", "chop", "combine", "cook",
            "cover", "cut", "drizzle", "fry", "fold", "grill", "heat", "knead", "lay", "let", "marinate",
            "mix", "place", "pour", "preheat", "roast", "serve", "simmer", "slice", "sprinkle", "stir",
            "top", "warm", "whisk",
            "baka", "blanda", "bryn", "finhacka", "fräs", "grädda", "garnera", "hacka", "häll", "hetta",
            "koka", "lägg", "mixa", "rör", "servera", "sjud", "skala", "skiva", "skär", "skar",
            "stek", "tillsätt", "toppa",
            "vänd", "värm"
    );
    private static final Set<String> PROMOTIONAL_MARKERS = Set.of(
            "i samarbete med",
            "presented by",
            "sponsored",
            "sponsor",
            "advertisement",
            "annons",
            "ad:"
    );
    private static final Set<String> SOURCE_MARKERS = Set.of(
            "foto:",
            "photo:",
            "image:",
            "bild:",
            "köket.se",
            "koket.se",
            "www.",
            "http://",
            "https://"
    );

    private final DocumentRecipeImportShaper documentImportShaper;

    ImageRecipeImportShaper(DocumentRecipeImportShaper documentImportShaper) {
        if (documentImportShaper == null) {
            throw new IllegalArgumentException("documentImportShaper must not be null");
        }
        this.documentImportShaper = documentImportShaper;
    }

    ParsedRecipeImportData shape(RecipeAssetIntakeReference reference, String extractedText) {
        return documentImportShaper.shape(reference, cleanOcrText(extractedText));
    }

    private String cleanOcrText(String extractedText) {
        List<String> normalizedLines = normalizeLines(extractedText);
        if (normalizedLines.isEmpty()) {
            return extractedText;
        }

        List<String> mergedLines = mergeTopTitleLines(normalizedLines);
        List<String> handoffLines = buildSectionAwareHandoff(mergedLines);
        if (!looksMeaningfullyUsable(handoffLines)) {
            handoffLines = filterNoiseBySection(mergedLines);
        }
        if (!looksMeaningfullyUsable(handoffLines)) {
            handoffLines = mergedLines;
        }
        return String.join("\n", handoffLines);
    }

    private List<String> filterNoiseBySection(List<String> lines) {
        List<String> result = new ArrayList<>();
        Section currentSection = Section.NONE;
        int promotionalSuppressionBudget = 0;

        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);
            if (line == null) {
                continue;
            }

            if (containsPromotionalMarker(line)) {
                promotionalSuppressionBudget = 2;
                continue;
            }

            if (isSectionHeading(line)) {
                currentSection = classifySection(line);
                promotionalSuppressionBudget = 0;
                result.add(line);
                continue;
            }

            if (promotionalSuppressionBudget > 0 && looksLikePromotionalContinuation(line)) {
                promotionalSuppressionBudget -= 1;
                continue;
            }

            if (shouldKeepLine(currentSection, line)) {
                promotionalSuppressionBudget = 0;
                result.add(line);
            }
        }

        return result;
    }

    private List<String> buildSectionAwareHandoff(List<String> lines) {
        List<String> preamble = new ArrayList<>();
        List<String> ingredientLines = new ArrayList<>();
        List<String> instructionLines = new ArrayList<>();
        List<String> noteLines = new ArrayList<>();
        String ingredientHeading = null;
        String instructionHeading = null;
        String noteHeading = null;
        Section currentSection = Section.NONE;
        int promotionalSuppressionBudget = 0;
        boolean instructionFlowStarted = false;

        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);
            if (line == null) {
                continue;
            }

            if (containsPromotionalMarker(line)) {
                promotionalSuppressionBudget = 2;
                continue;
            }

            if (isSectionHeading(line)) {
                currentSection = classifySection(line);
                promotionalSuppressionBudget = 0;
                switch (currentSection) {
                    case INGREDIENTS -> ingredientHeading = firstNonBlank(ingredientHeading, line);
                    case INSTRUCTIONS -> instructionHeading = firstNonBlank(instructionHeading, line);
                    case NOTES -> noteHeading = firstNonBlank(noteHeading, line);
                    case NONE -> {
                    }
                }
                continue;
            }

            if (promotionalSuppressionBudget > 0 && looksLikePromotionalContinuation(line)) {
                promotionalSuppressionBudget -= 1;
                continue;
            }

            if (!shouldKeepLine(currentSection, line)) {
                continue;
            }

            switch (currentSection) {
                case NONE -> preamble.add(line);
                case INGREDIENTS -> {
                    if (startsMethodFlow(line)) {
                        instructionHeading = firstNonBlank(instructionHeading, "Gör så här");
                        instructionLines.add(line);
                        instructionFlowStarted = true;
                    } else {
                        ingredientLines.add(line);
                    }
                }
                case INSTRUCTIONS -> {
                    if (startsMethodFlow(line)) {
                        instructionFlowStarted = true;
                        instructionLines.add(line);
                    } else if (instructionFlowStarted && shouldKeepInstructionSectionLine(line)) {
                        instructionLines.add(line);
                    }
                }
                case NOTES -> noteLines.add(line);
            }
        }

        if (ingredientHeading != null && !instructionLines.isEmpty()) {
            List<String> rescuedIngredientLines = new ArrayList<>();
            List<String> retainedInstructionLines = new ArrayList<>();
            for (String line : instructionLines) {
                if (looksLikeIngredientLine(line) && !startsMethodFlow(line)) {
                    rescuedIngredientLines.add(line);
                } else {
                    retainedInstructionLines.add(line);
                }
            }
            if (!rescuedIngredientLines.isEmpty()) {
                ingredientLines.addAll(rescuedIngredientLines);
                instructionLines.clear();
                instructionLines.addAll(retainedInstructionLines);
            }
        }

        List<String> result = new ArrayList<>();
        result.addAll(preamble);
        appendSection(result, ingredientHeading, "Ingredienser", ingredientLines);
        appendSection(result, instructionHeading, "Gör så här", instructionLines);
        appendSection(result, noteHeading, "Tips", noteLines);
        return result;
    }

    private void appendSection(List<String> result, String heading, String fallbackHeading, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        result.add(firstNonBlank(heading, fallbackHeading));
        result.addAll(lines);
    }

    private boolean shouldKeepLine(Section currentSection, String line) {
        if (looksLikeSourceLine(line) || looksLikeRecipeMetadataLine(line)) {
            return false;
        }

        return switch (currentSection) {
            case INGREDIENTS -> shouldKeepIngredientSectionLine(line);
            case INSTRUCTIONS -> shouldKeepInstructionSectionLine(line);
            case NOTES -> shouldKeepNoteSectionLine(line);
            case NONE -> true;
        };
    }

    private boolean shouldKeepIngredientSectionLine(String line) {
        if (isSimpleServingsLine(line)) {
            return false;
        }
        if (looksLikeIngredientLine(line) || isIngredientSubheading(line)) {
            return true;
        }
        return startsMethodFlow(line);
    }

    private boolean shouldKeepInstructionSectionLine(String line) {
        return startsMethodFlow(line) || looksLikeSentenceProse(line);
    }

    private boolean shouldKeepNoteSectionLine(String line) {
        return !looksLikeSourceLine(line) && !looksLikePromotionalContinuation(line);
    }

    private List<String> mergeTopTitleLines(List<String> lines) {
        int scanLimit = Math.min(lines.size(), 4);
        List<String> titleFragments = new ArrayList<>();
        int consumed = 0;

        for (int index = 0; index < scanLimit; index += 1) {
            String line = lines.get(index);
            boolean titleLike = index == 0
                    ? looksLikeStrongTitleFragment(line)
                    : looksLikeTitleContinuation(line, titleFragments);
            if (!titleLike) {
                break;
            }
            titleFragments.add(line);
            consumed += 1;
            if (titleFragments.size() >= 3) {
                break;
            }
        }

        if (titleFragments.size() < 2) {
            return lines;
        }

        String mergedTitle = joinTitleFragments(titleFragments);
        List<String> result = new ArrayList<>(lines.size() - consumed + 1);
        result.add(mergedTitle);
        result.addAll(lines.subList(consumed, lines.size()));
        return result;
    }

    private String joinTitleFragments(List<String> fragments) {
        StringBuilder builder = new StringBuilder();
        for (String fragment : fragments) {
            String normalized = normalizeLine(fragment);
            if (normalized == null) {
                continue;
            }
            if (builder.length() == 0) {
                builder.append(normalized);
            } else if (builder.charAt(builder.length() - 1) == '-') {
                builder.append(normalized);
            } else {
                builder.append(' ').append(normalized);
            }
        }
        return builder.toString().replaceAll("\\s+", " ").trim();
    }

    private boolean looksLikeStrongTitleFragment(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        if (isSectionHeading(normalized)
                || looksLikeRecipeMetadataLine(normalized)
                || containsPromotionalMarker(normalized)
                || looksLikeSourceLine(normalized)
                || startsMethodFlow(normalized)
                || endsWithSentencePunctuation(normalized)) {
            return false;
        }
        int wordCount = normalized.split("\\s+").length;
        return wordCount >= 1
                && wordCount <= 5
                && normalized.length() <= 40
                && (isMostlyUppercase(normalized) || isMostlyTitleCase(normalized));
    }

    private boolean looksLikeTitleContinuation(String line, List<String> titleFragments) {
        String normalized = normalizeLine(line);
        if (normalized == null || titleFragments == null || titleFragments.isEmpty()) {
            return false;
        }
        if (isSectionHeading(normalized)
                || looksLikeRecipeMetadataLine(normalized)
                || containsPromotionalMarker(normalized)
                || looksLikeSourceLine(normalized)
                || startsMethodFlow(normalized)
                || endsWithSentencePunctuation(normalized)) {
            return false;
        }

        int wordCount = normalized.split("\\s+").length;
        if (wordCount == 0 || wordCount > 4 || normalized.length() > 32) {
            return false;
        }

        if (isMostlyUppercase(normalized) || isMostlyTitleCase(normalized)) {
            return true;
        }

        return Character.isLowerCase(normalized.charAt(0))
                && !looksLikeSentenceProse(normalized)
                && (!looksLikeIngredientLine(normalized)
                || looksLikeTitleContinuationCue(normalized)
                || wordCount == 1);
    }

    private boolean looksLikeIngredientLine(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null || normalized.length() > 90) {
            return false;
        }
        if (isSectionHeading(normalized) || looksLikeRecipeMetadataLine(normalized) || looksLikeSourceLine(normalized)) {
            return false;
        }
        if (looksLikeShortOcrNoise(normalized) || looksLikeLikelyProductCaption(normalized)) {
            return false;
        }
        if (LEADING_QUANTITY_PATTERN.matcher(normalized).matches()
                || ATTACHED_UNIT_PATTERN.matcher(normalized).matches()) {
            return true;
        }
        String[] tokens = normalized.split("\\s+");
        if (tokens.length == 0 || tokens.length > 4) {
            return false;
        }
        if (startsMethodFlow(normalized) || isMostlyUppercase(normalized)) {
            return false;
        }
        if (tokens.length == 1) {
            return normalized.length() >= 4;
        }
        if (tokens.length >= 3 && isMostlyTitleCase(normalized)) {
            return false;
        }
        return !containsSentenceConnector(normalized) && !looksLikeSentenceProse(normalized);
    }

    private boolean startsMethodFlow(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        return STEP_PREFIX_PATTERN.matcher(normalized).matches() || startsWithActionVerb(normalized);
    }

    private boolean looksLikeSentenceProse(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        return normalized.split("\\s+").length >= 5 || endsWithSentencePunctuation(normalized);
    }

    private boolean looksLikeRecipeMetadataLine(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        return INGREDIENT_COUNT_PATTERN.matcher(normalized).matches()
                || INGREDIENT_COUNT_AND_DURATION_PATTERN.matcher(normalized).matches()
                || DURATION_PATTERN.matcher(normalized).matches();
    }

    private boolean isSimpleServingsLine(String line) {
        String normalized = normalizeLine(line);
        return normalized != null && SIMPLE_SERVINGS_PATTERN.matcher(normalized).matches();
    }

    private boolean isSectionHeading(String line) {
        String heading = normalizeHeading(line);
        return INGREDIENT_HEADINGS.contains(heading)
                || INSTRUCTION_HEADINGS.contains(heading)
                || NOTE_HEADINGS.contains(heading);
    }

    private Section classifySection(String line) {
        String heading = normalizeHeading(line);
        if (INGREDIENT_HEADINGS.contains(heading)) {
            return Section.INGREDIENTS;
        }
        if (INSTRUCTION_HEADINGS.contains(heading)) {
            return Section.INSTRUCTIONS;
        }
        return Section.NOTES;
    }

    private boolean isIngredientSubheading(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        if (normalized.endsWith(":")) {
            return true;
        }
        return normalized.split("\\s+").length <= 3 && isMostlyUppercase(normalized);
    }

    private boolean containsPromotionalMarker(String line) {
        String normalized = normalizeHeading(line);
        for (String marker : PROMOTIONAL_MARKERS) {
            if (normalized.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikeSourceLine(String line) {
        String lowercase = normalizeHeading(line);
        for (String marker : SOURCE_MARKERS) {
            if (lowercase.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikePromotionalContinuation(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null || isSectionHeading(normalized) || looksLikeIngredientLine(normalized) || startsMethodFlow(normalized)) {
            return false;
        }
        int wordCount = normalized.split("\\s+").length;
        return (isMostlyTitleCase(normalized) || isMostlyUppercase(normalized) || looksLikeSentenceProse(normalized))
                && wordCount >= 2
                && wordCount <= 12;
    }

    private boolean looksLikeShortOcrNoise(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        return SHORT_ALPHA_NOISE_PATTERN.matcher(normalized).matches();
    }

    private boolean looksLikeLikelyProductCaption(String line) {
        String normalized = normalizeLine(line);
        if (normalized == null) {
            return false;
        }
        String[] tokens = normalized.split("\\s+");
        return tokens.length >= 3
                && tokens.length <= 6
                && isMostlyTitleCase(normalized)
                && !containsSentenceConnector(normalized)
                && !startsWithActionVerb(normalized);
    }

    private boolean looksLikeTitleContinuationCue(String value) {
        String lowercase = value.toLowerCase(Locale.ROOT);
        return lowercase.startsWith("med ")
                || lowercase.startsWith("och ")
                || lowercase.startsWith("till ")
                || lowercase.endsWith(" och")
                || lowercase.endsWith(" med")
                || lowercase.endsWith(" till")
                || containsSentenceConnector(lowercase);
    }

    private boolean looksMeaningfullyUsable(List<String> lines) {
        if (lines == null || lines.size() < 2) {
            return false;
        }
        int totalCharacters = lines.stream().mapToInt(String::length).sum();
        return totalCharacters >= 24;
    }

    private List<String> normalizeLines(String extractedText) {
        List<String> lines = new ArrayList<>();
        if (extractedText == null) {
            return lines;
        }
        String normalized = extractedText
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replace('|', ' ');
        for (String rawLine : normalized.split("\n")) {
            String line = normalizeLine(rawLine);
            if (line == null || isDecorativeNoise(line)) {
                continue;
            }
            lines.add(line);
        }
        return lines;
    }

    private String normalizeHeading(String line) {
        String normalized = normalizeLine(line == null ? null : line.replace(":", ""));
        return normalized == null ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeLine(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isDecorativeNoise(String value) {
        if (value.length() <= 2 && value.chars().allMatch(Character::isDigit)) {
            return true;
        }
        return value.chars().filter(Character::isLetterOrDigit).count() < 2;
    }

    private boolean startsWithActionVerb(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return false;
        }
        String firstToken = normalized
                .replaceFirst("^[\\-•*]+\\s*", "")
                .split("\\s+")[0]
                .replaceAll("[^\\p{L}]", "")
                .toLowerCase(Locale.ROOT);
        return ACTION_VERBS.contains(firstToken);
    }

    private boolean containsSentenceConnector(String value) {
        String lowercase = value.toLowerCase(Locale.ROOT);
        return lowercase.contains(" och ")
                || lowercase.contains(" med ")
                || lowercase.contains(" till ")
                || lowercase.contains(" som ")
                || lowercase.contains(" the ")
                || lowercase.contains(" and ")
                || lowercase.contains(" with ");
    }

    private boolean endsWithSentencePunctuation(String value) {
        return value.endsWith(".") || value.endsWith("!") || value.endsWith("?");
    }

    private boolean isMostlyUppercase(String value) {
        long letters = value.chars().filter(Character::isLetter).count();
        if (letters == 0) {
            return false;
        }
        long uppercaseLetters = value.chars().filter(Character::isUpperCase).count();
        return uppercaseLetters >= Math.max(letters - 1, 1);
    }

    private boolean isMostlyTitleCase(String value) {
        String[] tokens = value.split("\\s+");
        int titleCasedTokens = 0;
        int letterTokens = 0;
        for (String token : tokens) {
            String cleaned = token.replaceAll("[^\\p{L}-]", "");
            if (cleaned.isBlank()) {
                continue;
            }
            letterTokens += 1;
            if (Character.isUpperCase(cleaned.charAt(0))) {
                titleCasedTokens += 1;
            }
        }
        return letterTokens > 0 && titleCasedTokens >= Math.max(1, letterTokens - 1);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = normalizeLine(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private enum Section {
        NONE,
        INGREDIENTS,
        INSTRUCTIONS,
        NOTES
    }
}
