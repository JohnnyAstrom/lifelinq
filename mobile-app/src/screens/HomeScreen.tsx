import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { useTodos } from '../features/todo/hooks/useTodos';
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
  const todos = useTodos(token, 'OPEN');
  const strings = {
    appName: 'LifeLinq',
    tagline: 'Your group, in sync.',
    userLabel: 'User',
    groupLabel: 'Group',
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
    todaySubtitle: 'Open todos for today.',
    todayPlaceholderTitle: 'No items yet',
    todayPlaceholderBody: 'Add a dated todo and it will show up here.',
    sectionTitle: 'Home',
    settings: 'Settings',
    todayTodoCount: 'items due today',
  };

  const todayItems = todos.items.filter((item) => {
    if (!item.dueDate) {
      return false;
    }
    const due = parseApiDate(item.dueDate);
    return due ? isSameDay(due, new Date()) : false;
  });

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
          {todayItems.length === 0 ? (
            <View style={styles.todayPlaceholder}>
              <Text style={styles.todayPlaceholderTitle}>{strings.todayPlaceholderTitle}</Text>
              <Subtle>{strings.todayPlaceholderBody}</Subtle>
            </View>
          ) : (
            <View style={styles.todayList}>
              <Subtle>
                {todayItems.length} {strings.todayTodoCount}
              </Subtle>
              {todayItems.slice(0, 3).map((item) => (
                <View key={item.id} style={styles.todayRow}>
                  <Text style={styles.todayRowText} numberOfLines={1}>{item.text}</Text>
                  <Text style={styles.todayRowMeta}>
                    {item.dueTime ? item.dueTime.slice(0, 5) : 'Any time'}
                  </Text>
                </View>
              ))}
            </View>
          )}
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

function parseApiDate(value?: string | null) {
  if (!value) {
    return null;
  }
  const [year, month, day] = value.split('-').map(Number);
  if (!year || !month || !day) {
    return null;
  }
  return new Date(year, month - 1, day);
}

function isSameDay(left: Date, right: Date) {
  return left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate();
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
  todayList: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  todayRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  todayRowText: {
    ...textStyles.body,
    flex: 1,
  },
  todayRowMeta: {
    ...textStyles.subtle,
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
