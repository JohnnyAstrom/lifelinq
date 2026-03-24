import { Pressable, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  mealTitle: string;
  recipeId: string | null;
  recipeTitle: string | null;
  shoppingListName: string | null;
};

type Strings = {
  title: string;
  close: string;
  loadingDay: string;
  emptyDayTitle?: string;
  emptyDayHint?: string;
  addMeal: string;
  editMeal: string;
  planMealForSlot: (mealLabel: string) => string;
  emptySlotTitle: (mealLabel: string) => string;
  titleOnlyMealHint?: string;
  recipeBackedMealHint?: (mealTitle: string, recipeTitle: string) => string | null;
  openRecipe: string;
  openShopping: string;
  reviewShopping: string;
  addedToShopping: (listName?: string | null) => string;
};

type Props = {
  title: string;
  subtitle: string | null;
  meals: MealEntry[];
  mealTypeLabels: Record<MealType, string>;
  focusedMealType?: MealType | null;
  isLoading: boolean;
  error: string | null;
  onOpenMeal: (mealType: MealType) => void;
  onOpenRecipe: (mealType: MealType) => void;
  onOpenShopping: (mealType: MealType) => void;
  onClose: () => void;
  strings: Strings;
};

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER'];

export function MealDayDetailSheet({
  title,
  subtitle,
  meals,
  mealTypeLabels,
  focusedMealType = null,
  isLoading,
  error,
  onOpenMeal,
  onOpenRecipe,
  onOpenShopping,
  onClose,
  strings,
}: Props) {
  const mealsByType = new Map(meals.map((meal) => [meal.mealType, meal]));
  const plannedMealCount = meals.length;
  const isEmptyDay = !isLoading && !error && plannedMealCount === 0;

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{title}</Text>
          {subtitle ? <Subtle>{subtitle}</Subtle> : null}
        </View>

        <View style={styles.body}>
          {isLoading ? (
            <Subtle>{strings.loadingDay}</Subtle>
          ) : error ? (
            <Text style={styles.error}>{error}</Text>
          ) : (
            <View style={styles.overview}>
              {isEmptyDay && (strings.emptyDayTitle || strings.emptyDayHint) ? (
                <View style={styles.emptyDayIntro}>
                  {strings.emptyDayTitle ? (
                    <Text style={styles.emptyDayTitle}>{strings.emptyDayTitle}</Text>
                  ) : null}
                  {strings.emptyDayHint ? (
                    <Text style={styles.emptyDayHint}>{strings.emptyDayHint}</Text>
                  ) : null}
                </View>
              ) : null}
              <View style={styles.slotList}>
              {MEAL_TYPES.map((mealType) => {
                const meal = mealsByType.get(mealType) ?? null;
                const mealTypeLabel = mealTypeLabels[mealType];
                const isRecipeBacked = !!meal?.recipeId;
                const isTitleOnly = !!meal && !isRecipeBacked;
                const slotHint = meal
                  ? (isRecipeBacked
                    ? strings.recipeBackedMealHint?.(meal.mealTitle, meal.recipeTitle ?? '')
                    : strings.titleOnlyMealHint ?? null)
                  : null;
                const shoppingActionLabel = meal?.shoppingListName
                  ? strings.reviewShopping
                  : strings.openShopping;
                return (
                  <View
                    key={mealType}
                    style={[
                      styles.slotRow,
                      focusedMealType === mealType ? styles.slotRowFocused : null,
                      isTitleOnly ? styles.slotRowQuiet : null,
                      !meal ? styles.slotRowEmpty : null,
                    ]}
                  >
                    <View style={styles.slotHeader}>
                      <Text style={styles.slotLabel}>{mealTypeLabel}</Text>
                      {meal?.shoppingListName ? (
                        <Text style={styles.slotMeta}>{strings.addedToShopping(meal.shoppingListName)}</Text>
                      ) : null}
                    </View>
                    <Text
                      style={
                        meal
                          ? styles.slotValue
                          : styles.slotEmptyValue
                      }
                    >
                      {meal ? meal.mealTitle : strings.emptySlotTitle(mealTypeLabel)}
                    </Text>
                    {slotHint ? <Text style={styles.slotHint}>{slotHint}</Text> : null}
                    <View style={styles.slotActionsRow}>
                      {meal ? (
                        <>
                          {isRecipeBacked ? (
                            <Pressable
                              onPress={() => onOpenRecipe(mealType)}
                              style={({ pressed }) => [
                                styles.primaryActionButton,
                                pressed ? styles.actionPressed : null,
                              ]}
                            >
                              <Text style={styles.primaryActionText}>{strings.openRecipe}</Text>
                            </Pressable>
                          ) : null}
                          <Pressable
                            onPress={() => onOpenMeal(mealType)}
                            style={({ pressed }) => [
                              styles.secondaryActionButton,
                              pressed ? styles.actionPressed : null,
                            ]}
                          >
                            <Text style={styles.secondaryActionText}>{strings.editMeal}</Text>
                          </Pressable>
                          {isRecipeBacked ? (
                            <Pressable
                              onPress={() => onOpenShopping(mealType)}
                              style={({ pressed }) => [
                                styles.secondaryActionButton,
                                pressed ? styles.actionPressed : null,
                              ]}
                            >
                              <Text style={styles.secondaryActionText}>{shoppingActionLabel}</Text>
                            </Pressable>
                          ) : null}
                        </>
                      ) : (
                        <Pressable
                          onPress={() => onOpenMeal(mealType)}
                          style={({ pressed }) => [
                            styles.secondaryActionButton,
                            pressed ? styles.actionPressed : null,
                          ]}
                        >
                          <Text style={styles.secondaryActionText}>{strings.planMealForSlot(mealTypeLabel)}</Text>
                        </Pressable>
                      )}
                    </View>
                  </View>
                );
              })}
              </View>
            </View>
          )}
        </View>

        <View style={styles.footer}>
          <AppButton
            title={strings.close}
            onPress={onClose}
            variant="secondary"
            fullWidth
          />
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
    gap: theme.spacing.sm,
  },
  header: {
    gap: theme.spacing.xs,
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  body: {
    gap: theme.spacing.sm,
  },
  overview: {
    gap: theme.spacing.sm,
  },
  emptyDayIntro: {
    gap: 4,
    paddingHorizontal: theme.spacing.xs,
    paddingTop: 2,
  },
  emptyDayTitle: {
    ...textStyles.body,
    color: theme.colors.text,
    fontWeight: '700',
  },
  emptyDayHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  slotList: {
    gap: theme.spacing.xs,
  },
  slotRow: {
    gap: theme.spacing.xs,
    paddingVertical: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
  },
  slotRowFocused: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderColor: theme.colors.border,
  },
  slotRowQuiet: {
    gap: 4,
    paddingVertical: 10,
  },
  slotRowEmpty: {
    backgroundColor: theme.colors.surfaceAlt,
    gap: 4,
    paddingVertical: 10,
  },
  slotHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  slotLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    color: theme.colors.textSecondary,
    fontWeight: '700',
  },
  slotMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  slotValue: {
    ...textStyles.body,
    color: theme.colors.text,
    fontSize: 18,
    lineHeight: 25,
    fontWeight: '700',
  },
  slotEmptyValue: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  slotHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  slotActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  primaryActionButton: {
    minHeight: 34,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 6,
    borderRadius: theme.radius.pill,
    backgroundColor: theme.colors.feature.meals,
    alignItems: 'center',
    justifyContent: 'center',
  },
  secondaryActionButton: {
    minHeight: 34,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 6,
    borderRadius: theme.radius.pill,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  actionPressed: {
    opacity: 0.72,
  },
  primaryActionText: {
    ...textStyles.subtle,
    color: theme.colors.surface,
    fontWeight: '700',
  },
  secondaryActionText: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '700',
  },
  footer: {
    paddingTop: theme.spacing.xs,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
