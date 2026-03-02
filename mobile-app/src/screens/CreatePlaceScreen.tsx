import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { createGroup } from '../features/group/api/groupApi';
import { formatApiError } from '../shared/api/client';
import { useAuth } from '../shared/auth/AuthContext';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppInput, AppScreen, BackIconButton, TopBar } from '../shared/ui/components';
import { theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
  onCreated: (groupId: string) => Promise<void> | void;
};

export function CreatePlaceScreen({ token, onDone, onCreated }: Props) {
  const { handleApiError } = useAuth();
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pendingGroupId, setPendingGroupId] = useState<string | null>(null);
  const strings = {
    title: 'Create new place',
    subtitle: 'Give your new place a name.',
    placeholder: 'Place name',
    create: 'Create',
    creating: 'Creating...',
    retrySwitch: 'Try again',
  };

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
  });

  async function handleCreate() {
    const nextName = name.trim();
    if (!nextName || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    setPendingGroupId(null);
    try {
      const created = await createGroup(token, nextName);
      setPendingGroupId(created.groupId);
      try {
        await Promise.resolve(onCreated(created.groupId));
      } catch (activateErr) {
        await handleApiError(activateErr);
        setError(`Place created, but we could not switch yet. ${formatApiError(activateErr)}`);
      }
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleRetrySwitch() {
    if (!pendingGroupId || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await Promise.resolve(onCreated(pendingGroupId));
    } catch (err) {
      await handleApiError(err);
      setError(`Could not switch place yet. ${formatApiError(err)}`);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        <AppCard style={styles.card}>
          <AppInput
            value={name}
            onChangeText={setName}
            placeholder={strings.placeholder}
          />
          {error ? <Text style={styles.error}>{error}</Text> : null}
          <AppButton
            title={loading ? strings.creating : strings.create}
            onPress={handleCreate}
            disabled={loading || name.trim().length === 0}
            fullWidth
          />
          {pendingGroupId && error ? (
            <AppButton
              title={strings.retrySwitch}
              onPress={handleRetrySwitch}
              disabled={loading}
              variant="ghost"
              fullWidth
            />
          ) : null}
        </AppCard>
      </View>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  card: {
    gap: theme.spacing.md,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
