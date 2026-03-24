import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type RecentMealOption = {
  id: string;
  mealTitle: string;
  contextLabel: string;
};

type RecentMealSection = {
  id: string;
  title: string;
  meals: RecentMealOption[];
};

type Strings = {
  title: string;
  hint?: string;
  loadingMeals: string;
  noMeals: string;
  close: string;
};

type Props = {
  sections: RecentMealSection[];
  isLoading: boolean;
  error: string | null;
  onSelectMeal: (mealId: string) => void;
  onClose: () => void;
  strings: Strings;
};

export function MealRecentMealsSheet({
  sections,
  isLoading,
  error,
  onSelectMeal,
  onClose,
  strings,
}: Props) {
  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
        </View>

        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            {isLoading ? <Subtle>{strings.loadingMeals}</Subtle> : null}
            {error ? <Text style={styles.error}>{error}</Text> : null}
            {!isLoading && !error && sections.length === 0 ? (
              <Subtle>{strings.noMeals}</Subtle>
            ) : null}

            {!isLoading && sections.length > 0 ? (
              <View style={styles.sectionList}>
                {sections.map((section) => (
                  <View key={section.id} style={styles.section}>
                    <Text style={styles.sectionTitle}>{section.title}</Text>
                    <View style={styles.mealList}>
                      {section.meals.map((meal) => (
                        <Pressable
                          key={meal.id}
                          onPress={() => onSelectMeal(meal.id)}
                          style={({ pressed }) => [
                            styles.mealRow,
                            pressed ? styles.mealRowPressed : null,
                          ]}
                        >
                          <View style={styles.mealRowCopy}>
                            <Text style={styles.mealTitle}>{meal.mealTitle}</Text>
                            <Text style={styles.mealMeta}>{meal.contextLabel}</Text>
                          </View>
                          <Ionicons name="chevron-forward" size={16} color={theme.colors.textSecondary} />
                        </Pressable>
                      ))}
                    </View>
                  </View>
                ))}
              </View>
            ) : null}

          </ScrollView>
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
  },
  header: {
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
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
  mealList: {
    gap: theme.spacing.xs,
  },
  sectionList: {
    gap: theme.spacing.md,
  },
  section: {
    gap: theme.spacing.xs,
  },
  sectionTitle: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  mealRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
  },
  mealRowPressed: {
    opacity: 0.8,
  },
  mealRowCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  mealTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  mealMeta: {
    ...textStyles.subtle,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  footer: {
    paddingTop: theme.spacing.xs,
  },
});
