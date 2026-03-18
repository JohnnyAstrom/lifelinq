import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  MEAL_INGREDIENT_UNIT_OPTIONS,
  type MealIngredientRow,
} from '../utils/ingredientRows';

export type MealIngredientEditorRowStrings = {
  ingredientNamePlaceholder: string;
  quantityPlaceholder: string;
  removeIngredient: string;
  importedIngredientHint?: string;
  importedIngredientNeedsReviewHint?: string;
};

type Props = {
  row: MealIngredientRow;
  isActive: boolean;
  isReadOnly?: boolean;
  isImportDraft?: boolean;
  onActivate: () => void;
  onRemove: () => void;
  onChangeName: (value: string) => void;
  onChangeQuantity: (value: string) => void;
  onToggleUnit: (value: typeof MEAL_INGREDIENT_UNIT_OPTIONS[number]['value']) => void;
  strings: MealIngredientEditorRowStrings;
};

function formatIngredientMeta(row: MealIngredientRow) {
  if (!row.quantityText) {
    return null;
  }

  if (!row.unit) {
    return row.quantityText;
  }

  const unitLabel = MEAL_INGREDIENT_UNIT_OPTIONS.find((option) => option.value === row.unit)?.label;
  return unitLabel ? `${row.quantityText} ${unitLabel}` : row.quantityText;
}

function normalizeComparableText(value: string | null) {
  if (!value) {
    return '';
  }
  return value.trim().replace(/\s+/g, ' ').toLocaleLowerCase();
}

function getImportedFallbackHint(row: MealIngredientRow) {
  const rawText = row.rawText?.trim();
  if (!rawText || row.quantityText.trim().length > 0 || !/\d/.test(rawText)) {
    return null;
  }

  const normalizedRaw = normalizeComparableText(rawText);
  const normalizedName = normalizeComparableText(row.name);
  if (!normalizedRaw || normalizedRaw === normalizedName) {
    return null;
  }

  return rawText;
}

export type ImportedIngredientReviewInfo = {
  needsReview: boolean;
  keptAsText: boolean;
  originalLine: string | null;
  reviewHint: string | null;
};

export function getImportedIngredientReviewInfo(row: MealIngredientRow): ImportedIngredientReviewInfo {
  const rawText = row.rawText?.trim() ?? null;
  if (!rawText) {
    return {
      needsReview: false,
      keptAsText: false,
      originalLine: null,
      reviewHint: null,
    };
  }

  const fallbackHint = getImportedFallbackHint(row);
  if (fallbackHint) {
    return {
      needsReview: true,
      keptAsText: false,
      originalLine: rawText,
      reviewHint: fallbackHint,
    };
  }

  const normalizedRaw = normalizeComparableText(rawText);
  const normalizedName = normalizeComparableText(row.name);
  const hasStructuredAmount = row.quantityText.trim().length > 0;
  if (!hasStructuredAmount && normalizedRaw.length > 0 && normalizedRaw === normalizedName) {
    return {
      needsReview: true,
      keptAsText: true,
      originalLine: rawText,
      reviewHint: rawText,
    };
  }

  return {
    needsReview: false,
    keptAsText: false,
    originalLine: rawText,
    reviewHint: null,
  };
}

export function isMealIngredientRowEffectivelyEmpty(row: MealIngredientRow) {
  return row.name.trim().length === 0;
}

