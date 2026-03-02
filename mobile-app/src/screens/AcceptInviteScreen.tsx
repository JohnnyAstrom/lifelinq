import { useEffect, useRef, useState } from 'react';
import { Text, View } from 'react-native';
import { AppButton, AppCard, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';
import { acceptInvitation } from '../features/group/api/groupApi';

type Props = {
  token: string;
  inviteToken: string | null;
  onAccepted: () => Promise<void>;
  onBackHome: () => void;
};

export function AcceptInviteScreen({
  token,
  inviteToken,
  onAccepted,
  onBackHome,
}: Props) {
  const [state, setState] = useState<'loading' | 'error'>('loading');
  const startedRef = useRef(false);

  useEffect(() => {
    if (startedRef.current) {
      return;
    }
    startedRef.current = true;

    let cancelled = false;
    async function run() {
      const normalizedToken = inviteToken?.trim() ?? '';
      if (!normalizedToken) {
        setState('error');
        return;
      }

      try {
        await acceptInvitation(token, normalizedToken);
        if (cancelled) {
          return;
        }
        await onAccepted();
      } catch {
        if (!cancelled) {
          setState('error');
        }
      }
    }

    void run();
    return () => {
      cancelled = true;
    };
  }, [inviteToken, onAccepted, token]);

  if (state === 'loading') {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard style={{ gap: theme.spacing.sm }}>
          <Text style={textStyles.h3}>Joining place</Text>
          <Subtle>Please wait a moment.</Subtle>
        </AppCard>
      </AppScreen>
    );
  }

  return (
    <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
      <AppCard style={{ gap: theme.spacing.md }}>
        <Text style={textStyles.h3}>Unable to join</Text>
        <Subtle>This invitation is invalid or expired.</Subtle>
        <View style={{ paddingTop: theme.spacing.xs }}>
          <AppButton title="Back to Home" onPress={onBackHome} fullWidth />
        </View>
      </AppCard>
    </AppScreen>
  );
}
