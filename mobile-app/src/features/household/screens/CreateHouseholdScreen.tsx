import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { createHousehold } from '../api/householdApi';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onCreated: () => void;
};

export function CreateHouseholdScreen({ token, onCreated }: Props) {
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { handleApiError } = useAuth();
  const strings = {
    title: 'Create household',
    subtitle: 'Give your household a name to get started.',
    nameLabel: 'Household name',
    namePlaceholder: 'The Nordics',
    create: 'Create',
    creating: 'Creating...',
  };

  async function handleCreate() {
    if (!name.trim() || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await createHousehold(token, name.trim());
      onCreated();
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppScreen contentStyle={styles.container}>
      <AppCard style={styles.card}>
        <Text style={textStyles.h1}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
        <View style={styles.field}>
          <Text style={styles.label}>{strings.nameLabel}</Text>
          <AppInput value={name} onChangeText={setName} placeholder={strings.namePlaceholder} />
        </View>
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <AppButton
          title={loading ? strings.creating : strings.create}
          onPress={handleCreate}
          fullWidth
          disabled={loading}
        />
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
  field: {
    gap: theme.spacing.xs,
  },
  label: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
