import { StyleSheet } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { GroupSwitcher } from '../features/group/components/GroupSwitcher';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onSwitchedGroup: () => void;
  onDone: () => void;
  onManageMembers: () => void;
  onLogout: () => void;
};

export function SettingsScreen({ token, me, onSwitchedGroup, onDone, onManageMembers, onLogout }: Props) {
  const strings = {
    title: 'Settings',
    subtitle: 'Manage group access and preferences.',
    groupTitle: 'Group',
    membersAction: 'Manage members',
    accountTitle: 'Account',
    logout: 'Log out',
    back: 'Back',
  };

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
  });

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
      />

      <AppCard style={styles.contentOffset}>
        <SectionTitle>{strings.groupTitle}</SectionTitle>
        <Subtle>Invite and manage who can access this group.</Subtle>
        {me.memberships.length > 1 ? (
          <GroupSwitcher token={token} me={me} onSwitched={onSwitchedGroup} />
        ) : null}
        <AppButton title={strings.membersAction} onPress={onManageMembers} fullWidth />
      </AppCard>

      <AppCard>
        <SectionTitle>{strings.accountTitle}</SectionTitle>
        <AppButton title={strings.logout} onPress={onLogout} variant="ghost" fullWidth />
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    marginTop: 90,
    gap: theme.spacing.sm,
  },
});
