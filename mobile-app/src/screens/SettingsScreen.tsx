import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles } from '../shared/ui/theme';
import { theme } from '../shared/ui/theme';

type Props = {
  me: MeResponse;
  onDone: () => void;
  onManagePlace: () => void;
  onSwitchPlace: () => void;
  onLogout: () => void;
};

export function SettingsScreen({ me, onDone, onManagePlace, onSwitchPlace, onLogout }: Props) {
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentPlaceName = activeMembership?.groupName?.trim() || 'My place';

  const strings = {
    title: 'Settings',
    subtitle: 'Your account and app.',
    accountTitle: 'Account',
    profile: 'Profile',
    notifications: 'Notifications',
    privacy: 'Privacy',
    currentPlaceTitle: 'Current place',
    switchPlace: 'Switch place',
    createNewPlace: 'Create new place',
    appTitle: 'App',
    appearance: 'Appearance',
    accessibility: 'Accessibility',
    about: 'About',
    version: 'Version',
    versionValue: 'v0.1.0',
    logout: 'Log out',
    logoutTitle: 'Log out?',
    logoutMessage: 'Do you want to log out now?',
    cancel: 'Cancel',
    confirmLogout: 'Log out',
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
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        <View style={styles.section}>
          <SectionTitle>{strings.accountTitle}</SectionTitle>
          <AppCard>
            <SettingRow label={strings.profile} />
            <SettingRow label={strings.notifications} disabled />
            <SettingRow label={strings.privacy} disabled />
          </AppCard>
        </View>

        <View style={styles.section}>
          <SectionTitle>{strings.currentPlaceTitle}</SectionTitle>
          <AppCard>
            <SettingRow label={currentPlaceName} onPress={onManagePlace} />
            {me.memberships.length > 1 ? (
              <SettingRow label={strings.switchPlace} onPress={onSwitchPlace} />
            ) : null}
            <SettingRow label={strings.createNewPlace} disabled />
          </AppCard>
        </View>

        <View style={styles.section}>
          <SectionTitle>{strings.appTitle}</SectionTitle>
          <AppCard>
            <SettingRow label={strings.appearance} disabled />
            <SettingRow label={strings.accessibility} disabled />
            <SettingRow label={strings.about} />
            <View style={styles.versionRow}>
              <Text style={styles.rowLabel}>{strings.version}</Text>
              <Subtle>{strings.versionValue}</Subtle>
            </View>
          </AppCard>
        </View>

        <View style={styles.logoutSection}>
          <AppButton
            title={strings.logout}
            onPress={() => {
              Alert.alert(strings.logoutTitle, strings.logoutMessage, [
                { text: strings.cancel, style: 'cancel' },
                { text: strings.confirmLogout, style: 'destructive', onPress: onLogout },
              ]);
            }}
            variant="ghost"
            fullWidth
          />
        </View>
      </View>
    </AppScreen>
  );
}

function SettingRow({ label, onPress, disabled }: { label: string; onPress?: () => void; disabled?: boolean }) {
  return (
    <Pressable
      onPress={onPress}
      disabled={disabled || !onPress}
      style={({ pressed }) => [
        styles.settingRow,
        (disabled || !onPress) ? styles.settingRowDisabled : null,
        pressed ? styles.settingRowPressed : null,
      ]}
      accessibilityRole="button"
    >
      <Text style={styles.rowLabel}>{label}</Text>
      {onPress && !disabled ? <Text style={styles.chevron}>→</Text> : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  section: {
    gap: theme.spacing.sm,
  },
  settingRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  settingRowDisabled: {
    opacity: 0.5,
  },
  settingRowPressed: {
    opacity: 0.8,
  },
  rowLabel: {
    ...textStyles.body,
  },
  versionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: theme.spacing.sm,
  },
  logoutSection: {
    marginTop: theme.spacing.lg,
  },
  chevron: {
    ...textStyles.body,
  },
});
