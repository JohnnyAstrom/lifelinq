import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../../auth/api/meApi';
import { setActiveGroup } from '../../auth/api/meApi';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { AppButton, AppCard, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onSwitched: () => void;
};

export function GroupSwitcher({ token, me, onSwitched }: Props) {
  const [switchingGroupId, setSwitchingGroupId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { handleApiError } = useAuth();
  const strings = {
    title: 'Active group',
    noneSelected: 'No active group selected',
    unknownGroup: 'Unknown group',
    availableGroups: 'Available groups',
    select: 'Switch',
    switching: 'Switching...',
  };

  if (me.memberships.length <= 1) {
    return null;
  }

  async function handleSwitch(groupId: string) {
    if (switchingGroupId) {
      return;
    }
    setError(null);
    setSwitchingGroupId(groupId);
    try {
      await setActiveGroup(token, groupId);
      await Promise.resolve(onSwitched());
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setSwitchingGroupId(null);
    }
  }

  const activeMembership = me.activeGroupId == null
    ? null
    : me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null;
  const activeGroupName = activeMembership?.groupName ?? strings.unknownGroup;

  return (
    <AppCard style={styles.card}>
      <Text style={textStyles.h3}>{strings.title}</Text>
      <Text style={styles.activeValue}>
        {me.activeGroupId ? activeGroupName : strings.noneSelected}
      </Text>
      <Subtle>{strings.availableGroups}</Subtle>

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <View style={styles.list}>
        {me.memberships.map((membership) => {
          const isActive = membership.groupId === me.activeGroupId;
          const isSwitching = switchingGroupId === membership.groupId;
          const label = membership.groupName?.trim() || strings.unknownGroup;
          return (
            <View key={membership.groupId} style={styles.row}>
              <View style={styles.rowTexts}>
                <Text style={styles.groupLabel}>{label}</Text>
                <Subtle>{membership.role}{isActive ? ' • Active' : ''}</Subtle>
              </View>
              <AppButton
                title={isSwitching ? strings.switching : strings.select}
                onPress={() => handleSwitch(membership.groupId)}
                disabled={!!switchingGroupId || isActive}
                variant="ghost"
              />
            </View>
          );
        })}
      </View>
    </AppCard>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: theme.spacing.sm,
  },
  activeValue: {
    ...textStyles.body,
  },
  list: {
    gap: theme.spacing.xs,
  },
  row: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    padding: theme.spacing.sm,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  rowTexts: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  groupLabel: {
    ...textStyles.body,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
