import { useState } from 'react';
import { KeyboardAvoidingView, Platform, StyleSheet, Text, View } from 'react-native';
import { devLogin } from '../api/devLoginApi';
import { formatApiError } from '../../../shared/api/client';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  onLoggedIn: (token: string) => Promise<void> | void;
  authError?: string | null;
  onClearAuthError?: () => void;
};

export function LoginScreen({ onLoggedIn, authError = null, onClearAuthError }: Props) {
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
    onClearAuthError?.();
    try {
      const response = await devLogin(email.trim());
      await Promise.resolve(onLoggedIn(response.token));
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppScreen scroll={false} contentStyle={styles.container}>
      <KeyboardAvoidingView
        style={styles.keyboardArea}
        behavior={Platform.OS === 'ios' ? 'padding' : 'position'}
        keyboardVerticalOffset={Platform.OS === 'android' ? 24 : 0}
      >
        <AppCard style={styles.card}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
          <View style={styles.field}>
            <Text style={styles.label}>{strings.emailLabel}</Text>
            <AppInput
              value={email}
              placeholder={strings.emailPlaceholder}
              onChangeText={(value) => {
                if (authError) {
                  onClearAuthError?.();
                }
                setEmail(value);
              }}
              autoFocus
              returnKeyType="done"
              onSubmitEditing={handleLogin}
            />
          </View>
          {error || authError ? <Text style={styles.error}>{error ?? authError}</Text> : null}
          <AppButton
            title={loading ? strings.loggingIn : strings.login}
            onPress={handleLogin}
            disabled={!email.trim() || loading}
            fullWidth
          />
        </AppCard>
      </KeyboardAvoidingView>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
  },
  keyboardArea: {
    width: '100%',
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
