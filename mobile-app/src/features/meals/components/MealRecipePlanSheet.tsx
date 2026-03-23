import { useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type DayOption = {
  dayOfWeek: number;
  label: string;
  shortLabel: string;
  date: Date;
};

type ExistingMeal = {
  dayOfWeek: number;
  mealType: MealType;
  mealTitle: string;
};

type Strings = {
  eyebrow: string;
  title: string;
  subtitle: string;
  recipeLabel: string;
  weekLabel: string;
  dayLabel: string;
  mealLabel: string;
  slotOccupiedHint: (mealTitle: string) => string;
  planAction: string;
  replaceAction: string;
  planningAction: string;
  close: string;
};

type Props = {
  recipeTitle: string;
  weekSummary: string;
  days: DayOption[];
  mealTypeLabels: Record<MealType, string>;
  existingMeals: ExistingMeal[];
  defaultDayOfWeek: number;
  defaultMealType: MealType;
  isSubmitting: boolean;
  error: string | null;
  onConfirm: (selection: { dayOfWeek: number; mealType: MealType; date: Date }) => void;
  onClose: () => void;
  strings: Strings;
};

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER'];

export function MealRecipePlanSheet({
  recipeTitle,
  weekSummary,
  days,
  mealTypeLabels,
  existingMeals,
  defaultDayOfWeek,
  defaultMealType,
  isSubmitting,
  error,
  onConfirm,
  onClose,
  strings,
}: Props) {
  const [selectedDayOfWeek, setSelectedDayOfWeek] = useState(defaultDayOfWeek);
  const [selectedMealType, setSelectedMealType] = useState<MealType>(defaultMealType);

  const selectedDay = useMemo(
    () => days.find((day) => day.dayOfWeek === selectedDayOfWeek) ?? days[0] ?? null,
    [days, selectedDayOfWeek]
  );
  const selectedExistingMeal = useMemo(
    () => existingMeals.find((meal) => meal.dayOfWeek === selectedDayOfWeek && meal.mealType === selectedMealType) ?? null,
    [existingMeals, selectedDayOfWeek, selectedMealType]
  );

  function handleConfirm() {
    if (!selectedDay) {
      return;
    }
    onConfirm({
      dayOfWeek: selectedDay.dayOfWeek,
      mealType: selectedMealType,
      date: selectedDay.date,
    });
  }

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={styles.eyebrow}>{strings.eyebrow}</Text>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
        </View>

        <View style={styles.body}>
          <View style={styles.recipeSummaryCard}>
            <Text style={styles.summaryLabel}>{strings.recipeLabel}</Text>
            <Text style={styles.recipeTitle}>{recipeTitle}</Text>
            <Text style={styles.weekSummary}>
              {strings.weekLabel} {weekSummary}
            </Text>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionLabel}>{strings.dayLabel}</Text>
            <View style={styles.chipRow}>
              {days.map((day) => (
                <AppChip
                  key={`${day.dayOfWeek}-${day.shortLabel}`}
                  label={day.shortLabel}
                  active={day.dayOfWeek === selectedDayOfWeek}
                  accentKey="meals"
                  onPress={() => setSelectedDayOfWeek(day.dayOfWeek)}
                  style={styles.dayChip}
                />
              ))}
            </View>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionLabel}>{strings.mealLabel}</Text>
            <View style={styles.chipRow}>
              {MEAL_TYPES.map((mealType) => (
                <AppChip
                  key={mealType}
                  label={mealTypeLabels[mealType]}
                  active={mealType === selectedMealType}
                  accentKey="meals"
                  onPress={() => setSelectedMealType(mealType)}
                />
              ))}
            </View>
          </View>

          {selectedExistingMeal ? (
            <Text style={styles.slotHint}>
              {strings.slotOccupiedHint(selectedExistingMeal.mealTitle)}
            </Text>
          ) : null}

          {error ? <Text style={styles.error}>{error}</Text> : null}
        </View>

        <View style={styles.footer}>
          <AppButton
            title={isSubmitting
              ? strings.planningAction
              : selectedExistingMeal
                ? strings.replaceAction
                : strings.planAction}
            onPress={handleConfirm}
            fullWidth
            disabled={isSubmitting || !selectedDay}
          />
          <AppButton
            title={strings.close}
            onPress={onClose}
            variant="secondary"
            fullWidth
            disabled={isSubmitting}
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
    gap: theme.spacing.md,
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
    gap: theme.spacing.md,
  },
  recipeSummaryCard: {
    gap: theme.spacing.xs,
    padding: theme.spacing.md,
    borderRadius: theme.radius.lg,
    backgroundColor: theme.colors.surfaceSubtle,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  summaryLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  recipeTitle: {
    ...textStyles.body,
    fontWeight: '700',
    color: theme.colors.textPrimary,
  },
  weekSummary: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  section: {
    gap: theme.spacing.xs,
  },
  sectionLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  dayChip: {
    minWidth: 88,
  },
  slotHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  error: {
    ...textStyles.subtle,
    color: theme.colors.danger,
  },
  footer: {
    gap: theme.spacing.xs,
  },
});
