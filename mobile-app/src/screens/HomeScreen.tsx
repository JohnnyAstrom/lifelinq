import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { setActiveGroup, type MeMembership } from '../features/auth/api/meApi';
import { useTodos } from '../features/todo/hooks/useTodos';
import { useGroupMembers } from '../features/group/hooks/useGroupMembers';
import { formatApiError } from '../shared/api/client';
import { useAuth } from '../shared/auth/AuthContext';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../shared/ui/OverlaySheet';
import { AppCard, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  spaceName?: string;
  memberships: MeMembership[];
  activeGroupId: string | null;
  onSwitchedGroup: () => void;
  onCreateTodo: () => void;
  onCreateShopping: () => void;
  onMeals: () => void;
  onDocuments: () => void;
  onSettings: () => void;
};

export function HomeScreen({
  token,
  spaceName,
  memberships,
  activeGroupId,
  onSwitchedGroup,
  onCreateTodo,
  onCreateShopping,
  onMeals,
  onDocuments,
  onSettings,
}: Props) {
  const todos = useTodos(token, 'OPEN', undefined, {
    onContextInvalidated: onSwitchedGroup,
  });
  const members = useGroupMembers(token);
  const { handleApiError } = useAuth();
  const [showSwitchSheet, setShowSwitchSheet] = useState(false);
  const [switchingGroupId, setSwitchingGroupId] = useState<string | null>(null);
  const [switchError, setSwitchError] = useState<string | null>(null);
  const strings = {
    unnamedSpace: 'My space',
    createNewPlace: 'Create new place',
    todosTitle: 'Todos',
    todosSubtitle: 'Keep the day on track',
    mealsTitle: 'Meals',
    mealsSubtitle: 'Plan breakfast, lunch, dinner',
    shoppingTitle: 'Shopping',
    shoppingSubtitle: 'Sync lists instantly',
    documentsTitle: 'Documents',
    documentsSubtitle: 'Store receipts and records',
    todayTitle: 'Today',
    todaySubtitle: 'Open todos for today.',
    todayPlaceholderTitle: 'No items yet',
    todayPlaceholderBody: 'Add a dated todo and it will show up here.',
    sectionTitle: 'Home',
    settingsLabel: 'Settings',
    todayTodoCount: 'items due today',
  };

  const todayItems = todos.items.filter((item) => {
    if (!item.dueDate) {
      return false;
    }
    const due = parseApiDate(item.dueDate);
    return due ? isSameDay(due, new Date()) : false;
  });
  const showMemberSubtitle = members.items.length > 0 || !members.loading;
  const memberSubtitle = members.items.length <= 1
    ? 'Only you'
    : `${members.items.length} members`;

  useAppBackHandler({
    isOverlayOpen: showSwitchSheet,
    onCloseOverlay: () => setShowSwitchSheet(false),
  });

  async function handleSwitch(groupId: string) {
    if (switchingGroupId || groupId === activeGroupId) {
      setShowSwitchSheet(false);
      return;
    }
    setSwitchError(null);
    setSwitchingGroupId(groupId);
    try {
      await setActiveGroup(token, groupId);
      await Promise.resolve(onSwitchedGroup());
      setShowSwitchSheet(false);
    } catch (err) {
      await handleApiError(err);
      setSwitchError(formatApiError(err));
    } finally {
      setSwitchingGroupId(null);
    }
  }

  return (
    <AppScreen scroll={false}>
      <View style={styles.header}>
        <Pressable
          onPress={() => setShowSwitchSheet(true)}
          accessibilityRole="button"
          style={({ pressed }) => [styles.headerTextBlock, pressed ? styles.spaceNamePressed : null]}
        >
          <View>
            <Text style={styles.headerTitle}>{`${spaceName || strings.unnamedSpace} ▾`}</Text>
          </View>
          {showMemberSubtitle ? <Subtle style={styles.headerSubtitle}>{memberSubtitle}</Subtle> : null}
        </Pressable>
        <Pressable
          onPress={onSettings}
          accessibilityRole="button"
          accessibilityLabel={strings.settingsLabel}
          style={({ pressed }) => [
            styles.settingsIconButton,
            pressed ? styles.settingsIconButtonPressed : null,
          ]}
        >
          <Ionicons name="settings-outline" size={27} color={theme.colors.text} />
        </Pressable>
      </View>

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

      {showSwitchSheet ? (
        <OverlaySheet onClose={() => setShowSwitchSheet(false)} sheetStyle={styles.sheet}>
          <View style={styles.sheetContent}>
            <Subtle>Switch place</Subtle>
            {switchError ? <Text style={styles.error}>{switchError}</Text> : null}
            <View style={styles.switchList}>
              {memberships.map((membership) => {
                const isActive = membership.groupId === activeGroupId;
                const isSwitching = switchingGroupId === membership.groupId;
                const label = membership.groupName?.trim() || strings.unnamedSpace;
                return (
                  <Pressable
                    key={membership.groupId}
                    onPress={() => {
                      void handleSwitch(membership.groupId);
                    }}
                    disabled={!!switchingGroupId}
                    style={({ pressed }) => [
                      styles.switchRow,
                      isActive ? styles.switchRowActive : null,
                      pressed ? styles.switchRowPressed : null,
                    ]}
                  >
                    <Text style={styles.switchRowLabel}>{label}</Text>
                    {isActive ? <Ionicons name="checkmark" size={18} color={theme.colors.text} /> : null}
                    {isSwitching ? <Subtle>…</Subtle> : null}
                  </Pressable>
                );
              })}
            </View>
            <Pressable style={styles.createPlaceRow} disabled>
              <Subtle>{strings.createNewPlace}</Subtle>
            </Pressable>
          </View>
        </OverlaySheet>
      ) : null}
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
  header: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    paddingTop: theme.spacing.md,
    paddingBottom: theme.spacing.sm,
    paddingHorizontal: theme.spacing.lg,
    backgroundColor: theme.colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    zIndex: 5,
  },
  headerTitle: {
    ...textStyles.h2,
    flex: 1,
  },
  headerTextBlock: {
    flex: 1,
    gap: 2,
  },
  headerSubtitle: {
    fontSize: 13,
  },
  spaceNamePressed: {
    opacity: 0.8,
  },
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
  settingsIconButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  settingsIconButtonPressed: {
    opacity: 0.75,
  },
  sheet: {
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.xl + theme.spacing.md,
  },
  sheetContent: {
    gap: theme.spacing.md,
  },
  switchList: {
    gap: theme.spacing.xs,
  },
  switchRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  switchRowActive: {
    borderColor: theme.colors.borderStrong,
  },
  switchRowPressed: {
    opacity: 0.85,
  },
  switchRowLabel: {
    ...textStyles.body,
    flex: 1,
  },
  createPlaceRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
    opacity: 0.6,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
