import { Ionicons } from '@expo/vector-icons';
import { useEffect, useState } from 'react';
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
  collapseIngredient?: string;
  importedIngredientHint?: string;
  importedIngredientNeedsReviewHint?: string;
  importedIngredientReviewTag?: string;
};

type Props = {
  row: MealIngredientRow;
  isActive: boolean;
  isReadOnly?: boolean;
  isImportDraft?: boolean;
  onActivate: () => void;
  onCollapse?: () => void;
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
  onCollapse,
  onRemove,
  onChangeName,
  onChangeQuantity,
  onToggleUnit,
  strings,
}: Props) {
  const [isDeleteArmed, setIsDeleteArmed] = useState(false);
  const isEffectivelyEmpty = isMealIngredientRowEffectivelyEmpty(row);
  const isExpanded = isActive || isEffectivelyEmpty;
  const meta = formatIngredientMeta(row);
  const importReview = isImportDraft ? getImportedIngredientReviewInfo(row) : null;
  const importedFallbackHint = importReview?.reviewHint ?? null;
  const needsImportReview = !!importReview?.needsReview;

  useEffect(() => {
    if (!isDeleteArmed) {
      return;
    }

    const timeoutId = setTimeout(() => {
      setIsDeleteArmed(false);
    }, 1800);

    return () => clearTimeout(timeoutId);
  }, [isDeleteArmed]);

  function handleRemovePress() {
    if (!isDeleteArmed) {
      setIsDeleteArmed(true);
      return;
    }

    setIsDeleteArmed(false);
    onRemove();
  }

  if (isReadOnly) {
    return (
      <View style={[
        styles.compactRow,
        isImportDraft ? styles.importCompactRow : null,
        needsImportReview ? styles.importReviewRow : null,
      ]}>
        <View style={styles.compactMain}>
          <Text style={styles.compactTitle} numberOfLines={1}>
            {row.name.trim()}
          </Text>
          {meta ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {meta}
            </Text>
          ) : null}
        </View>
        {needsImportReview && strings.importedIngredientReviewTag ? (
          <View style={styles.reviewPill}>
            <Text style={styles.reviewPillText}>{strings.importedIngredientReviewTag}</Text>
          </View>
        ) : null}
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
          isImportDraft ? styles.importCompactRow : null,
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
          ) : null}
        </View>
        {needsImportReview && strings.importedIngredientReviewTag ? (
          <View style={styles.reviewPill}>
            <Text style={styles.reviewPillText}>{strings.importedIngredientReviewTag}</Text>
          </View>
        ) : null}
        <Pressable
          onPress={handleRemovePress}
          accessibilityRole="button"
          accessibilityLabel={strings.removeIngredient}
          style={({ pressed }) => [
            styles.iconButton,
            isDeleteArmed ? styles.deleteIconButtonArmed : null,
            pressed ? styles.iconButtonPressed : null,
          ]}
        >
          <Ionicons
            name={isDeleteArmed ? 'trash-outline' : 'close-outline'}
            size={16}
            color={isDeleteArmed ? theme.colors.danger : theme.colors.textSecondary}
          />
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
          autoFocus={isEffectivelyEmpty && isActive}
          style={[styles.ingredientNameInput, styles.lightInput]}
        />
        <View style={styles.expandedActions}>
          {!isEffectivelyEmpty && onCollapse ? (
            <Pressable
              onPress={onCollapse}
              accessibilityRole="button"
              accessibilityLabel={strings.collapseIngredient ?? 'Collapse ingredient'}
              style={({ pressed }) => [
                styles.iconButton,
                styles.collapseIconButton,
                pressed ? styles.iconButtonPressed : null,
              ]}
            >
              <Ionicons name="chevron-up" size={16} color={theme.colors.textSecondary} />
            </Pressable>
          ) : null}
          <Pressable
            onPress={handleRemovePress}
            accessibilityRole="button"
            accessibilityLabel={strings.removeIngredient}
            style={({ pressed }) => [
              styles.iconButton,
              isDeleteArmed ? styles.deleteIconButtonArmed : null,
              pressed ? styles.iconButtonPressed : null,
            ]}
          >
            <Ionicons
              name={isDeleteArmed ? 'trash-outline' : 'close-outline'}
              size={16}
              color={isDeleteArmed ? theme.colors.danger : theme.colors.textSecondary}
            />
          </Pressable>
        </View>
      </View>

      <View style={styles.expandedSecondaryRow}>
        <AppInput
          placeholder={strings.quantityPlaceholder}
          value={row.quantityText}
          onChangeText={onChangeQuantity}
          onFocus={onActivate}
          keyboardType="decimal-pad"
          style={[styles.quantityInput, styles.lightInput]}
        />
      </View>

      {needsImportReview ? (
        <View style={styles.expandedReviewRow}>
          {strings.importedIngredientReviewTag ? (
            <View style={styles.reviewPill}>
              <Text style={styles.reviewPillText}>{strings.importedIngredientReviewTag}</Text>
            </View>
          ) : null}
          {importReview?.keptAsText && strings.importedIngredientNeedsReviewHint ? (
            <Text style={styles.importReviewInline}>{strings.importedIngredientNeedsReviewHint}</Text>
          ) : importedFallbackHint && strings.importedIngredientHint ? (
            <Text style={styles.importReviewInline}>
              {strings.importedIngredientHint} {importedFallbackHint}
            </Text>
          ) : null}
        </View>
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
    paddingVertical: 6,
    backgroundColor: theme.colors.surface,
  },
  importCompactRow: {
    borderWidth: 0,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
    borderRadius: 0,
    paddingHorizontal: 0,
    paddingVertical: theme.spacing.sm,
    backgroundColor: 'transparent',
  },
  compactRowPressed: {
    opacity: 0.9,
  },
  importReviewRow: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderRadius: theme.radius.sm,
    paddingHorizontal: theme.spacing.xs,
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
  reviewPill: {
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: 3,
    backgroundColor: theme.colors.surface,
  },
  reviewPillText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  expandedRow: {
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 6,
    backgroundColor: theme.colors.surface,
  },
  expandedRowActive: {
    borderColor: theme.colors.feature.meals,
    shadowColor: theme.colors.feature.meals,
    shadowOpacity: 0.08,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 3 },
    elevation: 1,
  },
  expandedImportReviewRow: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderColor: theme.colors.border,
  },
  expandedTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  expandedActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  expandedSecondaryRow: {
    flexDirection: 'row',
    gap: theme.spacing.sm,
  },
  ingredientNameInput: {
    flex: 1,
  },
  lightInput: {
    backgroundColor: theme.colors.surface,
  },
  quantityInput: {
    width: 96,
  },
  unitChipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    paddingTop: 2,
  },
  expandedReviewRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  importReviewInline: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    flex: 1,
    minWidth: 0,
  },
  iconButton: {
    width: 28,
    height: 28,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
  },
  collapseIconButton: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
  deleteIconButtonArmed: {
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.danger,
  },
  iconButtonPressed: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
});
