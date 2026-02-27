import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { GroupSwitcher } from '../features/group/components/GroupSwitcher';
import { useGroupMembers } from '../features/group/hooks/useGroupMembers';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppInput, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles } from '../shared/ui/theme';
import { theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onSwitchedGroup: () => void;
  onDone: () => void;
  onManageMembers: () => void;
  onLogout: () => void;
};

export function SettingsScreen({ token, me, onSwitchedGroup, onDone, onManageMembers: _onManageMembers, onLogout }: Props) {
  const members = useGroupMembers(token);
  const [isRenaming, setIsRenaming] = useState(false);
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const canInvite = activeMembership?.role === 'ADMIN';
  const currentSpaceName = activeMembership?.groupName ?? 'My space';
  const [draftSpaceName, setDraftSpaceName] = useState(currentSpaceName);

  useEffect(() => {
    setDraftSpaceName(currentSpaceName);
  }, [currentSpaceName]);

  const strings = {
    title: 'Settings',
    subtitle: 'Manage your account and app.',
    rename: 'Rename',
    save: 'Save',
    cancel: 'Cancel',
    membersTitle: 'Members',
    inviteSomeone: 'Invite someone',
    unknownMemberName: 'Someone',
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

      <View style={styles.contentOffset}>
        <View style={styles.section}>
          <Pressable
            style={styles.spaceNameRow}
            onPress={() => setIsRenaming((prev) => !prev)}
            accessibilityRole="button"
            accessibilityLabel={strings.rename}
          >
            <Text style={styles.spaceName}>{draftSpaceName.trim() || currentSpaceName}</Text>
            <Subtle style={styles.renameIcon}>✎</Subtle>
          </Pressable>

          {isRenaming ? (
            <View style={styles.renameEditor}>
              <AppInput
                value={draftSpaceName}
                onChangeText={setDraftSpaceName}
                placeholder={currentSpaceName}
              />
              <View style={styles.renameActions}>
                <AppButton
                  title={strings.save}
                  variant="ghost"
                  onPress={() => setIsRenaming(false)}
                />
                <AppButton
                  title={strings.cancel}
                  variant="ghost"
                  onPress={() => {
                    setDraftSpaceName(currentSpaceName);
                    setIsRenaming(false);
                  }}
                />
              </View>
            </View>
          ) : null}

          {members.items.length > 1 ? (
            <>
              <SectionTitle>{strings.membersTitle}</SectionTitle>
              <View style={styles.memberList}>
                {members.items.map((member) => (
                  <Text key={member.userId} style={styles.memberName}>
                    {member.displayName && member.displayName.trim().length > 0
                      ? member.displayName
                      : strings.unknownMemberName}
                  </Text>
                ))}
              </View>
            </>
          ) : null}

          {canInvite ? (
            <AppButton title={strings.inviteSomeone} onPress={() => {}} fullWidth />
          ) : null}
        </View>

        {me.memberships.length > 1 ? (
          <View style={styles.section}>
            <GroupSwitcher token={token} me={me} onSwitched={onSwitchedGroup} />
          </View>
        ) : null}

        <View style={styles.section}>
          <SectionTitle>{strings.accountTitle}</SectionTitle>
          <AppButton title={strings.logout} onPress={onLogout} variant="ghost" fullWidth />
        </View>
      </View>
    </AppScreen>
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
  spaceNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  spaceName: {
    ...textStyles.h2,
  },
  renameIcon: {
    lineHeight: 18,
  },
  renameEditor: {
    gap: theme.spacing.sm,
  },
  renameActions: {
    flexDirection: 'row',
    gap: theme.spacing.sm,
  },
  memberList: {
    gap: theme.spacing.xs,
  },
  memberName: {
    ...textStyles.body,
  },
});
