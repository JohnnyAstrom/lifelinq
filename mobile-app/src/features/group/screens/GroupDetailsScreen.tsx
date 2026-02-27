import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../../auth/api/meApi';
import { useGroupMembers } from '../hooks/useGroupMembers';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppCard, AppInput, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
};

export function GroupDetailsScreen({ token, me, onDone }: Props) {
  const members = useGroupMembers(token);
  const [showInviteSheet, setShowInviteSheet] = useState(false);
  const [showAddExistingUserForm, setShowAddExistingUserForm] = useState(false);
  const [inviteUserId, setInviteUserId] = useState('');
  const [isSubmittingInvite, setIsSubmittingInvite] = useState(false);
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentRole = activeMembership?.role ?? 'MEMBER';
  const currentGroupName = activeMembership?.groupName ?? 'Unknown group';
  const isAdmin = currentRole === 'ADMIN';
  const strings = {
    identityTitle: 'Space',
    membersTitle: 'Members',
    collaborationTitle: 'Collaboration',
    loadingMembers: 'Loading members...',
    inviteMember: 'Invite someone',
    inviteTitle: 'Invite someone',
    addExistingUser: 'Add existing user',
    lifeLinqIdLabel: 'LifeLinq ID',
    lifeLinqIdPlaceholder: 'Enter LifeLinq ID',
    add: 'Add',
    close: 'Close',
    back: 'Back',
    unnamedSpace: 'My space',
  };
  const memberNames = members.items
    .map((member) => member.displayName?.trim() ?? '')
    .filter((name) => name.length > 0);

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: showInviteSheet,
    onCloseOverlay: () => {
      setShowInviteSheet(false);
      setShowAddExistingUserForm(false);
      setInviteUserId('');
    },
  });

  async function handleAddExistingUser() {
    const userId = inviteUserId.trim();
    if (!userId || isSubmittingInvite) {
      return;
    }
    setIsSubmittingInvite(true);
    try {
      await members.add(userId);
      await members.reload();
      setShowInviteSheet(false);
      setShowAddExistingUserForm(false);
      setInviteUserId('');
    } finally {
      setIsSubmittingInvite(false);
    }
  }

  return (
    <AppScreen>
      <TopBar
        title={currentGroupName || strings.unnamedSpace}
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        <AppCard>
          <SectionTitle>{strings.identityTitle}</SectionTitle>
          <Text style={styles.spaceName}>{currentGroupName || strings.unnamedSpace}</Text>
        </AppCard>

        {members.items.length > 1 ? (
          <AppCard>
            <SectionTitle>{strings.membersTitle}</SectionTitle>
            {members.loading ? <Subtle>{strings.loadingMembers}</Subtle> : null}
            {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
            <View style={styles.list}>
              {memberNames.map((name) => (
                <View key={name} style={styles.memberRow}>
                  <Text style={styles.memberName}>{name}</Text>
                </View>
              ))}
            </View>
          </AppCard>
        ) : null}

        <AppCard>
          <SectionTitle>{strings.collaborationTitle}</SectionTitle>
          {isAdmin ? (
            <AppButton
              title={strings.inviteMember}
              onPress={() => {
                setShowInviteSheet(true);
                setShowAddExistingUserForm(false);
                setInviteUserId('');
              }}
              fullWidth
            />
          ) : null}
        </AppCard>
      </View>

      {showInviteSheet ? (
        <OverlaySheet
          onClose={() => {
            setShowInviteSheet(false);
            setShowAddExistingUserForm(false);
            setInviteUserId('');
          }}
          sheetStyle={styles.sheet}
        >
          <View style={styles.sheetContent}>
            <SectionTitle>{strings.inviteTitle}</SectionTitle>
            {!showAddExistingUserForm ? (
              <AppButton
                title={strings.addExistingUser}
                onPress={() => setShowAddExistingUserForm(true)}
                fullWidth
              />
            ) : (
              <View style={styles.addUserForm}>
                <Subtle>{strings.lifeLinqIdLabel}</Subtle>
                <AppInput
                  value={inviteUserId}
                  onChangeText={setInviteUserId}
                  placeholder={strings.lifeLinqIdPlaceholder}
                />
                <AppButton
                  title={isSubmittingInvite ? `${strings.add}...` : strings.add}
                  onPress={() => void handleAddExistingUser()}
                  disabled={isSubmittingInvite || inviteUserId.trim().length === 0}
                  fullWidth
                />
                {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
              </View>
            )}
            <AppButton
              title={strings.close}
              onPress={() => {
                setShowInviteSheet(false);
                setShowAddExistingUserForm(false);
                setInviteUserId('');
              }}
              variant="ghost"
              fullWidth
            />
          </View>
        </OverlaySheet>
      ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  list: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  spaceName: {
    ...textStyles.body,
    marginTop: theme.spacing.sm,
  },
  memberRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    padding: theme.spacing.sm,
    borderRadius: theme.radius.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceAlt,
  },
  memberName: {
    ...textStyles.body,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  sheet: {
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.lg,
  },
  sheetContent: {
    gap: theme.spacing.md,
  },
  addUserForm: {
    gap: theme.spacing.sm,
  },
});
