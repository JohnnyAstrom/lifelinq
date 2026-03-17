import { useEffect, useRef, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  type MealIngredientRow,
  type MealIngredientUnit,
} from '../utils/ingredientRows';
import {
  isMealIngredientRowEffectivelyEmpty,
  MealIngredientEditorRow,
  type MealIngredientEditorRowStrings,
} from './MealIngredientEditorRow';

type Strings = MealIngredientEditorRowStrings & {
  eyebrow: string;
  title: string;
  subtitle?: string;
  newRecipeLabel: string;
  usingRecipeLabel: string;
  mealSpecificRecipeLabel: string;
  editingSavedRecipeLabel: string;
  newRecipeTitle: string;
  recipeContextHint?: string;
  mealAttachmentLabel?: string;
  mealAttachmentValue?: string;
  editSavedRecipeAction?: string;
  editingSavedRecipeHint?: string;
  recipeNameLabel: string;
  recipeNamePlaceholder: string;
  recipeContentLabel: string;
  recipeSourceLabel: string;
  recipeSourcePlaceholder: string;
  recipeShortNoteLabel: string;
  recipeShortNotePlaceholder: string;
  recipeInstructionsLabel: string;
  recipeInstructionsPlaceholder: string;
  ingredientsLabel: string;
  ingredientsRecipeHint?: string;
  ingredientsEmptyState: string;
  loadingIngredients: string;
  saveAsNewRecipeHint?: string;
  saveRecipe?: string;
  savingRecipe?: string;
  addIngredient: string;
  close: string;
};

type Props = {
  recipeTitle: string;
  onChangeRecipeTitle: (value: string) => void;
  recipeSource: string;
  onChangeRecipeSource: (value: string) => void;
  recipeShortNote: string;
  onChangeRecipeShortNote: (value: string) => void;
  recipeInstructions: string;
  onChangeRecipeInstructions: (value: string) => void;
  ingredientRows: MealIngredientRow[];
  isRecipeLoading: boolean;
  hasExistingRecipe: boolean;
  hasIngredients: boolean;
  showSaveAsNewRecipeHint: boolean;
  canEnterSavedRecipeEditMode: boolean;
  isEditingSavedRecipeDirectly: boolean;
  isActionPending: boolean;
  onAddIngredientRow: () => void;
  onRemoveIngredientRow: (rowId: string) => void;
  onChangeIngredientName: (rowId: string, value: string) => void;
  onChangeIngredientQuantity: (rowId: string, value: string) => void;
  onToggleIngredientUnit: (rowId: string, value: MealIngredientUnit) => void;
  onStartEditingSavedRecipeDirectly: () => void;
  onSave?: () => void;
  onClose: () => void;
  isSaving?: boolean;
  error?: string | null;
  strings: Strings;
};