export function MealIngredientEditorRow({
  row,
  isActive,
  isReadOnly = false,
  isImportDraft = false,
  onActivate,
  onRemove,
  onChangeName,
  onChangeQuantity,
  onToggleUnit,
  strings,
}: Props) {
  const isEffectivelyEmpty = isMealIngredientRowEffectivelyEmpty(row);
  const isExpanded = isActive || isEffectivelyEmpty;
  const meta = formatIngredientMeta(row);
  const importReview = isImportDraft ? getImportedIngredientReviewInfo(row) : null;
  const importedFallbackHint = importReview?.reviewHint ?? null;
  const needsImportReview = !!importReview?.needsReview;

  if (isReadOnly) {
    return (
      <View style={[styles.compactRow, needsImportReview ? styles.importReviewRow : null]}>
        <View style={styles.compactMain}>
          <Text style={styles.compactTitle} numberOfLines={1}>
            {row.name.trim()}
          </Text>
          {meta ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {meta}
            </Text>
          ) : needsImportReview && importReview?.keptAsText && strings.importedIngredientNeedsReviewHint ? (
            <Text style={styles.compactMeta} numberOfLines={2}>
              {strings.importedIngredientNeedsReviewHint}
            </Text>
          ) : importedFallbackHint && strings.importedIngredientHint ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {strings.importedIngredientHint} {importedFallbackHint}
            </Text>
          ) : null}
        </View>
      </View>
    );
  }

  if (!isExpanded) {
    return (
      <Pressable
        onPress={onActivate}
        accessibilityRole="button"
        style={({ pressed }) => [
          styles.compactRow,
          needsImportReview ? styles.importReviewRow : null,
          pressed ? styles.compactRowPressed : null,
        ]}
      >
        <View style={styles.compactMain}>
          <Text style={styles.compactTitle} numberOfLines={1}>
            {row.name.trim()}
          </Text>
          {meta ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {meta}
            </Text>
          ) : needsImportReview && importReview?.keptAsText && strings.importedIngredientNeedsReviewHint ? (
            <Text style={styles.compactMeta} numberOfLines={2}>
              {strings.importedIngredientNeedsReviewHint}
            </Text>
          ) : importedFallbackHint && strings.importedIngredientHint ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {strings.importedIngredientHint} {importedFallbackHint}
            </Text>
          ) : null}
        </View>
        <Pressable
          onPress={onRemove}
          accessibilityRole="button"
          accessibilityLabel={strings.removeIngredient}
          style={({ pressed }) => [
            styles.removeIconButton,
            pressed ? styles.removeIconButtonPressed : null,
          ]}
        >
          <Ionicons name="close-outline" size={16} color={theme.colors.textSecondary} />
        </Pressable>
      </Pressable>
    );
  }

  return (
      <View style={[
      styles.expandedRow,
      isActive ? styles.expandedRowActive : null,
      needsImportReview ? styles.expandedImportReviewRow : null,
    ]}>
      <View style={styles.expandedTopRow}>
        <AppInput
          placeholder={strings.ingredientNamePlaceholder}
          value={row.name}
          onChangeText={onChangeName}
          onFocus={onActivate}
          style={styles.ingredientNameInput}
        />
        <Pressable
          onPress={onRemove}
          accessibilityRole="button"
          accessibilityLabel={strings.removeIngredient}
          style={({ pressed }) => [
            styles.removeIconButton,
            pressed ? styles.removeIconButtonPressed : null,
          ]}
        >
          <Ionicons name="close-outline" size={16} color={theme.colors.textSecondary} />
        </Pressable>
      </View>

      <View style={styles.expandedSecondaryRow}>
        <AppInput
          placeholder={strings.quantityPlaceholder}
          value={row.quantityText}
          onChangeText={onChangeQuantity}
          onFocus={onActivate}
          keyboardType="decimal-pad"
          style={styles.quantityInput}
        />
      </View>

      {needsImportReview
        && (
          (importReview?.keptAsText && strings.importedIngredientNeedsReviewHint)
          || (importedFallbackHint && strings.importedIngredientHint)
        ) ? (
        <Text style={styles.importReviewInline}>
          {importReview?.keptAsText && strings.importedIngredientNeedsReviewHint
            ? strings.importedIngredientNeedsReviewHint
            : `${strings.importedIngredientHint} ${importedFallbackHint}`}
        </Text>
      ) : null}

      {row.quantityText.length > 0 && isActive ? (
        <View style={styles.unitChipRow}>
          {MEAL_INGREDIENT_UNIT_OPTIONS.map((option) => (
            <AppChip
              key={option.value}
              label={option.label}
              active={row.unit === option.value}
              accentKey="meals"
              onPress={() => onToggleUnit(option.value)}
            />
          ))}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  compactRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surfaceAlt,
  },
  compactRowPressed: {
    opacity: 0.9,
  },
  importReviewRow: {
    borderLeftWidth: 3,
    borderLeftColor: theme.colors.feature.meals,
  },
  compactMain: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  compactTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  compactMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  expandedRow: {
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surface,
  },
  expandedRowActive: {
    borderColor: theme.colors.feature.meals,
  },
  expandedImportReviewRow: {
    borderLeftWidth: 3,
    borderLeftColor: theme.colors.feature.meals,
  },
  expandedTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  expandedSecondaryRow: {
    flexDirection: 'row',
    gap: theme.spacing.sm,
  },
  ingredientNameInput: {
    flex: 1,
  },
  quantityInput: {
    width: 112,
  },
  unitChipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    paddingTop: 2,
  },
  importReviewInline: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    paddingLeft: 2,
  },
  removeIconButton: {
    width: 28,
    height: 28,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeIconButtonPressed: {
    backgroundColor: theme.colors.surfaceAlt,
  },
});
