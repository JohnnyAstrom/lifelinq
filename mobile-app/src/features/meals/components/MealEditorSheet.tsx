import { Ionicons } from '@expo/vector-icons';
import { Keyboard, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import { type MealIngredientRow } from '../utils/ingredientRows';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEditorSheetStrings = {
  planMealTitle: string;
  mealTitleLabel: string;
  mealTitlePlaceholder: string;
  mealTypeLabel: string;
  recipeLabel: string;
  recipeOptionalLabel?: string;
  noRecipeAttached: string;
  useExistingRecipe: string;
  changeRecipe: string;
  openRecipe: string;
  addRecipeDetails: string;
  recipeSummaryHint: string;
  loadingRecipe: string;
  ingredientsSummarySuffix: string;
  shoppingLabel: string;
  addIngredientsToShoppingAction: string;
  saveMeal: string;
  savingMeal: string;
  removeMeal: string;
  removingMeal: string;
  close: string;
};

type Props = {
  initialDate: Date;
  onClose: () => void;
  onSave: () => void;
  onRemove: () => void;
  selectedMealType: MealType | null;
  onSelectMealType: (mealType: MealType) => void;
  mealTypeLabels: Record<MealType, string>;
  mealTitle: string;
  onChangeMealTitle: (value: string) => void;
  recipeTitle: string;
  ingredientRows: MealIngredientRow[];
  isRecipeLoading: boolean;
  onOpenRecipeDetail: () => void;
  onOpenRecipePicker: () => void;
  hasIngredients: boolean;
  hasRecipeDraftContent: boolean;
  onOpenShoppingReview: () => void;
  hasExistingMeal: boolean;
  hasExistingRecipe: boolean;
  isSavingMeal: boolean;
  isRemovingMeal: boolean;
  isActionPending: boolean;
  strings: MealEditorSheetStrings;
};

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER'];

export function MealEditorSheet({
  initialDate,
  onClose,
  onSave,
  onRemove,
  selectedMealType,
  onSelectMealType,
  mealTypeLabels,
  mealTitle,
  onChangeMealTitle,
  recipeTitle,
  ingredientRows,
  isRecipeLoading,
  onOpenRecipeDetail,
  onOpenRecipePicker,
  hasIngredients,
  hasRecipeDraftContent,
  onOpenShoppingReview,
  hasExistingMeal,
  hasExistingRecipe,
  isSavingMeal,
  isRemovingMeal,
  isActionPending,
  strings,
}: Props) {
  if (!selectedMealType) {
    return null;
  }

  const ingredientPreview = ingredientRows
    .map((row) => row.name.trim())
    .filter((name) => name.length > 0)
    .slice(0, 2)
    .join(', ');
  const ingredientCount = ingredientRows
    .map((row) => row.name.trim())
    .filter((name) => name.length > 0)
    .length;
  const ingredientSummary = ingredientCount === 1
      ? `1 ${strings.ingredientsSummarySuffix}`
      : `${ingredientCount} ${strings.ingredientsSummarySuffix}`;
  const saveActionLabel = isSavingMeal ? strings.savingMeal : strings.saveMeal;
  const removeActionLabel = isRemovingMeal ? strings.removingMeal : strings.removeMeal;
  const recipeSelectionActionLabel = hasExistingRecipe
    ? strings.changeRecipe
    : strings.useExistingRecipe;
  const recipeSummary = hasRecipeDraftContent && recipeTitle.trim().length > 0
    ? recipeTitle.trim()
    : hasRecipeDraftContent
      ? strings.openRecipe
      : strings.noRecipeAttached;
  const recipeMeta = isRecipeLoading
    ? strings.loadingRecipe
    : hasIngredients
      ? ingredientPreview
        ? `${ingredientSummary} · ${ingredientPreview}`
        : ingredientSummary
      : strings.recipeSummaryHint;
  const recipePrompt = hasRecipeDraftContent
    ? strings.openRecipe
    : strings.addRecipeDetails;
  const dayContext = initialDate.toLocaleDateString(undefined, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  });

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.sheetLayout}>
        <View style={styles.sheetStickyHeader}>
          <Text style={textStyles.h2}>{strings.planMealTitle}</Text>
        </View>

        <View style={styles.sheetBody}>
          <ScrollView
            style={styles.sheetScroll}
            contentContainerStyle={styles.sheetScrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.editorSection}>
              <View style={styles.planningContextBlock}>
                <Text style={styles.dayContextText}>{dayContext}</Text>
                <Subtle style={styles.planningContextValue}>{mealTypeLabels[selectedMealType]}</Subtle>
              </View>
              <View style={styles.contextField}>
                <Text style={styles.fieldLabel}>{strings.mealTitleLabel}</Text>
                <AppInput
                  value={mealTitle}
                  onChangeText={onChangeMealTitle}
                  placeholder={strings.mealTitlePlaceholder}
                  multiline
                  style={styles.mealTitleInput}
                />
              </View>
              <View style={styles.contextField}>
                <Text style={styles.fieldLabel}>{strings.mealTypeLabel}</Text>
                <View style={styles.mealTypeRow}>
                  {MEAL_TYPES.map((mealType) => (
                    <AppChip
                      key={mealType}
                      label={mealTypeLabels[mealType]}
                      active={mealType === selectedMealType}
                      accentKey="meals"
                      onPress={() => {
                        Keyboard.dismiss();
                        onSelectMealType(mealType);
                      }}
                    />
                  ))}
                </View>
              </View>
            </View>
            <View style={styles.sectionDivider} />
            <View style={styles.editorSection}>
              <View style={styles.recipeSectionHeader}>
                <Text style={styles.fieldLabel}>{strings.recipeLabel}</Text>
                {strings.recipeOptionalLabel ? (
                  <Subtle style={styles.recipeSectionOptional}>{strings.recipeOptionalLabel}</Subtle>
                ) : null}
              </View>
              <Pressable
                onPress={onOpenRecipeDetail}
                disabled={isActionPending || isRecipeLoading}
                style={({ pressed }) => [
                  styles.recipeSummaryCard,
                  pressed ? styles.recipeSummaryCardPressed : null,
                  isActionPending || isRecipeLoading ? styles.recipeSummaryCardDisabled : null,
                ]}
              >
                <View style={styles.recipeSummaryCardCopy}>
                  <Text
                    style={[
                      styles.recipeSummaryTitle,
                      !hasRecipeDraftContent ? styles.recipeSummaryTitlePlaceholder : null,
                    ]}
                    numberOfLines={2}
                  >
                    {recipeSummary}
                  </Text>
                  <Text style={styles.recipeSummaryMeta} numberOfLines={2}>
                    {recipeMeta}
                  </Text>
                </View>
                <View style={styles.recipeSummaryAction}>
                  <Text style={styles.recipeSummaryActionText}>{recipePrompt}</Text>
                  <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
                </View>
              </Pressable>
              <View style={styles.recipeSecondaryActionRow}>
                <AppButton
                  title={recipeSelectionActionLabel}
                  onPress={onOpenRecipePicker}
                  variant="ghost"
                  disabled={isActionPending || isRecipeLoading}
                />
              </View>
            </View>
            {hasIngredients ? (
              <View style={styles.shoppingActionSection}>
                <Text style={styles.sectionLabel}>{strings.shoppingLabel}</Text>
                <AppButton
                  title={strings.addIngredientsToShoppingAction}
                  onPress={onOpenShoppingReview}
                  variant="ghost"
                  disabled={isActionPending}
                />
              </View>
            ) : null}
            <View style={styles.sheetFooterActions}>
              <AppButton
                title={saveActionLabel}
                onPress={onSave}
                fullWidth
                accentKey="meals"
                disabled={isActionPending}
              />
              <View style={styles.sheetFooterSecondaryActions}>
                {hasExistingMeal ? (
                  <AppButton
                    title={removeActionLabel}
                    onPress={onRemove}
                    variant="ghost"
                    disabled={isActionPending}
                  />
                ) : null}
                <Pressable
                  onPress={onClose}
                  disabled={isActionPending}
                  style={({ pressed }) => [
                    styles.footerCloseLink,
                    isActionPending ? styles.footerCloseLinkDisabled : null,
                    pressed ? styles.footerCloseLinkPressed : null,
                  ]}
                >
                  <Text style={styles.footerCloseText}>{strings.close}</Text>
                </Pressable>
              </View>
            </View>
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
  sheetStickyHeader: {
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  sheetLayout: {
    maxHeight: '100%',
    flexShrink: 1,
    minHeight: 0,
  },
  sheetBody: {
    flexShrink: 1,
    minHeight: 0,
  },
  editorSection: {
    gap: theme.spacing.sm,
  },
  contextField: {
    gap: theme.spacing.xs,
  },
  planningContextBlock: {
    gap: 2,
  },
  dayContextText: {
    ...textStyles.h2,
  },
  planningContextValue: {
    color: theme.colors.textSecondary,
  },
  sectionLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  fieldLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  mealTypeRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  mealTitleInput: {
    ...textStyles.h2,
    fontWeight: '700',
    lineHeight: 32,
    minHeight: 68,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
    textAlignVertical: 'top',
  },
  sectionDivider: {
    height: 1,
    backgroundColor: theme.colors.border,
  },
  recipeSectionHeader: {
    flexDirection: 'row',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: theme.spacing.xs,
  },
  recipeSectionOptional: {
    color: theme.colors.textSecondary,
  },
  recipeSummaryCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: theme.spacing.sm,
  },
  recipeSummaryCardPressed: {
    opacity: 0.8,
  },
  recipeSummaryCardDisabled: {
    opacity: 0.6,
  },
  recipeSummaryCardCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  recipeSummaryTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  recipeSummaryTitlePlaceholder: {
    color: theme.colors.textSecondary,
    fontWeight: '500',
  },
  recipeSummaryMeta: {
    ...textStyles.subtle,
  },
  recipeSummaryAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
    paddingTop: 2,
  },
  recipeSummaryActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  recipeSecondaryActionRow: {
    alignItems: 'flex-start',
    marginTop: -2,
  },
  sheetScroll: {
    minHeight: 0,
    maxHeight: '100%',
    marginTop: theme.spacing.sm,
  },
  sheetScrollContent: {
    gap: theme.spacing.sm,
    minWidth: 0,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
  },
  shoppingActionSection: {
    gap: theme.spacing.xs,
  },
  sheetFooterActions: {
    gap: theme.spacing.sm,
    paddingTop: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  sheetFooterSecondaryActions: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: theme.spacing.sm,
  },
  footerCloseLink: {
    minHeight: 36,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    borderRadius: theme.radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  footerCloseLinkPressed: {
    opacity: 0.7,
  },
  footerCloseLinkDisabled: {
    opacity: 0.5,
  },
  footerCloseText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
});
