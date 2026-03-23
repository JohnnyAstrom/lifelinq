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
  savedRecipeLabel: string;
  recipeOptionalLabel?: string;
  noRecipeAttached: string;
  useExistingRecipe: string;
  changeRecipe: string;
  openRecipe: string;
  openSavedRecipe: string;
  addRecipeDetails: string;
  reuseRecentMeal: string;
  recipeSummaryHint: string;
  savedRecipeSummaryHint: string;
  loadingRecipe: string;
  ingredientsSummarySuffix: string;
  shoppingLabel: string;
  shoppingAddHint: string;
  shoppingHandledTitle: string;
  shoppingHandledState: string;
  shoppingHandledOnList: (listName: string) => string;
  shoppingNeedsReviewAgain: string;
  addIngredientsToShoppingAction: string;
  reviewShoppingAgainAction: string;
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
  onOpenRecentMeals: () => void;
  showRecipePickerAction: boolean;
  hasIngredients: boolean;
  hasRecipeDraftContent: boolean;
  onOpenShoppingReview: () => void;
  hasShoppingHandled: boolean;
  needsShoppingReviewAgain: boolean;
  shoppingListName: string | null;
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
  onOpenRecentMeals,
  showRecipePickerAction,
  hasIngredients,
  hasRecipeDraftContent,
  onOpenShoppingReview,
  hasShoppingHandled,
  needsShoppingReviewAgain,
  shoppingListName,
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
  const recipeSectionLabel = hasExistingRecipe ? strings.savedRecipeLabel : strings.recipeLabel;
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
      : hasExistingRecipe
        ? strings.savedRecipeSummaryHint
        : strings.recipeSummaryHint;
  const recipePrompt = hasExistingRecipe
    ? strings.openSavedRecipe
    : hasRecipeDraftContent
      ? strings.openRecipe
      : strings.addRecipeDetails;
  const dayContext = initialDate.toLocaleDateString(undefined, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  });
  const shoppingStatusText = needsShoppingReviewAgain
    ? strings.shoppingNeedsReviewAgain
    : hasShoppingHandled
      ? (shoppingListName
        ? strings.shoppingHandledOnList(shoppingListName)
        : strings.shoppingHandledState)
      : strings.shoppingAddHint;
  const shoppingTitle = needsShoppingReviewAgain
    ? strings.reviewShoppingAgainAction
    : hasShoppingHandled
      ? strings.shoppingHandledTitle
      : strings.addIngredientsToShoppingAction;

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
              <Pressable
                onPress={onOpenRecentMeals}
                disabled={isActionPending}
                style={({ pressed }) => [
                  styles.recentMealActionRow,
                  pressed ? styles.recentMealActionRowPressed : null,
                  isActionPending ? styles.recipeSummaryCardDisabled : null,
                ]}
              >
                <Text style={styles.recentMealActionText}>{strings.reuseRecentMeal}</Text>
                <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
              </Pressable>
            </View>
            <View style={styles.sectionDivider} />
            <View style={styles.editorSection}>
              <View style={styles.recipeSectionHeader}>
                <Text style={styles.fieldLabel}>{recipeSectionLabel}</Text>
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
                <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
              </Pressable>
              {showRecipePickerAction ? (
                <Pressable
                  onPress={onOpenRecipePicker}
                  disabled={isActionPending || isRecipeLoading}
                  style={({ pressed }) => [
                    styles.recipeSecondaryActionRow,
                    pressed ? styles.recipeSecondaryActionRowPressed : null,
                    isActionPending || isRecipeLoading ? styles.recipeSummaryCardDisabled : null,
                  ]}
                >
                  <Text style={styles.recipeSecondaryActionText}>{recipeSelectionActionLabel}</Text>
                  <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
                </Pressable>
              ) : null}
            </View>
            {hasIngredients ? (
              <View style={styles.shoppingActionSection}>
                <Text style={styles.fieldLabel}>{strings.shoppingLabel}</Text>
                <Pressable
                  onPress={onOpenShoppingReview}
                  disabled={isActionPending}
                  style={({ pressed }) => [
                    styles.shoppingSummaryCard,
                    hasShoppingHandled ? styles.shoppingSummaryCardHandled : null,
                    needsShoppingReviewAgain ? styles.shoppingSummaryCardNeedsReview : null,
                    pressed ? styles.shoppingSummaryCardPressed : null,
                    isActionPending ? styles.shoppingSummaryCardDisabled : null,
                  ]}
                >
                  <View style={styles.shoppingSummaryLeading}>
                    <Ionicons
                      name={hasShoppingHandled ? 'checkmark-circle-outline' : 'cart-outline'}
                      size={18}
                      color={needsShoppingReviewAgain ? theme.colors.textPrimary : theme.colors.feature.meals}
                    />
                  </View>
                  <View style={styles.shoppingSummaryCopy}>
                    <Text style={styles.shoppingSummaryTitle}>{shoppingTitle}</Text>
                    <Text
                      style={[
                        styles.shoppingSummaryMeta,
                        needsShoppingReviewAgain ? styles.shoppingSummaryMetaNeedsReview : null,
                      ]}
                      numberOfLines={2}
                    >
                      {shoppingStatusText}
                    </Text>
                  </View>
                  <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
                </Pressable>
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
  recentMealActionRow: {
    marginTop: theme.spacing.xs,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  recentMealActionRowPressed: {
    opacity: 0.72,
  },
  recentMealActionText: {
    ...textStyles.body,
    color: theme.colors.textPrimary,
    fontWeight: '600',
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
    alignItems: 'center',
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
  recipeSecondaryActionRow: {
    marginTop: -2,
    marginLeft: theme.spacing.sm,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    minHeight: 32,
  },
  recipeSecondaryActionRowPressed: {
    opacity: 0.72,
  },
  recipeSecondaryActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
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
  shoppingSummaryCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  shoppingSummaryCardHandled: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
  shoppingSummaryCardNeedsReview: {
    borderColor: theme.colors.feature.meals,
    backgroundColor: theme.colors.surfaceSubtle,
  },
  shoppingSummaryCardPressed: {
    opacity: 0.8,
  },
  shoppingSummaryCardDisabled: {
    opacity: 0.6,
  },
  shoppingSummaryLeading: {
    width: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  shoppingSummaryCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  shoppingSummaryTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  shoppingSummaryMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  shoppingSummaryMetaNeedsReview: {
    color: theme.colors.textPrimary,
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
