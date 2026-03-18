import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  recipeId: string;
  recipeTitle: string;
};

type Strings = {
  title: string;
  close: string;
  loadingDay: string;
  emptyDay?: string;
  addMeal: string;
  editMeal: string;
  mealsLabel: string;
  mealHint?: string;
  mealActionHint?: string;
  recipeLabel: string;
  openRecipe: string;
};

type Props = {
  title: string;
  subtitle: string | null;
  meals: MealEntry[];
  mealTypeLabels: Record<MealType, string>;
  isLoading: boolean;
  error: string | null;
  onOpenMeal: (mealType: MealType) => void;
  onOpenRecipe: (mealType: MealType) => void;
  onClose: () => void;
  strings: Strings;
};

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER'];

export function MealDayDetailSheet({
  title,
  subtitle,
  meals,
  mealTypeLabels,
  isLoading,
  error,
  onOpenMeal,
  onOpenRecipe,
  onClose,
  strings,
}: Props) {
  const mealsByType = new Map(meals.map((meal) => [meal.mealType, meal]));

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={styles.eyebrow}>{strings.title}</Text>
          <Text style={textStyles.h2}>{title}</Text>
          {subtitle ? <Subtle>{subtitle}</Subtle> : null}
        </View>

        <View style={styles.body}>
          {isLoading ? (
            <Subtle>{strings.loadingDay}</Subtle>
          ) : error ? (
            <Text style={styles.error}>{error}</Text>
          ) : (
            <View style={styles.slotList}>
              <Text style={styles.sectionLabel}>{strings.mealsLabel}</Text>
              {MEAL_TYPES.map((mealType) => {
                const meal = mealsByType.get(mealType) ?? null;
                return (
                  <View key={mealType} style={styles.slotRow}>
                    <Pressable
                      onPress={() => onOpenMeal(mealType)}
                      style={({ pressed }) => [
                        styles.slotMainPressable,
                        pressed ? styles.slotRowPressed : null,
                      ]}
                    >
                      <View style={styles.slotHeader}>
                        <Text style={styles.slotLabel}>{mealTypeLabels[mealType]}</Text>
                        <View style={styles.slotAction}>
                          <Text style={styles.slotActionText}>
                            {meal ? strings.editMeal : strings.addMeal}
                          </Text>
                          <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
                        </View>
                      </View>
                      <Text style={meal ? styles.slotValue : styles.slotEmptyValue}>
                        {meal ? meal.recipeTitle : `${strings.addMeal} ${mealTypeLabels[mealType].toLowerCase()}`}
                      </Text>
                      {(meal ? strings.mealHint : strings.mealActionHint) ? (
                        <Text style={styles.slotHint}>{meal ? strings.mealHint : strings.mealActionHint}</Text>
                      ) : null}
                    </Pressable>
                    {meal ? (
                      <View style={styles.recipeRow}>
                        <Text style={styles.recipeLabel}>{strings.recipeLabel}</Text>
                        <Pressable
                          onPress={() => onOpenRecipe(mealType)}
                          style={({ pressed }) => [
                            styles.recipeAction,
                            pressed ? styles.recipeActionPressed : null,
                          ]}
                        >
                          <Text style={styles.recipeActionText}>{strings.openRecipe}</Text>
                        </Pressable>
                      </View>
                    ) : null}
                  </View>
                );
              })}
              {meals.length === 0 && strings.emptyDay ? <Subtle>{strings.emptyDay}</Subtle> : null}
            </View>
          )}
        </View>

        <View style={styles.footer}>
          <AppButton title={strings.close} onPress={onClose} variant="ghost" fullWidth />
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
  eyebrow: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    fontWeight: '700',
  },
  body: {
    gap: theme.spacing.sm,
  },
  slotList: {
    gap: theme.spacing.xs,
  },
  sectionLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  slotRow: {
    gap: theme.spacing.xs,
    paddingVertical: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  slotMainPressable: {
    gap: 2,
  },
  slotRowPressed: {
    opacity: 0.72,
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
  },
  slotActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  slotAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  slotValue: {
    ...textStyles.body,
    fontWeight: '600',
  },
  slotEmptyValue: {
    ...textStyles.body,
    color: theme.colors.textSecondary,
  },
  slotHint: {
    ...textStyles.subtle,
  },
  recipeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    paddingTop: 2,
  },
  recipeLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  recipeAction: {
    paddingVertical: 2,
    paddingHorizontal: theme.spacing.xs,
    borderRadius: theme.radius.pill,
  },
  recipeActionPressed: {
    opacity: 0.72,
  },
  recipeActionText: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    fontWeight: '700',
  },
  footer: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