export function MealRecipeDetailSheet({
  recipeTitle,
  onChangeRecipeTitle,
  recipeSource,
  onChangeRecipeSource,
  recipeShortNote,
  onChangeRecipeShortNote,
  recipeInstructions,
  onChangeRecipeInstructions,
  ingredientRows,
  isRecipeLoading,
  hasExistingRecipe,
  hasIngredients,
  showSaveAsNewRecipeHint,
  canEnterSavedRecipeEditMode,
  isEditingSavedRecipeDirectly,
  isActionPending,
  onAddIngredientRow,
  onRemoveIngredientRow,
  onChangeIngredientName,
  onChangeIngredientQuantity,
  onToggleIngredientUnit,
  onStartEditingSavedRecipeDirectly,
  onSave,
  onClose,
  isSaving = false,
  error = null,
  strings,
}: Props) {
  const [activeRowId, setActiveRowId] = useState<string | null>(null);
  const previousRowCountRef = useRef(ingredientRows.length);
  const resolvedTitle = recipeTitle.trim().length > 0
    ? recipeTitle.trim()
    : strings.newRecipeTitle;
  const isMealSpecificDraft = hasExistingRecipe
    && showSaveAsNewRecipeHint
    && !isEditingSavedRecipeDirectly;
  const identityLabel = isMealSpecificDraft
    ? strings.mealSpecificRecipeLabel
    : isEditingSavedRecipeDirectly
      ? strings.editingSavedRecipeLabel
    : hasExistingRecipe
      ? strings.usingRecipeLabel
      : strings.newRecipeLabel;

  useEffect(() => {
    const previousCount = previousRowCountRef.current;
    previousRowCountRef.current = ingredientRows.length;

    if (ingredientRows.length === 0) {
      if (activeRowId !== null) {
        setActiveRowId(null);
      }
      return;
    }

    if (ingredientRows.length > previousCount) {
      setActiveRowId(ingredientRows[ingredientRows.length - 1]?.id ?? null);
      return;
    }

    if (activeRowId === null) {
      return;
    }

    const hasActiveRow = activeRowId != null && ingredientRows.some((row) => row.id === activeRowId);
    if (hasActiveRow) {
      return;
    }

    const firstEmptyRow = ingredientRows.find((row) => isMealIngredientRowEffectivelyEmpty(row));
    setActiveRowId(firstEmptyRow?.id ?? null);
  }, [ingredientRows, activeRowId]);

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={styles.eyebrow}>{strings.eyebrow}</Text>
          <Text style={textStyles.h2}>{resolvedTitle}</Text>
          {strings.subtitle ? <Subtle>{strings.subtitle}</Subtle> : null}
          <View style={styles.headerMeta}>
            <View style={styles.identityBadge}>
              <Text style={styles.identityBadgeText}>{identityLabel}</Text>
            </View>
            {strings.mealAttachmentLabel && strings.mealAttachmentValue ? (
              <View style={styles.attachmentRow}>
                <Text style={styles.attachmentLabel}>{strings.mealAttachmentLabel}</Text>
                <Text style={styles.attachmentValue}>{strings.mealAttachmentValue}</Text>
              </View>
            ) : null}
            {strings.recipeContextHint ? (
              <Text style={styles.contextHint}>{strings.recipeContextHint}</Text>
            ) : null}
            {showSaveAsNewRecipeHint && !isEditingSavedRecipeDirectly && strings.saveAsNewRecipeHint ? (
              <Subtle>{strings.saveAsNewRecipeHint}</Subtle>
            ) : null}
            {isEditingSavedRecipeDirectly && strings.editingSavedRecipeHint ? (
              <Subtle>{strings.editingSavedRecipeHint}</Subtle>
            ) : null}
            {canEnterSavedRecipeEditMode && strings.editSavedRecipeAction ? (
              <AppButton
                title={strings.editSavedRecipeAction}
                onPress={onStartEditingSavedRecipeDirectly}
                variant="ghost"
                disabled={isActionPending || isRecipeLoading}
              />
            ) : null}
          </View>
        </View>

        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.section}>
              <Text style={styles.fieldLabel}>{strings.recipeNameLabel}</Text>
              <AppInput
                placeholder={strings.recipeNamePlaceholder}
                value={recipeTitle}
                onChangeText={onChangeRecipeTitle}
              />
            </View>

            <View style={styles.section}>
              <Text style={styles.fieldLabel}>{strings.recipeContentLabel}</Text>
              <View style={styles.subSection}>
                <Text style={styles.fieldLabel}>{strings.recipeSourceLabel}</Text>
                <AppInput
                  placeholder={strings.recipeSourcePlaceholder}
                  value={recipeSource}
                  onChangeText={onChangeRecipeSource}
                />
              </View>
              <View style={styles.subSection}>
                <Text style={styles.fieldLabel}>{strings.recipeShortNoteLabel}</Text>
                <AppInput
                  placeholder={strings.recipeShortNotePlaceholder}
                  value={recipeShortNote}
                  onChangeText={onChangeRecipeShortNote}
                  multiline
                  style={styles.noteInput}
                />
              </View>
              <View style={styles.subSection}>
                <Text style={styles.fieldLabel}>{strings.recipeInstructionsLabel}</Text>
                <AppInput
                  placeholder={strings.recipeInstructionsPlaceholder}
                  value={recipeInstructions}
                  onChangeText={onChangeRecipeInstructions}
                  multiline
                  style={styles.instructionsInput}
                />
              </View>
            </View>

            <View style={styles.section}>
              <View style={styles.sectionHeader}>
                <View style={styles.sectionCopy}>
                  <Text style={styles.fieldLabel}>{strings.ingredientsLabel}</Text>
                  {strings.ingredientsRecipeHint ? (
                    <Text style={styles.ingredientsHint}>{strings.ingredientsRecipeHint}</Text>
                  ) : null}
                </View>
                <AppButton
                  title={strings.addIngredient}
                  onPress={onAddIngredientRow}
                  variant="ghost"
                  disabled={isActionPending}
                />
              </View>

              {isRecipeLoading ? <Subtle>{strings.loadingIngredients}</Subtle> : null}

              {!isRecipeLoading && !hasIngredients ? (
                <Text style={styles.ingredientsHint}>{strings.ingredientsEmptyState}</Text>
              ) : null}

              {!isRecipeLoading && hasIngredients ? (
                <View style={styles.ingredientList}>
                  {ingredientRows.map((row) => (
                    <MealIngredientEditorRow
                      key={row.id}
                      row={row}
                      isActive={row.id === activeRowId}
                      onActivate={() => setActiveRowId(row.id)}
                      onRemove={() => onRemoveIngredientRow(row.id)}
                      onChangeName={(value) => onChangeIngredientName(row.id, value)}
                      onChangeQuantity={(value) => onChangeIngredientQuantity(row.id, value)}
                      onToggleUnit={(value) => onToggleIngredientUnit(row.id, value)}
                      strings={strings}
                    />
                  ))}
                </View>
              ) : null}
            </View>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            {onSave && strings.saveRecipe ? (
              <AppButton
                title={isSaving && strings.savingRecipe ? strings.savingRecipe : strings.saveRecipe}
                onPress={onSave}
                fullWidth
                disabled={isActionPending || isRecipeLoading}
                accentKey="meals"
              />
            ) : null}

            <AppButton
              title={strings.close}
              onPress={onClose}
              variant="secondary"
              fullWidth
              disabled={isActionPending}
            />
          </ScrollView>
        </View>
      </View>
    </OverlaySheet>
  );
}

