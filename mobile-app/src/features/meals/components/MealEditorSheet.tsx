import { Keyboard, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import { type MealIngredientRow } from '../utils/ingredientRows';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type DayOption = {
  dayNumber: number;
  date: Date;
  label: string;
};

type MealEditorSheetStrings = {
  planMealTitle: string;
  planningLabel: string;
  dayLabel: string;
  mealTypeLabel: string;
  recipeLabel: string;
  newRecipeLabel: string;
  usingRecipeLabel: string;
  useExistingRecipe: string;
  changeRecipe: string;
  recipeNameLabel: string;
  recipeNamePlaceholder: string;
  ingredientsLabel: string;
  ingredientsRecipeHint?: string;
  addIngredients: string;
  editIngredients: string;
  ingredientsEmptyState: string;
  ingredientsSummarySuffix: string;
  loadingIngredients: string;
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
  dayOptions: DayOption[];
  onSelectDay: (dayNumber: number) => void;
  onClose: () => void;
  onSave: () => void;
  onRemove: () => void;
  selectedMealType: MealType | null;
  onSelectMealType: (mealType: MealType) => void;
  mealTypeLabels: Record<MealType, string>;
  recipeTitle: string;
  onChangeRecipeTitle: (value: string) => void;
  ingredientRows: MealIngredientRow[];
  isRecipeLoading: boolean;
  onOpenRecipePicker: () => void;
  onOpenIngredients: () => void;
  hasIngredients: boolean;
  onOpenShoppingReview: () => void;
  hasExistingMeal: boolean;
  hasExistingRecipe: boolean;
  isSavingMeal: boolean;
  isRemovingMeal: boolean;
  isActionPending: boolean;
  strings: MealEditorSheetStrings;
};

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER'];

function sameCalendarDay(left: Date, right: Date) {
  return left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate();
}

export function MealEditorSheet({
  initialDate,
  dayOptions,
  onSelectDay,
  onClose,
  onSave,
  onRemove,
  selectedMealType,
  onSelectMealType,
  mealTypeLabels,
  recipeTitle,
  onChangeRecipeTitle,
  ingredientRows,
  isRecipeLoading,
  onOpenRecipePicker,
  onOpenIngredients,
  hasIngredients,
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
  const ingredientActionLabel = hasIngredients
    ? strings.editIngredients
    : strings.addIngredients;
  const saveActionLabel = isSavingMeal ? strings.savingMeal : strings.saveMeal;
  const removeActionLabel = isRemovingMeal ? strings.removingMeal : strings.removeMeal;
  const recipeIdentityLabel = hasExistingRecipe
    ? strings.usingRecipeLabel
    : strings.newRecipeLabel;
  const recipeSelectionActionLabel = hasExistingRecipe
    ? strings.changeRecipe
    : strings.useExistingRecipe;

  const ingredientEntryHint = !hasIngredients && !isRecipeLoading
    ? strings.addIngredients
    : null;

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
              <Text style={styles.sectionLabel}>{strings.planningLabel}</Text>
              <View style={styles.contextField}>
                <Text style={styles.fieldLabel}>{strings.dayLabel}</Text>
                <View style={styles.dayChipRow}>
                  {dayOptions.map((option) => (
                    <AppChip
                      key={option.dayNumber}
                      label={option.label.split(' ')[0]}
                      active={sameCalendarDay(option.date, initialDate)}
                      accentKey="meals"
                      onPress={() => onSelectDay(option.dayNumber)}
                    />
                  ))}
                </View>
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
              <Text style={styles.sectionLabel}>{strings.recipeLabel}</Text>
              <View style={styles.recipeMetaRow}>
                <View style={styles.identityBadge}>
                  <Text style={styles.identityBadgeText}>{recipeIdentityLabel}</Text>
                </View>
                <AppButton
                  title={recipeSelectionActionLabel}
                  onPress={onOpenRecipePicker}
                  variant="ghost"
                  disabled={isActionPending || isRecipeLoading}
                />
              </View>
              <View style={styles.contextField}>
                <Text style={styles.fieldLabel}>{strings.recipeNameLabel}</Text>
                <AppInput
                  placeholder={strings.recipeNamePlaceholder}
                  value={recipeTitle}
                  onChangeText={onChangeRecipeTitle}
                />
              </View>
              <View style={styles.ingredientsSection}>
                <View style={styles.ingredientsSectionHeader}>
                  <View style={styles.ingredientsSectionCopy}>
                    <Text style={styles.fieldLabel}>{strings.ingredientsLabel}</Text>
                    {strings.ingredientsRecipeHint ? (
                      <Text style={styles.ingredientsHint}>{strings.ingredientsRecipeHint}</Text>
                    ) : null}
                  </View>
                  <AppButton
                    title={ingredientActionLabel}
                    onPress={onOpenIngredients}
                    variant="ghost"
                    disabled={isActionPending}
                  />
                </View>
                {isRecipeLoading ? (
                  <Subtle>{strings.loadingIngredients}</Subtle>
                ) : null}
                {ingredientEntryHint ? <Text style={styles.ingredientsHint}>{strings.ingredientsEmptyState}</Text> : null}
                {!isRecipeLoading && hasIngredients ? (
                  <Pressable onPress={onOpenIngredients} style={styles.ingredientsSummaryCard}>
                    <Text style={styles.ingredientsSummaryTitle}>{ingredientSummary}</Text>
                    {ingredientPreview ? (
                      <Text style={styles.ingredientsPreview} numberOfLines={1}>
                        {ingredientPreview}
                      </Text>
                    ) : null}
                  </Pressable>
                ) : null}
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
  dayChipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
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
  sectionDivider: {
    height: 1,
    backgroundColor: theme.colors.border,
  },
  recipeMetaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  identityBadge: {
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
  ingredientsInput: {
    minHeight: 64,
    textAlignVertical: 'top',
  },
  ingredientsSection: {
    gap: theme.spacing.sm,
  },
  ingredientsSectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  ingredientsSectionCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  ingredientsSummaryCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
  },
  ingredientsSummaryTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  ingredientsHint: {
    ...textStyles.subtle,
  },
  ingredientsPreview: {
    ...textStyles.subtle,
    marginTop: 2,
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
    backgroundColor: theme.colors.surfaceAlt,
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
