package app.lifelinq.features.meals.application;

import app.lifelinq.features.meals.contract.ParsedRecipeImportData;
import app.lifelinq.features.meals.contract.RecipeAssetIntakeReference;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DocumentRecipeImportShaper {
    private static final Pattern LEADING_QUANTITY_PATTERN = Pattern.compile(
            "^(?<quantity>(?:\\d+\\s+\\d+/\\d+|\\d+/\\d+|\\d+(?:[\\.,]\\d+)?|[¼½¾⅓⅔⅛⅜⅝⅞]|\\d+[¼½¾⅓⅔⅛⅜⅝⅞]))\\s*(?<rest>.+)$"
    );
    private static final Pattern ATTACHED_UNIT_PATTERN = Pattern.compile(
            "^(?<quantity>\\d+(?:[\\.,]\\d+)?)(?<unit>[\\p{L}]+)\\s+(?<rest>.+)$"
    );
    private static final Pattern STEP_PREFIX_PATTERN = Pattern.compile(
            "^(?:step\\s+)?\\d+[\\).:-]?\\s+.+$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern SERVINGS_PREFIX_PATTERN = Pattern.compile(
            "^(?:(?:servings?|yield|portioner?|serverar))\\s*[:\\-]?\\s*(?<value>.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Pattern SERVINGS_SUFFIX_PATTERN = Pattern.compile(
            "^(?<value>\\d+(?:[\\.,]\\d+)?)\\s*(?:servings?|portioner?|port)\\.?$",
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

    ParsedRecipeImportData shape(RecipeAssetIntakeReference reference, String extractedText) {
        List<String> lines = normalizeLines(extractedText);
        if (lines.isEmpty()) {
            throw new RecipeImportFailedException("We could not find enough recipe content in that file to review");
        }

        String fallbackTitle = deriveCleanLabel(reference, true);
        TitleCandidate titleCandidate = selectTitle(lines);
        String title = titleCandidate != null
                ? normalizeTitle(titleCandidate.line())
                : fallbackTitle;

        List<String> contentLines = new ArrayList<>(lines);
        if (titleCandidate != null) {
            contentLines.remove(titleCandidate.index());
        }

        SectionParseResult sections = extractSections(contentLines);
        String sourceName = deriveSourceName(reference, title);
        return new ParsedRecipeImportData(
                title,
                sourceName,
                null,
                sections.servings(),
                sections.shortNote(),
                joinLines(sections.instructions()),
                List.copyOf(sections.ingredients())
        );
    }

    private SectionParseResult extractSections(List<String> lines) {
        List<String> introLines = new ArrayList<>();
        List<String> ingredientLines = new ArrayList<>();
        List<String> instructionLines = new ArrayList<>();
        List<String> noteLines = new ArrayList<>();
        String servings = null;
        Section currentSection = Section.NONE;

        for (int index = 0; index < lines.size(); index += 1) {
            String line = lines.get(index);
            String servingsValue = extractServings(line);
            if (servings == null && servingsValue != null) {
                servings = servingsValue;
                continue;
            }
            if (isIngredientHeading(line)) {
                currentSection = Section.INGREDIENTS;
                continue;
            }
            if (isInstructionHeading(line)) {
                currentSection = Section.INSTRUCTIONS;
                continue;
            }
            if (isNoteHeading(line)) {
                currentSection = Section.NOTES;
                continue;
            }

            switch (currentSection) {
                case INGREDIENTS -> {
                    if (isIngredientSubheading(line)) {
                        continue;
                    }
                    if (looksLikeIngredientLine(line)) {
                        ingredientLines.add(cleanIngredientLine(line));
                        continue;
                    }
                    if (!ingredientLines.isEmpty() && startsMethodFlow(line)) {
                        currentSection = Section.INSTRUCTIONS;
                        instructionLines.add(cleanInstructionLine(line));
                        continue;
                    }
                    if (looksLikeNoteLine(line)) {
                        noteLines.add(line);
                    }
                }
                case INSTRUCTIONS -> {
                    if (looksLikeInstructionLine(line) || looksLikeInstructionContinuation(line)) {
                        instructionLines.add(cleanInstructionLine(line));
                        continue;
                    }
                    if (looksLikeNoteLine(line)) {
                        noteLines.add(line);
                    }
                }
                case NOTES -> noteLines.add(line);
                case NONE -> {
                    if (looksLikeIngredientLine(line) && countIngredientRun(lines, index) >= 2) {
                        currentSection = Section.INGREDIENTS;
                        ingredientLines.add(cleanIngredientLine(line));
                        continue;
                    }
                    if (startsMethodFlow(line)) {
                        currentSection = Section.INSTRUCTIONS;
                        instructionLines.add(cleanInstructionLine(line));
                        continue;
                    }
                    introLines.add(line);
                }
            }
        }

        if (ingredientLines.isEmpty()) {
            ingredientLines.addAll(extractFallbackIngredientLines(lines));
        }

        if (instructionLines.isEmpty()) {
            instructionLines.addAll(extractFallbackInstructionLines(lines, ingredientLines));
        }

        String shortNote = buildShortNote(introLines, noteLines);
        return new SectionParseResult(servings, ingredientLines, instructionLines, shortNote);
    }

    private List<String> extractFallbackIngredientLines(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (looksLikeIngredientLine(line) && !isIngredientSubheading(line)) {
                result.add(cleanIngredientLine(line));
            }
        }
        return result;
    }

    private List<String> extractFallbackInstructionLines(List<String> lines, List<String> ingredientLines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (ingredientLines.contains(cleanIngredientLine(line))) {
                continue;
            }
            if (looksLikeInstructionLine(line) || looksLikeInstructionContinuation(line)) {
                result.add(cleanInstructionLine(line));
            }
        }
        return result;
    }

    private TitleCandidate selectTitle(List<String> lines) {
        TitleCandidate bestCandidate = null;
        int limit = Math.min(lines.size(), 12);
        for (int index = 0; index < limit; index += 1) {
            String line = lines.get(index);
            int score = scoreTitleCandidate(line, index);
            if (score < 80) {
                continue;
            }
            if (bestCandidate == null || score > bestCandidate.score()) {
                bestCandidate = new TitleCandidate(index, line, score);
            }
        }
        return bestCandidate;
    }

    private int scoreTitleCandidate(String line, int index) {
        if (isIngredientHeading(line) || isInstructionHeading(line) || isNoteHeading(line)) {
            return Integer.MIN_VALUE;
        }
        if (looksLikeIngredientLine(line) || looksLikeInstructionLine(line) || looksLikeMetadataLine(line)) {
            return Integer.MIN_VALUE;
        }
        if (line.endsWith(":")) {
            return Integer.MIN_VALUE;
        }

        int length = line.length();
        int wordCount = line.split("\\s+").length;
        if (length < 4 || length > 80 || wordCount > 10) {
            return Integer.MIN_VALUE;
        }

        int score = 120 - (index * 10);
        if (wordCount >= 2 && wordCount <= 6) {
            score += 24;
        }
        if (!endsWithSentencePunctuation(line)) {
            score += 18;
        }
        if (isMostlyUppercase(line) || isMostlyTitleCase(line)) {
            score += 20;
        }
        if (length <= 36) {
            score += 12;
        }
        return score;
    }

    private boolean looksLikeIngredientLine(String value) {
        String normalized = cleanIngredientLine(value);
        if (normalized == null || normalized.length() > 90) {
            return false;
        }
        if (isIngredientHeading(normalized) || isInstructionHeading(normalized) || isNoteHeading(normalized)) {
            return false;
        }
        if (endsWithSentencePunctuation(normalized) && !startsWithQuantity(normalized)) {
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
        if (startsWithActionVerb(normalized)
                || looksLikeMetadataLine(normalized)
                || isMostlyUppercase(normalized)
                || (tokens.length > 1 && isMostlyTitleCase(normalized))) {
            return false;
        }
        if (tokens.length == 1) {
            return !endsWithSentencePunctuation(normalized);
        }
        return !containsSentenceConnector(normalized);
    }

    private boolean looksLikeInstructionLine(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null || isIngredientHeading(normalized) || isNoteHeading(normalized)) {
            return false;
        }
        if (startsMethodFlow(normalized)) {
            return true;
        }
        return endsWithSentencePunctuation(normalized) && normalized.split("\\s+").length >= 5;
    }

    private boolean startsMethodFlow(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return false;
        }
        return STEP_PREFIX_PATTERN.matcher(normalized).matches() || startsWithActionVerb(normalized);
    }

    private boolean looksLikeInstructionContinuation(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return false;
        }
        if (looksLikeIngredientLine(normalized) || isIngredientSubheading(normalized)) {
            return false;
        }
        return normalized.split("\\s+").length >= 5;
    }

    private boolean looksLikeNoteLine(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return false;
        }
        return normalized.split("\\s+").length >= 4 && !looksLikeIngredientLine(normalized);
    }

    private boolean looksLikeMetadataLine(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return false;
        }
        return extractServings(normalized) != null
                || normalized.contains(".pdf")
                || normalized.contains("www.")
                || normalized.contains("http://")
                || normalized.contains("https://");
    }

    private boolean isIngredientHeading(String value) {
        return INGREDIENT_HEADINGS.contains(normalizeHeading(value));
    }

    private boolean isInstructionHeading(String value) {
        return INSTRUCTION_HEADINGS.contains(normalizeHeading(value));
    }

    private boolean isNoteHeading(String value) {
        return NOTE_HEADINGS.contains(normalizeHeading(value));
    }

    private boolean isIngredientSubheading(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return false;
        }
        if (normalized.endsWith(":")) {
            return true;
        }
        return normalized.split("\\s+").length <= 3 && isMostlyUppercase(normalized);
    }

    private int countIngredientRun(List<String> lines, int startIndex) {
        int count = 0;
        for (int index = startIndex; index < lines.size(); index += 1) {
            if (!looksLikeIngredientLine(lines.get(index))) {
                break;
            }
            count += 1;
        }
        return count;
    }

    private String extractServings(String value) {
        Matcher prefixMatcher = SERVINGS_PREFIX_PATTERN.matcher(value);
        if (prefixMatcher.matches()) {
            return normalizeLine(prefixMatcher.group("value"));
        }
        Matcher suffixMatcher = SERVINGS_SUFFIX_PATTERN.matcher(value);
        if (suffixMatcher.matches()) {
            return normalizeLine(suffixMatcher.group("value"));
        }
        return null;
    }

    private String deriveSourceName(RecipeAssetIntakeReference reference, String title) {
        String cleaned = deriveCleanLabel(reference, false);
        if (cleaned == null) {
            return null;
        }
        if (title != null && cleaned.equalsIgnoreCase(title)) {
            return null;
        }
        return cleaned;
    }

    private String deriveCleanLabel(RecipeAssetIntakeReference reference, boolean preferTitleShaping) {
        String candidate = firstNonBlank(reference.sourceLabel(), reference.originalFilename());
        if (candidate == null) {
            return null;
        }
        String lastPathSegment = candidate
                .replace('\\', '/')
                .replaceFirst("^.+/", "");
        String decoded = URLDecoder.decode(lastPathSegment, StandardCharsets.UTF_8);
        String withoutExtension = decoded.replaceFirst("\\.[\\p{Alnum}]{1,8}$", "");
        String normalized = normalizeLine(withoutExtension
                .replace('_', ' ')
                .replaceAll("\\s*-\\s*", " - ")
                .replaceAll("\\s+", " "));
        if (normalized == null) {
            return null;
        }
        return preferTitleShaping ? normalizeTitle(normalized) : normalized;
    }

    private String buildShortNote(List<String> introLines, List<String> noteLines) {
        List<String> candidates = !noteLines.isEmpty() ? noteLines : introLines;
        if (candidates.isEmpty()) {
            return null;
        }
        List<String> selected = new ArrayList<>();
        int totalLength = 0;
        for (String line : candidates) {
            if (looksLikeIngredientLine(line) || (noteLines.isEmpty() && startsMethodFlow(line))) {
                continue;
            }
            if (totalLength >= 240) {
                break;
            }
            selected.add(line);
            totalLength += line.length();
            if (selected.size() >= 2) {
                break;
            }
        }
        return selected.isEmpty() ? null : String.join(" ", selected);
    }

    private List<String> normalizeLines(String extractedText) {
        List<String> lines = new ArrayList<>();
        if (extractedText == null) {
            return lines;
        }
        String normalized = extractedText
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ');
        for (String rawLine : normalized.split("\n")) {
            String line = normalizeLine(rawLine);
            if (line == null || isDecorativeNoise(line)) {
                continue;
            }
            lines.add(line);
        }
        return lines;
    }

    private String cleanIngredientLine(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return null;
        }
        return normalized.replaceFirst("^[\\-•*]+\\s*", "");
    }

    private String cleanInstructionLine(String value) {
        return normalizeLine(value);
    }

    private String normalizeTitle(String value) {
        String normalized = normalizeLine(value);
        if (normalized == null) {
            return null;
        }
        if (!isMostlyUppercase(normalized)) {
            return normalized;
        }
        StringBuilder builder = new StringBuilder();
        for (String part : normalized.toLowerCase(Locale.ROOT).split("(?<=-)\\s*|\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '-') {
                builder.append(' ');
            }
            if (part.endsWith("-")) {
                builder.append(capitalize(part.substring(0, part.length() - 1))).append('-');
            } else {
                builder.append(capitalize(part));
            }
        }
        return builder.toString().replaceAll("\\s+", " ").trim();
    }

    private String normalizeHeading(String value) {
        String normalized = normalizeLine(value == null ? null : value.replace(":", ""));
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

    private boolean startsWithQuantity(String value) {
        return LEADING_QUANTITY_PATTERN.matcher(value).matches()
                || ATTACHED_UNIT_PATTERN.matcher(value).matches();
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

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        return String.join("\n", lines);
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

    private record TitleCandidate(int index, String line, int score) {
    }

    private record SectionParseResult(
            String servings,
            List<String> ingredients,
            List<String> instructions,
            String shortNote
    ) {
    }

    private enum Section {
        NONE,
        INGREDIENTS,
        INSTRUCTIONS,
        NOTES
    }
}
