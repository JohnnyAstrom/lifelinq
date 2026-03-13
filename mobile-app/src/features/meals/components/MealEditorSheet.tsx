import { Ionicons } from '@expo/vector-icons';
import { Keyboard, Pressable, ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  MEAL_INGREDIENT_UNIT_OPTIONS,
  type MealIngredientRow,
} from '../utils/ingredientRows';

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
  ingredientNamePlaceholder: string;
  quantityPlaceholder: string;
  addIngredient: string;
  removeIngredient: string;
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
  onAddIngredientRow: () => void;
  onRemoveIngredientRow: (rowId: string) => void;
  onChangeIngredientName: (rowId: string, value: string) => void;
  onChangeIngredientQuantity: (rowId: string, value: string) => void;
  onToggleIngredientUnit: (rowId: string, value: typeof MEAL_INGREDIENT_UNIT_OPTIONS[number]['value']) => void;
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
  onAddIngredientRow,
  onRemoveIngredientRow,
  onChangeIngredientName,
  onChangeIngredientQuantity,
  onToggleIngredientUnit,
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
                  title={strings.addIngredient}
                  onPress={onAddIngredientRow}
                  variant="ghost"
                />
              </View>
              {isRecipeLoading ? (
                <Subtle>{strings.loadingIngredients}</Subtle>
              ) : null}
              <View style={styles.ingredientRows}>
                {ingredientRows.map((row, index) => (
                  <View key={row.id} style={styles.ingredientRowCard}>
                    <View style={styles.ingredientRowHeader}>
                      <Text style={styles.ingredientRowIndex}>{index + 1}</Text>
                      <Pressable
                        onPress={() => onRemoveIngredientRow(row.id)}
                        style={styles.ingredientRowRemove}
                        accessibilityRole="button"
                        accessibilityLabel={`${strings.removeIngredient} ${index + 1}`}
                      >
                        <Ionicons
                          name="close-outline"
                          size={16}
                          color={theme.colors.textSecondary}
                        />
                        <Text style={styles.ingredientRowRemoveText}>{strings.removeIngredient}</Text>
                      </Pressable>
                    </View>
                    <View style={styles.ingredientMainRow}>
                      <AppInput
                        placeholder={strings.ingredientNamePlaceholder}
                        value={row.name}
                        onChangeText={(value) => onChangeIngredientName(row.id, value)}
                        style={styles.ingredientNameInput}
                      />
                      <AppInput
                        placeholder={strings.quantityPlaceholder}
                        value={row.quantityText}
                        onChangeText={(value) => onChangeIngredientQuantity(row.id, value)}
                        keyboardType="decimal-pad"
                        style={styles.quantityInput}
                      />
                    </View>
                    {row.quantityText.length > 0 ? (
                      <View style={styles.unitChipRow}>
                        {MEAL_INGREDIENT_UNIT_OPTIONS.map((option) => (
                          <AppChip
                            key={option.value}
                            label={option.label}
                            active={row.unit === option.value}
                            accentKey="meals"
                            onPress={() => onToggleIngredientUnit(row.id, option.value)}
                          />
                        ))}
                      </View>
                    ) : null}
                  </View>
                ))}
              </View>
            </View>
            <View style={styles.toggleRow}>
              <Text style={styles.toggleLabel}>{strings.addIngredientsToShopping}</Text>
              <Switch value={pushToShopping} onValueChange={onChangePushToShopping} />
            </View>
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
            {shoppingSyncError ? (
              <Text style={styles.error}>{strings.shoppingSyncFailed} {shoppingSyncError}</Text>
            ) : null}
            <View style={styles.sheetFooterActions}>
              <AppButton title={strings.saveMeal} onPress={onSave} fullWidth accentKey="meals" />
              {hasExistingMeal ? (
                <AppButton
                  title={strings.removeMeal}
                  onPress={onRemove}
                  variant="ghost"
                  fullWidth
                />
              ) : null}
              <AppButton
                title={strings.close}
                onPress={onClose}
                variant="secondary"
                fullWidth
              />
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
  ingredientRows: {
    gap: theme.spacing.sm,
  },
  ingredientRowCard: {
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surface,
  },
  ingredientRowHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  ingredientRowIndex: {
    ...textStyles.subtle,
    fontWeight: '700',
    color: theme.colors.textSecondary,
  },
  ingredientRowRemove: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingVertical: 2,
  },
  ingredientRowRemoveText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  ingredientMainRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: theme.spacing.sm,
  },
  ingredientNameInput: {
    flex: 1,
  },
  quantityInput: {
    width: 104,
  },
  unitChipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    paddingTop: 2,
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
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
