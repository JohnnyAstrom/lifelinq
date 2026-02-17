import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { devLogin } from '../features/auth/api/devLoginApi';
import { formatApiError } from '../shared/api/client';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  onLoggedIn: (token: string) => void;
};

export function LoginScreen({ onLoggedIn }: Props) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const strings = {
    title: 'Welcome back',
    subtitle: 'Sign in with your dev email to continue.',
    emailLabel: 'Email',
    emailPlaceholder: 'you@lifelinq.dev',
    login: 'Login',
    loggingIn: 'Logging in...',
  };

  async function handleLogin() {
    if (!email.trim() || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await devLogin(email.trim());
      onLoggedIn(response.token);
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppScreen scroll={false} contentStyle={styles.container}>
      <AppCard style={styles.card}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
        <View style={styles.field}>
          <Text style={styles.label}>{strings.emailLabel}</Text>
          <AppInput
            value={email}
            placeholder={strings.emailPlaceholder}
            onChangeText={setEmail}
            autoFocus
          />
        </View>
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <AppButton
          title={loading ? strings.loggingIn : strings.login}
          onPress={handleLogin}
          disabled={!email.trim() || loading}
          fullWidth
        />
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  container: {
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
