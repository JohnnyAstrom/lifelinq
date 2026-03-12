import { Keyboard, ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

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
  ingredientsPlaceholder: string;
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
  ingredientsText: string;
  onChangeIngredientsText: (value: string) => void;
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
  ingredientsText,
  onChangeIngredientsText,
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
            <AppInput
              placeholder={strings.ingredientsPlaceholder}
              value={ingredientsText}
              onChangeText={onChangeIngredientsText}
              multiline
              style={styles.ingredientsInput}
            />
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
