import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { devLogin } from '../api/devLoginApi';
import { formatApiError } from '../../../shared/api/client';
import { AppButton, AppCard, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  onLoggedIn: (accessToken: string, refreshToken?: string | null) => Promise<void> | void;
  authError?: string | null;
  onClearAuthError?: () => void;
};

export function LoginScreen({ onLoggedIn, authError = null, onClearAuthError }: Props) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const strings = {
    appName: 'LifeLinq',
    title: 'Sign in',
    subtitle: 'Use magic link to continue.',
    emailLabel: 'Email',
    emailPlaceholder: 'you@lifelinq.dev',
    login: 'Send magic link',
    loggingIn: 'Sending...',
    google: 'Continue with Google',
    apple: 'Continue with Apple',
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
      await Promise.resolve(onLoggedIn(response.accessToken, response.refreshToken));
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.screen} edges={['top', 'bottom']}>
      <KeyboardAvoidingView
        style={styles.keyboardContainer}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={Platform.OS === 'android' ? 12 : 0}
      >
        <View style={styles.decorOne} />
        <View style={styles.decorTwo} />

        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
        >
          <View style={styles.header}>
            <Text style={styles.logo}>{strings.appName}</Text>
            <Text style={textStyles.h2}>{strings.title}</Text>
            <Subtle>{strings.subtitle}</Subtle>
          </View>

          <AppCard style={styles.card}>
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
            <View style={styles.providers}>
              <AppButton
                title={strings.google}
                onPress={() => {}}
                variant="ghost"
                disabled
                fullWidth
              />
              <AppButton
                title={strings.apple}
                onPress={() => {}}
                variant="ghost"
                disabled
                fullWidth
              />
            </View>
            <View style={styles.primaryCta}>
              <AppButton
                title={loading ? strings.loggingIn : strings.login}
                onPress={handleLogin}
                disabled={!email.trim() || loading}
                fullWidth
              />
            </View>
          </AppCard>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: theme.colors.bg,
  },
  keyboardContainer: {
    flex: 1,
  },
  decorOne: {
    position: 'absolute',
    top: -120,
    right: -80,
    width: 220,
    height: 220,
    borderRadius: 110,
    backgroundColor: theme.colors.accentSoft,
    opacity: 0.5,
  },
  decorTwo: {
    position: 'absolute',
    bottom: -140,
    left: -90,
    width: 260,
    height: 260,
    borderRadius: 130,
    backgroundColor: theme.colors.primarySoft,
    opacity: 0.6,
  },
  scrollContent: {
    paddingTop: 48,
    paddingHorizontal: 20,
    paddingBottom: 24,
    gap: theme.spacing.lg,
  },
  header: {
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  logo: {
    ...textStyles.subtle,
    fontFamily: theme.typography.heading,
    letterSpacing: 0.4,
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
  providers: {
    gap: theme.spacing.sm,
  },
  primaryCta: {
    marginTop: theme.spacing.md,
  },
});
