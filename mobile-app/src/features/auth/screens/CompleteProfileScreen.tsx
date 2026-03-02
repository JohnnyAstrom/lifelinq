import { useState } from 'react';
import { Text, View } from 'react-native';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import { updateProfile } from '../api/meApi';

type Props = {
  token?: string;
  initialFirstName?: string | null;
  initialLastName?: string | null;
  initialPlaceName?: string | null;
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
  onSubmitProfile,
  onCompleted,
}: Props) {
  const [firstName, setFirstName] = useState(initialFirstName ?? '');
  const [lastName, setLastName] = useState(initialLastName ?? '');
  const [placeName, setPlaceName] = useState(initialPlaceName ?? 'Personal');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { handleApiError } = useAuth();
  const strings = {
    placeNameLabel: 'What should we call your place?',
  };

  async function handleSubmit() {
    if (loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const trimmedPlaceName = placeName.trim();
      const normalizedPlaceName = trimmedPlaceName.length > 0 ? trimmedPlaceName : null;
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
    <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
      <AppCard style={{ gap: theme.spacing.md }}>
        <Text style={textStyles.h2}>Complete profile</Text>
        <Subtle>Please add your first and last name to continue.</Subtle>

        <View style={{ gap: theme.spacing.sm }}>
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
          <Text style={textStyles.subtle}>{strings.placeNameLabel}</Text>
          <AppInput
            value={placeName}
            onChangeText={setPlaceName}
            placeholder="Personal"
            returnKeyType="done"
            onSubmitEditing={handleSubmit}
          />
        </View>

        {error ? <Text style={{ color: theme.colors.danger }}>{error}</Text> : null}

        <AppButton
          title={loading ? 'Saving...' : 'Save profile'}
          onPress={handleSubmit}
          disabled={loading}
          fullWidth
        />
      </AppCard>
    </AppScreen>
  );
}
