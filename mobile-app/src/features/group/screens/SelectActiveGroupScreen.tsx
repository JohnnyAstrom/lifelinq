import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { setActiveGroup } from '../../auth/api/meApi';
import type { MeMembership } from '../../auth/api/meApi';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { AppButton, AppCard, AppScreen, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  memberships: MeMembership[];
  onSelected: () => void;
};

export function SelectActiveGroupScreen({ token, memberships, onSelected }: Props) {
  const [loadingGroupId, setLoadingGroupId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { handleApiError } = useAuth();
  const strings = {
    title: 'Select active group',
    subtitle: 'Choose which group to use for scoped actions.',
    noGroups: 'No memberships available.',
    select: 'Select',
    selecting: 'Selecting...',
  };

  async function handleSelect(groupId: string) {
    if (loadingGroupId) {
      return;
    }
    setError(null);
    setLoadingGroupId(groupId);
    try {
      await setActiveGroup(token, groupId);
      await Promise.resolve(onSelected());
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoadingGroupId(null);
    }
  }

  return (
    <AppScreen contentStyle={styles.container}>
      <AppCard style={styles.card}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>

        {error ? <Text style={styles.error}>{error}</Text> : null}

        {memberships.length === 0 ? (
          <Subtle>{strings.noGroups}</Subtle>
        ) : (
          <View style={styles.list}>
            {memberships.map((membership) => {
              const isLoading = loadingGroupId === membership.groupId;
              return (
                <View key={membership.groupId} style={styles.row}>
                  <View style={styles.texts}>
                    <Text style={styles.groupId}>{membership.groupId}</Text>
                    <Subtle>{membership.role}</Subtle>
                  </View>
                  <AppButton
                    title={isLoading ? strings.selecting : strings.select}
                    onPress={() => handleSelect(membership.groupId)}
                    disabled={!!loadingGroupId}
                    variant="ghost"
                  />
                </View>
              );
            })}
          </View>
        )}
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: 'center',
  },
  card: {
    gap: theme.spacing.md,
  },
  list: {
    gap: theme.spacing.sm,
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
  texts: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  groupId: {
    ...textStyles.body,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
