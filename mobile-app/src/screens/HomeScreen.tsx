import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { AppButton, AppCard, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onCreateTodo: () => void;
  onManageMembers: () => void;
  onCreateShopping: () => void;
  onMeals: () => void;
  onLogout: () => void;
};

export function HomeScreen({
  token,
  me,
  onCreateTodo,
  onManageMembers,
  onCreateShopping,
  onMeals,
  onLogout,
}: Props) {
  const strings = {
    appName: 'LifeLinq',
    tagline: 'Everything your household needs, in one place.',
    userLabel: 'User',
    householdLabel: 'Household',
    notLinked: 'Not linked',
    todosTitle: 'Todos',
    todosSubtitle: 'Keep the day on track',
    mealsTitle: 'Meals',
    mealsSubtitle: 'Plan breakfast, lunch, dinner',
    shoppingTitle: 'Shopping',
    shoppingSubtitle: 'Sync lists instantly',
    membersTitle: 'Members',
    membersSubtitle: 'Invite the household',
    quickActions: 'Quick actions',
    openMeals: 'Open meals',
    logout: 'Log out',
  };
  return (
    <AppScreen>
      <AppCard style={styles.headerCard}>
        <Text style={textStyles.h1}>{strings.appName}</Text>
        <Subtle>{strings.tagline}</Subtle>
        <View style={styles.meta}>
          <Text style={styles.metaLabel}>{strings.userLabel}</Text>
          <Text style={styles.metaValue}>{me.userId}</Text>
        </View>
        <View style={styles.meta}>
          <Text style={styles.metaLabel}>{strings.householdLabel}</Text>
          <Text style={styles.metaValue}>{me.householdId || strings.notLinked}</Text>
        </View>
      </AppCard>

      <View style={styles.grid}>
        <ActionTile title={strings.todosTitle} subtitle={strings.todosSubtitle} onPress={onCreateTodo} />
        <ActionTile title={strings.mealsTitle} subtitle={strings.mealsSubtitle} onPress={onMeals} />
        <ActionTile title={strings.shoppingTitle} subtitle={strings.shoppingSubtitle} onPress={onCreateShopping} />
        <ActionTile title={strings.membersTitle} subtitle={strings.membersSubtitle} onPress={onManageMembers} />
      </View>

      <AppCard>
        <Text style={textStyles.h3}>{strings.quickActions}</Text>
        <View style={styles.quickRow}>
          <AppButton title={strings.openMeals} onPress={onMeals} fullWidth />
          <AppButton title={strings.logout} onPress={onLogout} variant="ghost" fullWidth />
        </View>
      </AppCard>
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
  headerCard: {
    gap: theme.spacing.sm,
  },
  meta: {
    marginTop: theme.spacing.xs,
  },
  metaLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  metaValue: {
    ...textStyles.body,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.md,
  },
  tile: {
    flexBasis: '48%',
    backgroundColor: theme.colors.surface,
    borderRadius: theme.radius.lg,
    padding: theme.spacing.lg,
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
  quickRow: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
});