const styles = StyleSheet.create({
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    paddingTop: theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.layout.sheetPadding,
    borderWidth: 1,
    borderColor: theme.colors.border,
    overflow: 'hidden',
  },
  layout: {
    maxHeight: '100%',
    flexShrink: 1,
    minHeight: 0,
  },
  header: {
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
    gap: theme.spacing.xs,
  },
  eyebrow: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    fontWeight: '700',
  },
  headerMeta: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
  },
  body: {
    flexShrink: 1,
    minHeight: 0,
  },
  scroll: {
    minHeight: 0,
    maxHeight: '100%',
    marginTop: theme.spacing.sm,
  },
  scrollContent: {
    gap: theme.spacing.md,
    minWidth: 0,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
  },
  section: {
    gap: theme.spacing.sm,
  },
  subSection: {
    gap: theme.spacing.xs,
  },
  fieldLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  noteInput: {
    minHeight: 72,
    textAlignVertical: 'top',
  },
  instructionsInput: {
    minHeight: 140,
    textAlignVertical: 'top',
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  sectionCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  identityBadge: {
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 4,
    backgroundColor: theme.colors.surfaceAlt,
  },
  identityBadgeText: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '600',
  },
  contextHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  attachmentRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  attachmentLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  attachmentValue: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '600',
  },
  ingredientsHint: {
    ...textStyles.subtle,
  },
  ingredientList: {
    gap: theme.spacing.xs,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
