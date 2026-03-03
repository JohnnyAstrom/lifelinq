import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import { devLogin } from '../api/devLoginApi';
import { formatApiError } from '../../../shared/api/client';
import { AppButton, AppCard, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  onLoggedIn: (token: string) => Promise<void> | void;
  authError?: string | null;
  onClearAuthError?: () => void;
};

export function LoginScreen({ onLoggedIn, authError = null, onClearAuthError }: Props) {
  const insets = useSafeAreaInsets();
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
      await Promise.resolve(onLoggedIn(response.token));
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  const bottomInset = Math.max(insets.bottom, theme.spacing.md);

  return (
    <SafeAreaView style={styles.screen} edges={['top', 'bottom']}>
      <KeyboardAvoidingView
        style={styles.keyboardContainer}
        behavior={Platform.OS === 'ios' ? 'padding' : 'position'}
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
          </AppCard>
        </ScrollView>

        <View style={[styles.ctaContainer, { paddingBottom: bottomInset }]}>
          <AppButton
            title={loading ? strings.loggingIn : strings.login}
            onPress={handleLogin}
            disabled={!email.trim() || loading}
            fullWidth
          />
        </View>
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
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.xl,
    paddingBottom: 140,
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
  ctaContainer: {
    position: 'absolute',
    left: theme.spacing.lg,
    right: theme.spacing.lg,
    bottom: 0,
    paddingTop: theme.spacing.sm,
    backgroundColor: theme.colors.bg,
  },
});
