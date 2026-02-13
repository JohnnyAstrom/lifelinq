import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { AppButton, AppCard, AppScreen, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onCreateTodo: () => void;
  onCreateShopping: () => void;
  onMeals: () => void;
  onDocuments: () => void;
  onSettings: () => void;
  onLogout: () => void;
};

export function HomeScreen({
  token,
  me,
  onCreateTodo,
  onCreateShopping,
  onMeals,
  onDocuments,
  onSettings,
  onLogout,
}: Props) {
  const strings = {
    appName: 'LifeLinq',
    tagline: 'Your household, in sync.',
    userLabel: 'User',
    householdLabel: 'Household',
    notLinked: 'Not linked',
    todosTitle: 'Todos',
    todosSubtitle: 'Keep the day on track',
    mealsTitle: 'Meals',
    mealsSubtitle: 'Plan breakfast, lunch, dinner',
    shoppingTitle: 'Shopping',
    shoppingSubtitle: 'Sync lists instantly',
    documentsTitle: 'Documents',
    documentsSubtitle: 'Store receipts and records',
    quickActions: 'Quick actions',
    todayTitle: 'Today',
    todaySubtitle: 'Your plan for today will appear here.',
    todayPlaceholderTitle: 'No items yet',
    todayPlaceholderBody: 'Once you add meals or todos, they will show up here.',
    sectionTitle: 'Home',
    settings: 'Settings',
  };
  return (
    <AppScreen scroll={false}>
      <TopBar
        title={strings.appName}
        subtitle={strings.tagline}
        right={<AppButton title={strings.settings} onPress={onSettings} variant="ghost" />}
      />

      <View style={styles.contentOffset}>
        <AppCard style={styles.primaryCard}>
          <Text style={styles.sectionTitle}>{strings.todayTitle}</Text>
          <Subtle>{strings.todaySubtitle}</Subtle>
          <View style={styles.todayPlaceholder}>
            <Text style={styles.todayPlaceholderTitle}>{strings.todayPlaceholderTitle}</Text>
            <Subtle>{strings.todayPlaceholderBody}</Subtle>
          </View>
        </AppCard>

        <AppCard>
          <Text style={textStyles.h3}>{strings.sectionTitle}</Text>
          <View style={styles.grid}>
            <ActionTile title={strings.mealsTitle} subtitle={strings.mealsSubtitle} onPress={onMeals} />
            <ActionTile title={strings.todosTitle} subtitle={strings.todosSubtitle} onPress={onCreateTodo} />
            <ActionTile title={strings.shoppingTitle} subtitle={strings.shoppingSubtitle} onPress={onCreateShopping} />
            <ActionTile title={strings.documentsTitle} subtitle={strings.documentsSubtitle} onPress={onDocuments} />
          </View>
        </AppCard>
      </View>
    </AppScreen>
  );
}

function ActionTile({
  title,
  subtitle,
  onPress,
}: {
  title: string;
  subtitle: string;
  onPress: () => void;
}) {
  return (
    <Pressable style={({ pressed }) => [styles.tile, pressed ? styles.tilePressed : null]} onPress={onPress}>
      <Text style={styles.tileTitle}>{title}</Text>
      <Text style={styles.tileSubtitle}>{subtitle}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
    flex: 1,
    justifyContent: 'flex-start',
  },
  primaryCard: {
    gap: theme.spacing.sm,
  },
  sectionTitle: {
    ...textStyles.h3,
  },
  todayPlaceholder: {
    marginTop: theme.spacing.sm,
    padding: theme.spacing.md,
    borderRadius: theme.radius.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceAlt,
    gap: theme.spacing.xs,
  },
  todayPlaceholderTitle: {
    ...textStyles.h3,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
    justifyContent: 'space-between',
  },
  tile: {
    width: '47%',
    backgroundColor: theme.colors.surface,
    borderRadius: theme.radius.lg,
    padding: theme.spacing.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  tilePressed: {
    opacity: 0.9,
    transform: [{ scale: 0.98 }],
  },
  tileTitle: {
    ...textStyles.h3,
    marginBottom: theme.spacing.xs,
  },
  tileSubtitle: {
    ...textStyles.subtle,
  },
});
