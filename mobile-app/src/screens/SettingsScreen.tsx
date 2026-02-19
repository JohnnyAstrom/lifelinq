import { StyleSheet } from 'react-native';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
  onManageMembers: () => void;
  onLogout: () => void;
};

export function SettingsScreen({ onDone, onManageMembers, onLogout }: Props) {
  const strings = {
    title: 'Settings',
    subtitle: 'Manage household access and preferences.',
    householdTitle: 'Household',
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
        <SectionTitle>{strings.householdTitle}</SectionTitle>
        <Subtle>Invite and manage who can access this household.</Subtle>
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
