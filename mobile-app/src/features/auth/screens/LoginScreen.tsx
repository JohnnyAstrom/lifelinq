import { useState } from 'react';
import { KeyboardAvoidingView, Linking, Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { devLogin } from '../api/devLoginApi';
import { startMagicLink } from '../api/magicLinkApi';
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
  const [loadingMagic, setLoadingMagic] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const strings = {
    title: 'LifeLinq',
    subtitle: 'Organize life together.',
    emailLabel: 'Email address',
    emailPlaceholder: 'you@lifelinq.dev',
    devLogin: 'Dev login',
    devLoggingIn: 'Logging in...',
    magicLogin: 'Send magic link',
    magicLoading: 'Sending...',
    google: 'Continue with Google',
    divider: 'OR',
    helper: "We'll send a secure login link to your email.",
    magicSent: 'Magic link sent. Check your email.',
  };

  function trimTrailingSlash(value: string): string {
    return value.replace(/\/+$/, '');
  }

  function resolveApiBaseUrl(): string {
    const configured = process.env.EXPO_PUBLIC_API_BASE_URL?.trim();
    if (configured) {
      return trimTrailingSlash(configured);
    }
    return 'http://localhost:8080';
  }

  async function handleGoogleLogin() {
    if (loading || loadingMagic) {
      return;
    }
    onClearAuthError?.();
    const url = `${resolveApiBaseUrl()}/oauth2/authorization/google`;
    await Linking.openURL(url);
  }

  async function handleDevLogin() {
    if (!email.trim() || loading || loadingMagic) {
      return;
    }
    setLoading(true);
    setError(null);
    setNotice(null);
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

  async function handleMagicLogin() {
    if (!email.trim() || loading || loadingMagic) {
      return;
    }
    setLoadingMagic(true);
    setError(null);
    setNotice(null);
    onClearAuthError?.();
    try {
      await startMagicLink(email.trim());
      setNotice(strings.magicSent);
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoadingMagic(false);
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
            <Text style={textStyles.h2}>{strings.title}</Text>
            <Subtle>{strings.subtitle}</Subtle>
          </View>

          <AppCard style={styles.card}>
            <View style={styles.providers}>
              <AppButton
                title={strings.google}
                onPress={() => {
                  void handleGoogleLogin();
                }}
                variant="ghost"
                fullWidth
              />
            </View>

            <View style={styles.dividerRow}>
              <View style={styles.dividerLine} />
              <Text style={styles.dividerText}>{strings.divider}</Text>
              <View style={styles.dividerLine} />
            </View>

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
                onSubmitEditing={handleDevLogin}
              />
            </View>
            {error || authError ? <Text style={styles.error}>{error ?? authError}</Text> : null}
            {notice ? <Text style={styles.notice}>{notice}</Text> : null}
            <View style={styles.providers}>
              <AppButton
                title={loading ? strings.devLoggingIn : strings.devLogin}
                onPress={handleDevLogin}
                disabled={!email.trim() || loading || loadingMagic}
                fullWidth
              />
              <View style={styles.dividerRow}>
                <View style={styles.dividerLine} />
                <Text style={styles.dividerText}>{strings.divider}</Text>
                <View style={styles.dividerLine} />
              </View>
              <AppButton
                title={loadingMagic ? strings.magicLoading : strings.magicLogin}
                onPress={handleMagicLogin}
                disabled={!email.trim() || loading || loadingMagic}
                fullWidth
              />
            </View>
            <Subtle>{strings.helper}</Subtle>
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
  card: {
    gap: theme.spacing.md,
  },
  dividerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: theme.colors.border,
  },
  dividerText: {
    ...textStyles.subtle,
    textTransform: 'lowercase',
  },
  field: {
    gap: theme.spacing.xs,
  },
  label: {
    ...textStyles.body,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  notice: {
    color: theme.colors.primary,
    fontFamily: theme.typography.body,
  },
  providers: {
    gap: theme.spacing.sm,
  },
});
