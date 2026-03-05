import { useEffect, useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { getActiveInvitationLink, renameCurrentPlace } from '../features/group/api/groupApi';
import { useGroupMembers } from '../features/group/hooks/useGroupMembers';
import { formatApiError } from '../shared/api/client';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppInput, AppScreen, BackIconButton, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
  onPlaceRenamed: () => Promise<void> | void;
  onManageMembers: () => void;
  onManageInvitations: () => void;
};

type ActiveInviteSummary = {
  expiresAt: string;
};

export function ManagePlaceScreen({
  token,
  me,
  onDone,
  onPlaceRenamed,
  onManageMembers,
  onManageInvitations,
}: Props) {
  const members = useGroupMembers(token);
  const renameInputRef = useRef<TextInput>(null);
  const [isEditingName, setIsEditingName] = useState(false);
  const [nameDraft, setNameDraft] = useState('');
  const [renameSaving, setRenameSaving] = useState(false);
  const [renameError, setRenameError] = useState<string | null>(null);
  const [activeInvite, setActiveInvite] = useState<ActiveInviteSummary | null>(null);
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState<string | null>(null);

  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentRole = activeMembership?.role ?? 'MEMBER';
  const currentPlaceName = activeMembership?.groupName?.trim() || 'My place';
  const activeGroupId = activeMembership?.groupId ?? null;
  const isAdmin = currentRole === 'ADMIN';

  const memberNames = members.items
    .map((member) => member.displayName?.trim() ?? '')
    .filter((name) => name.length > 0);

  const strings = {
    title: 'Manage place',
    membersTitle: 'Members',
    loadingMembers: 'Loading members...',
    membersCount: (count: number) => `${count} member${count === 1 ? '' : 's'}`,
    manageMembers: 'Manage members',
    invitationsTitle: 'Invitations',
    inviteCount: (count: number) => `${count} active invite${count === 1 ? '' : 's'}`,
    noActiveInvite: 'No active invites',
    manageInvitations: 'Manage invitations',
    save: 'Save',
    saving: 'Saving...',
    renameValidationError: 'Name cannot be empty.',
    tapToRename: 'Tap to rename',
  };

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: false,
    onCloseOverlay: () => {},
  });

  useEffect(() => {
    if (!isEditingName) {
      return;
    }
    const cursorIndex = currentPlaceName.length;
    const timer = setTimeout(() => {
      renameInputRef.current?.focus();
      renameInputRef.current?.setNativeProps({
        selection: { start: cursorIndex, end: cursorIndex },
      });
    }, 0);
    return () => clearTimeout(timer);
  }, [isEditingName, currentPlaceName]);

  useEffect(() => {
    if (!isAdmin || !activeGroupId) {
      setActiveInvite(null);
      setInviteError(null);
      return;
    }
    void loadActiveInvite();
  }, [isAdmin, activeGroupId, token]);

  async function loadActiveInvite() {
    if (inviteLoading || !activeGroupId) {
      return;
    }
    setInviteError(null);
    setInviteLoading(true);
    try {
      const invitation = await getActiveInvitationLink(token, activeGroupId);
      if (!invitation) {
        setActiveInvite(null);
        return;
      }
      setActiveInvite({
        expiresAt: invitation.expiresAt,
      });
    } catch (err) {
      setInviteError(formatApiError(err));
      setActiveInvite(null);
    } finally {
      setInviteLoading(false);
    }
  }

  function daysUntilExpiry(value: string): number | null {
    const expiresAt = new Date(value).getTime();
    if (Number.isNaN(expiresAt)) {
      return null;
    }
    const diffMs = expiresAt - Date.now();
    if (diffMs <= 0) {
      return 0;
    }
    return Math.ceil(diffMs / (1000 * 60 * 60 * 24));
  }

  function getExpiryLabel(value: string): string {
    const days = daysUntilExpiry(value);
    if (days == null) {
      return 'Expires soon';
    }
    if (days === 0) {
      return 'Expires today';
    }
    if (days === 1) {
      return 'Expires in 1 day';
    }
    return `Expires in ${days} days`;
  }

  function startRename() {
    if (renameSaving) {
      return;
    }
    setNameDraft(currentPlaceName);
    setRenameError(null);
    setIsEditingName(true);
  }

  function cancelRename() {
    if (renameSaving) {
      return;
    }
    setRenameError(null);
    setIsEditingName(false);
    setNameDraft(currentPlaceName);
  }

  async function handleSaveRename() {
    if (renameSaving) {
      return;
    }
    const nextName = nameDraft.trim();
    if (!nextName) {
      setRenameError(strings.renameValidationError);
      return;
    }
    if (nextName === currentPlaceName) {
      setRenameError(null);
      setIsEditingName(false);
      return;
    }
    setRenameSaving(true);
    setRenameError(null);
    try {
      await renameCurrentPlace(token, nextName);
      await Promise.resolve(onPlaceRenamed());
      setIsEditingName(false);
    } catch (err) {
      setRenameError(formatApiError(err));
    } finally {
      setRenameSaving(false);
    }
  }

  return (
    <AppScreen>
      <TopBar title={strings.title} right={<BackIconButton onPress={onDone} />} />

      <View style={styles.contentOffset}>
        <Pressable
          onPress={startRename}
          disabled={isEditingName || renameSaving}
          style={({ pressed }) => [pressed ? styles.placeCardPressed : null]}
          accessibilityRole="button"
        >
          <AppCard>
            <View style={styles.sectionContent}>
              {isEditingName ? (
                <View style={styles.renameEditor}>
                  <AppInput
                    ref={renameInputRef}
                    value={nameDraft}
                    onChangeText={setNameDraft}
                    placeholder="Place name"
                    autoFocus
                  />
                  {renameError ? <Text style={styles.error}>{renameError}</Text> : null}
                  <View style={styles.renameActions}>
                    <AppButton
                      title={renameSaving ? strings.saving : strings.save}
                      onPress={() => {
                        void handleSaveRename();
                      }}
                      disabled={renameSaving || nameDraft.trim().length === 0}
                      fullWidth
                    />
                    <AppButton
                      title="Cancel"
                      onPress={cancelRename}
                      variant="ghost"
                      disabled={renameSaving}
                      fullWidth
                    />
                  </View>
                </View>
              ) : (
                <>
                  <Text style={styles.placeName}>{currentPlaceName}</Text>
                  <Subtle>{strings.tapToRename}</Subtle>
                </>
              )}
            </View>
          </AppCard>
        </Pressable>

        <AppCard>
          <View style={styles.sectionContent}>
            <Text style={styles.sectionTitle}>{strings.membersTitle}</Text>
            <Subtle>{strings.membersCount(memberNames.length)}</Subtle>
            {members.loading ? <Subtle>{strings.loadingMembers}</Subtle> : null}
            {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
            <View style={styles.membersList}>
              {memberNames.slice(0, 3).map((name) => (
                <Text key={name} style={styles.memberName}>{name}</Text>
              ))}
            </View>
            <AppButton title={strings.manageMembers} onPress={onManageMembers} variant="ghost" fullWidth />
          </View>
        </AppCard>

        <AppCard>
          <View style={styles.sectionContent}>
            <Text style={styles.sectionTitle}>{strings.invitationsTitle}</Text>
            {inviteLoading ? <Subtle>Loading invitations...</Subtle> : null}
            {inviteError ? <Text style={styles.error}>{inviteError}</Text> : null}
            {!inviteLoading && !inviteError ? (
              activeInvite ? (
                <>
                  <Subtle>{strings.inviteCount(1)}</Subtle>
                  <Subtle>{getExpiryLabel(activeInvite.expiresAt)}</Subtle>
                </>
              ) : (
                <>
                  <Subtle>{strings.inviteCount(0)}</Subtle>
                  <Subtle>{strings.noActiveInvite}</Subtle>
                </>
              )
            ) : null}
            <AppButton title={strings.manageInvitations} onPress={onManageInvitations} variant="ghost" fullWidth />
          </View>
        </AppCard>
      </View>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.xxl,
  },
  sectionContent: {
    gap: theme.spacing.md,
    paddingVertical: theme.spacing.xs,
  },
  placeName: {
    ...textStyles.h3,
    flex: 1,
  },
  sectionTitle: {
    ...textStyles.h3,
    fontWeight: '700',
    marginBottom: theme.spacing.xs,
  },
  placeCardPressed: {
    opacity: 0.88,
  },
  renameEditor: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  renameActions: {
    gap: theme.spacing.sm,
  },
  membersList: {
    marginTop: theme.spacing.xs,
    marginBottom: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  memberName: {
    ...textStyles.body,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
