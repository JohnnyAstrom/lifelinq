import { Keyboard, Pressable, ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import { type MealIngredientRow } from '../utils/ingredientRows';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type ShoppingListOption = {
  id: string;
  name: string;
};

type DayOption = {
  dayNumber: number;
  date: Date;
  label: string;
};

type MealEditorSheetStrings = {
  planMealTitle: string;
  dayLabel: string;
  mealTitlePlaceholder: string;
  ingredientsLabel: string;
  addIngredients: string;
  editIngredients: string;
  ingredientsEmptyState: string;
  ingredientsSummarySuffix: string;
  loadingIngredients: string;
  addIngredientsToShopping: string;
  noShoppingLists: string;
  shoppingSyncFailed: string;
  saveMeal: string;
  removeMeal: string;
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
  onOpenIngredients: () => void;
  pushToShopping: boolean;
  onChangePushToShopping: (value: boolean) => void;
  lists: ShoppingListOption[];
  effectiveListId: string | null;
  onSelectListId: (id: string) => void;
  shoppingSyncError: string | null;
  hasExistingMeal: boolean;
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
  onOpenIngredients,
  pushToShopping,
  onChangePushToShopping,
  lists,
  effectiveListId,
  onSelectListId,
  shoppingSyncError,
  hasExistingMeal,
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
  const hasIngredients = ingredientCount > 0;
  const ingredientSummary = ingredientCount === 1
      ? `1 ${strings.ingredientsSummarySuffix}`
      : `${ingredientCount} ${strings.ingredientsSummarySuffix}`;
  const ingredientActionLabel = hasIngredients
    ? strings.editIngredients
    : strings.addIngredients;

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
            <View style={styles.daySection}>
              <Text style={styles.sectionLabel}>{strings.dayLabel}</Text>
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
            <AppInput
              placeholder={strings.mealTitlePlaceholder}
              value={recipeTitle}
              onChangeText={onChangeRecipeTitle}
            />
            <View style={styles.ingredientsSection}>
              <View style={styles.ingredientsSectionHeader}>
                <Text style={styles.sectionLabel}>{strings.ingredientsLabel}</Text>
                <AppButton
                  title={ingredientActionLabel}
                  onPress={onOpenIngredients}
                  variant="ghost"
                />
              </View>
              {isRecipeLoading ? (
                <Subtle>{strings.loadingIngredients}</Subtle>
              ) : null}
              {ingredientEntryHint ? (
                <Text style={styles.ingredientsHint}>{strings.ingredientsEmptyState}</Text>
              ) : null}
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
            <View style={styles.toggleRow}>
              {hasIngredients ? (
                <>
                  <Text style={styles.toggleLabel}>{strings.addIngredientsToShopping}</Text>
                  <Switch value={pushToShopping} onValueChange={onChangePushToShopping} />
                </>
              ) : null}
            </View>
            {hasIngredients && pushToShopping ? (
              <View style={styles.lists}>
                {lists.length === 0 ? (
                  <Subtle>{strings.noShoppingLists}</Subtle>
                ) : (
                  <View style={styles.chipRow}>
                    {lists.map((list) => (
                      <AppChip
                        key={list.id}
                        label={list.name}
                        active={list.id === effectiveListId}
                        accentKey="meals"
                        onPress={() => onSelectListId(list.id)}
                      />
                    ))}
                  </View>
                )}
              </View>
            ) : null}
            {shoppingSyncError ? (
              <Text style={styles.error}>{strings.shoppingSyncFailed} {shoppingSyncError}</Text>
            ) : null}
            <View style={styles.sheetFooterActions}>
              <AppButton title={strings.saveMeal} onPress={onSave} fullWidth accentKey="meals" />
              <View style={styles.sheetFooterSecondaryActions}>
                {hasExistingMeal ? (
                  <AppButton
                    title={strings.removeMeal}
                    onPress={onRemove}
                    variant="ghost"
                  />
                ) : (
                  <View />
                )}
                <Pressable onPress={onClose} style={styles.footerCloseLink}>
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
  daySection: {
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
  mealTypeRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
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
  toggleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  toggleLabel: {
    fontWeight: '600',
    color: theme.colors.text,
    fontFamily: theme.typography.body,
  },
  lists: {
    gap: theme.spacing.xs,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
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
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  footerCloseLink: {
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.xs,
  },
  footerCloseText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
