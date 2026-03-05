import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { usePendingInvite } from '../../../shared/invite/PendingInviteContext';
import { AppButton, AppCard, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import { updateProfile } from '../api/meApi';

type Props = {
  token?: string;
  initialFirstName?: string | null;
  initialLastName?: string | null;
  initialPlaceName?: string | null;
  isInviteFlow?: boolean;
  inviteGroupName?: string | null;
  onSubmitProfile?: (
    firstName: string,
    lastName: string,
    initialPlaceName: string | null
  ) => Promise<void>;
  onCompleted: () => void;
};

export function CompleteProfileScreen({
  token,
  initialFirstName,
  initialLastName,
  initialPlaceName,
  isInviteFlow = false,
  inviteGroupName,
  onSubmitProfile,
  onCompleted,
}: Props) {
  const [firstName, setFirstName] = useState(initialFirstName ?? '');
  const [lastName, setLastName] = useState(initialLastName ?? '');
  const [placeName, setPlaceName] = useState(initialPlaceName ?? 'Personal');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { handleApiError } = useAuth();
  const { pendingInviteToken } = usePendingInvite();
  const effectiveIsInviteFlow = isInviteFlow || !!pendingInviteToken;
  const strings = {
    title: 'Complete profile',
    subtitle: 'Please add your first and last name to continue.',
    inviteSubtitle: 'You are joining:',
    placeNameLabel: 'What should we call your place?',
    save: 'Continue',
    saving: 'Saving...',
  };

  async function handleSubmit() {
    if (loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const normalizedPlaceName = effectiveIsInviteFlow
        ? null
        : (() => {
            const trimmedPlaceName = placeName.trim();
            return trimmedPlaceName.length > 0 ? trimmedPlaceName : null;
          })();
      if (onSubmitProfile) {
        await onSubmitProfile(firstName, lastName, normalizedPlaceName);
      } else {
        if (!token) {
          throw new Error('Missing token for profile update');
        }
        await updateProfile(token, firstName, lastName);
      }
      await Promise.resolve(onCompleted());
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView edges={['top', 'bottom']} style={styles.screen}>
      <KeyboardAvoidingView
        style={styles.keyboardContainer}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <View style={styles.decorOne} />
        <View style={styles.decorTwo} />
        <ScrollView
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
          contentInsetAdjustmentBehavior="always"
          contentContainerStyle={styles.scrollContent}
        >
          <View style={styles.header}>
            <Text style={textStyles.h2}>{strings.title}</Text>
            {effectiveIsInviteFlow ? (
              <>
                <Subtle>{strings.inviteSubtitle}</Subtle>
                {inviteGroupName ? <Text style={styles.joinGroupName}>{inviteGroupName}</Text> : null}
              </>
            ) : (
              <Subtle>{strings.subtitle}</Subtle>
            )}
          </View>

          <AppCard style={styles.card}>
            <View style={styles.fields}>
              <AppInput
                value={firstName}
                onChangeText={setFirstName}
                placeholder="First name"
                autoFocus
                returnKeyType="next"
              />
              <AppInput
                value={lastName}
                onChangeText={setLastName}
                placeholder="Last name"
                returnKeyType="next"
              />
              {!effectiveIsInviteFlow ? (
                <>
                  <Text style={textStyles.subtle}>{strings.placeNameLabel}</Text>
                  <AppInput
                    value={placeName}
                    onChangeText={setPlaceName}
                    placeholder="Personal"
                    returnKeyType="done"
                    onSubmitEditing={handleSubmit}
                  />
                </>
              ) : null}
            </View>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            <View style={styles.primaryCta}>
              <AppButton
                title={loading ? strings.saving : strings.save}
                onPress={handleSubmit}
                disabled={loading}
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
    gap: theme.spacing.xs,
  },
  joinGroupName: {
    ...textStyles.h3,
  },
  card: {
    gap: theme.spacing.md,
  },
  fields: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  primaryCta: {
    marginTop: theme.spacing.md,
  },
});
