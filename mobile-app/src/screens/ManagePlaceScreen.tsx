import { useEffect, useRef, useState } from 'react';
import { Pressable, Share, StyleSheet, Text, TextInput, View } from 'react-native';
import * as Clipboard from 'expo-clipboard';
import type { MeResponse } from '../features/auth/api/meApi';
import {
  createInvitationLink,
  deleteCurrentPlace,
  getActiveInvitationLink,
  leaveCurrentPlace,
  renameCurrentPlace,
  revokeInvitation,
} from '../features/group/api/groupApi';
import { WEB_BASE_URL } from '../shared/config/web';
import { useGroupMembers } from '../features/group/hooks/useGroupMembers';
import { formatApiError } from '../shared/api/client';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { ActionSheet } from '../shared/ui/ActionSheet';
import { AppButton, AppCard, AppInput, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
  onPlaceRenamed: () => Promise<void> | void;
  onPlaceLeft: (oldPlaceName: string) => Promise<void> | void;
  onPlaceDeleted: (oldPlaceName: string) => Promise<void> | void;
  onShowToast: (message: string) => void;
};

export function ManagePlaceScreen({
  token,
  me,
  onDone,
  onPlaceRenamed,
  onPlaceLeft,
  onPlaceDeleted,
  onShowToast,
}: Props) {
  type ActiveLinkInvite = {
    invitationId: string;
    token: string;
    shortCode: string;
    expiresAt: string;
  };

  const members = useGroupMembers(token);
  const renameInputRef = useRef<TextInput>(null);
  const [isEditingName, setIsEditingName] = useState(false);
  const [nameDraft, setNameDraft] = useState('');
  const [renameSaving, setRenameSaving] = useState(false);
  const [renameError, setRenameError] = useState<string | null>(null);
  const [leaveSheetOpen, setLeaveSheetOpen] = useState(false);
  const [leaveSubmitting, setLeaveSubmitting] = useState(false);
  const [leaveError, setLeaveError] = useState<string | null>(null);
  const [deleteSheetOpen, setDeleteSheetOpen] = useState(false);
  const [deleteSubmitting, setDeleteSubmitting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [activeInvite, setActiveInvite] = useState<ActiveLinkInvite | null>(null);
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [showInviteOptions, setShowInviteOptions] = useState(false);
  const [revokeInviteSheetOpen, setRevokeInviteSheetOpen] = useState(false);
  const [revokeInviteSubmitting, setRevokeInviteSubmitting] = useState(false);
  const [revokeInviteError, setRevokeInviteError] = useState<string | null>(null);
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentRole = activeMembership?.role ?? 'MEMBER';
  const currentPlaceName = activeMembership?.groupName?.trim() || 'My place';
  const activeGroupId = activeMembership?.groupId ?? null;
  const isAdmin = currentRole === 'ADMIN';
  const memberCount = members.items.length;
  const isDefaultPlace = activeMembership?.isDefault === true;
  const showGovernance = !isDefaultPlace && memberCount >= 1;
  const showLeave = !isDefaultPlace && memberCount > 1;
  const showDelete = !isDefaultPlace && (memberCount === 1 || (memberCount > 1 && isAdmin));
  const memberNames = members.items
    .map((member) => member.displayName?.trim() ?? '')
    .filter((name) => name.length > 0);

  const strings = {
    title: 'Manage place',
    membersTitle: 'Members',
    loadingMembers: 'Loading members...',
    inviteSomeone: 'Invite someone',
    inviteByLink: 'Invite by link',
    inviteByEmail: 'Invite by email',
    comingSoon: 'Coming soon',
    inviteOptionsTitle: 'Invite someone',
    invitationsTitle: 'Invitations',
    activeInviteTitle: 'Active invite link',
    inviteCodeManualTitle: 'Invite code (manual entry)',
    activeInviteExpiry: 'Expires',
    shareLink: 'Share link',
    copyCode: 'Copy code',
    revokeInvite: 'Revoke invite',
    loadingInvite: 'Loading invite...',
    noActiveInvite: 'No active invites yet.',
    revokeInviteTitle: 'Revoke this invite?',
    revokeInviteBody: 'Anyone with this code will lose access to join.',
    leavePlace: 'Leave place',
    deletePlace: 'Delete place',
    leaveTitle: 'Leave this place?',
    leaveBody: 'You will lose access to shared content.',
    deleteTitle: 'Delete this place?',
    deleteBody: 'This cannot be undone.',
    cancel: 'Cancel',
    deleteAction: 'Delete',
    save: 'Save',
    saving: 'Saving...',
    renameValidationError: 'Name cannot be empty.',
    tapToRename: 'Tap to rename',
    revoke: 'Revoke',
  };

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: leaveSheetOpen,
    onCloseOverlay: () => {
      if (leaveSubmitting) {
        return;
      }
      setLeaveSheetOpen(false);
      setLeaveError(null);
    },
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

  async function handleConfirmLeave() {
    if (leaveSubmitting) {
      return;
    }
    const oldPlaceName = currentPlaceName;
    setLeaveSubmitting(true);
    setLeaveError(null);
    try {
      await leaveCurrentPlace(token);
      await Promise.resolve(onPlaceLeft(oldPlaceName));
      setLeaveSheetOpen(false);
    } catch (err) {
      setLeaveError(formatApiError(err));
    } finally {
      setLeaveSubmitting(false);
    }
  }

  async function handleConfirmDelete() {
    if (deleteSubmitting) {
      return;
    }
    const oldPlaceName = currentPlaceName;
    setDeleteSubmitting(true);
    setDeleteError(null);
    try {
      await deleteCurrentPlace(token);
      await Promise.resolve(onPlaceDeleted(oldPlaceName));
      setDeleteSheetOpen(false);
    } catch (err) {
      setDeleteError(formatApiError(err));
    } finally {
      setDeleteSubmitting(false);
    }
  }

  async function loadActiveInvite() {
    if (inviteLoading) {
      return;
    }
    if (!activeGroupId) {
      setActiveInvite(null);
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
        invitationId: invitation.invitationId,
        token: invitation.token,
        shortCode: invitation.shortCode?.trim() || invitation.token.slice(0, 6).toUpperCase(),
        expiresAt: invitation.expiresAt,
      });
    } catch (err) {
      setInviteError(formatApiError(err));
      setActiveInvite(null);
    } finally {
      setInviteLoading(false);
    }
  }

  async function handleCreateInvite() {
    if (inviteLoading) {
      return;
    }
    setInviteError(null);
    setInviteLoading(true);
    try {
      const invitation = await createInvitationLink(token);
      setActiveInvite({
        invitationId: invitation.invitationId,
        token: invitation.token,
        shortCode: invitation.shortCode?.trim() || invitation.token.slice(0, 6).toUpperCase(),
        expiresAt: invitation.expiresAt,
      });
    } catch (err) {
      setInviteError(formatApiError(err));
    } finally {
      setInviteLoading(false);
    }
  }

  function openInviteOptions() {
    if (inviteLoading) {
      return;
    }
    setShowInviteOptions(true);
  }

  function handleInviteByLink() {
    setShowInviteOptions(false);
    void handleCreateInvite();
  }

  function getInviteUrl(invitationToken: string): string {
    return `${WEB_BASE_URL}/invite/${invitationToken}`;
  }

  async function handleShareInviteLink() {
    if (!activeInvite) {
      return;
    }
    const inviteUrl = getInviteUrl(activeInvite.token);
    await Share.share({
      message: inviteUrl,
      url: inviteUrl,
    });
  }

  async function handleCopyInviteCode() {
    if (!activeInvite) {
      return;
    }
    await Clipboard.setStringAsync(activeInvite.shortCode);
    onShowToast('Code copied.');
  }

  async function handleConfirmRevokeInvite() {
    if (!activeInvite || revokeInviteSubmitting) {
      return;
    }
    setRevokeInviteSubmitting(true);
    setRevokeInviteError(null);
    try {
      await revokeInvitation(token, activeInvite.invitationId);
      await loadActiveInvite();
      setRevokeInviteSheetOpen(false);
      setRevokeInviteError(null);
      setInviteError(null);
      onShowToast('Invite revoked.');
    } catch (err) {
      setRevokeInviteError(formatApiError(err));
    } finally {
      setRevokeInviteSubmitting(false);
    }
  }

  function formatInviteExpiry(value: string): string {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    return parsed.toLocaleString();
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
      <TopBar
        title={strings.title}
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        <Pressable
          onPress={startRename}
          disabled={isEditingName || renameSaving}
          style={({ pressed }) => [pressed ? styles.placeCardPressed : null]}
          accessibilityRole="button"
        >
          <AppCard>
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
                    title={strings.cancel}
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
          </AppCard>
        </Pressable>

        <AppCard>
          <SectionTitle>{strings.membersTitle}</SectionTitle>
          {members.loading ? <Subtle>{strings.loadingMembers}</Subtle> : null}
          {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
          <View style={styles.membersList}>
            {memberNames.map((name) => (
              <Text key={name} style={styles.memberName}>{name}</Text>
            ))}
          </View>
          {isAdmin ? (
            <View style={styles.activeInviteBlock}>
              <SectionTitle>{strings.invitationsTitle}</SectionTitle>
              <AppButton
                title={strings.inviteSomeone}
                onPress={() => {
                  openInviteOptions();
                }}
                fullWidth
              />
              {inviteLoading ? <Subtle>{strings.loadingInvite}</Subtle> : null}
              {inviteError ? <Text style={styles.error}>{inviteError}</Text> : null}
              {activeInvite ? (
                <>
                  <Text style={styles.activeInviteTitle}>{strings.activeInviteTitle}</Text>
                  <View style={styles.inviteActions}>
                    <AppButton
                      title={strings.shareLink}
                      onPress={() => {
                        void handleShareInviteLink();
                      }}
                      fullWidth
                    />
                  </View>
                  <Subtle>
                    {strings.activeInviteExpiry}: {formatInviteExpiry(activeInvite.expiresAt)}
                  </Subtle>
                  <Text style={styles.activeInviteTitle}>{strings.inviteCodeManualTitle}</Text>
                  <View style={styles.inviteCodeBox}>
                    <Text style={styles.inviteCodeText}>{activeInvite.shortCode}</Text>
                  </View>
                  <View style={styles.inviteActions}>
                    <AppButton
                      title={strings.copyCode}
                      onPress={() => {
                        void handleCopyInviteCode();
                      }}
                      variant="ghost"
                      fullWidth
                    />
                    <AppButton
                      title={strings.revokeInvite}
                      onPress={() => {
                        setRevokeInviteError(null);
                        setRevokeInviteSheetOpen(true);
                      }}
                      variant="ghost"
                      fullWidth
                    />
                  </View>
                </>
              ) : (
                <Subtle>{strings.noActiveInvite}</Subtle>
              )}
            </View>
          ) : null}
        </AppCard>

        {showGovernance ? (
          <AppCard>
            <SectionTitle>Governance</SectionTitle>
            {showLeave ? (
              <AppButton
                title={strings.leavePlace}
                onPress={() => {
                  setLeaveError(null);
                  setLeaveSheetOpen(true);
                }}
                variant="ghost"
                disabled={leaveSubmitting}
                fullWidth
              />
            ) : null}
            {showDelete ? (
              <AppButton
                title={strings.deletePlace}
                onPress={() => {
                  setDeleteError(null);
                  setDeleteSheetOpen(true);
                }}
                variant="ghost"
                disabled={deleteSubmitting}
                fullWidth
              />
            ) : null}
          </AppCard>
        ) : null}
      </View>
      <ActionSheet
        visible={leaveSheetOpen}
        onClose={() => {
          if (leaveSubmitting) {
            return;
          }
          setLeaveSheetOpen(false);
          setLeaveError(null);
        }}
        presentation="standard"
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>{strings.leaveTitle}</Text>
          <Subtle>{strings.leaveBody}</Subtle>
          {leaveError ? <Text style={styles.error}>{leaveError}</Text> : null}
          <View style={styles.confirmActions}>
            <AppButton
              title={strings.cancel}
              onPress={() => {
                setLeaveSheetOpen(false);
                setLeaveError(null);
              }}
              variant="ghost"
              disabled={leaveSubmitting}
              fullWidth
            />
            <AppButton
              title={strings.leavePlace}
              onPress={() => {
                void handleConfirmLeave();
              }}
              variant="secondary"
              disabled={leaveSubmitting}
              fullWidth
            />
          </View>
        </View>
      </ActionSheet>
      <ActionSheet
        visible={deleteSheetOpen}
        onClose={() => {
          if (deleteSubmitting) {
            return;
          }
          setDeleteSheetOpen(false);
          setDeleteError(null);
        }}
        presentation="standard"
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>{strings.deleteTitle}</Text>
          <Subtle>{strings.deleteBody}</Subtle>
          {deleteError ? <Text style={styles.error}>{deleteError}</Text> : null}
          <View style={styles.confirmActions}>
            <AppButton
              title={strings.cancel}
              onPress={() => {
                setDeleteSheetOpen(false);
                setDeleteError(null);
              }}
              variant="ghost"
              disabled={deleteSubmitting}
              fullWidth
            />
            <Pressable
              onPress={() => {
                void handleConfirmDelete();
              }}
              disabled={deleteSubmitting}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.deleteButton,
                pressed ? styles.deleteButtonPressed : null,
                deleteSubmitting ? styles.deleteButtonDisabled : null,
              ]}
            >
              <Text style={styles.deleteButtonText}>{strings.deleteAction}</Text>
            </Pressable>
          </View>
        </View>
      </ActionSheet>
      <ActionSheet
        visible={showInviteOptions}
        onClose={() => {
          setShowInviteOptions(false);
        }}
        presentation="standard"
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>{strings.inviteOptionsTitle}</Text>
          <View style={styles.confirmActions}>
            <AppButton
              title={strings.inviteByLink}
              onPress={handleInviteByLink}
              disabled={inviteLoading}
              fullWidth
            />
            <AppButton
              title={`${strings.inviteByEmail} (${strings.comingSoon})`}
              onPress={() => {}}
              disabled
              variant="ghost"
              fullWidth
            />
            <AppButton
              title={strings.cancel}
              onPress={() => {
                setShowInviteOptions(false);
              }}
              variant="ghost"
              disabled={inviteLoading}
              fullWidth
            />
          </View>
        </View>
      </ActionSheet>
      <ActionSheet
        visible={revokeInviteSheetOpen}
        onClose={() => {
          if (revokeInviteSubmitting) {
            return;
          }
          setRevokeInviteSheetOpen(false);
          setRevokeInviteError(null);
        }}
        presentation="standard"
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>{strings.revokeInviteTitle}</Text>
          <Subtle>{strings.revokeInviteBody}</Subtle>
          {revokeInviteError ? <Text style={styles.error}>{revokeInviteError}</Text> : null}
          <View style={styles.confirmActions}>
            <AppButton
              title={strings.cancel}
              onPress={() => {
                setRevokeInviteSheetOpen(false);
                setRevokeInviteError(null);
              }}
              variant="ghost"
              disabled={revokeInviteSubmitting}
              fullWidth
            />
            <AppButton
              title={strings.revoke}
              onPress={() => {
                void handleConfirmRevokeInvite();
              }}
              variant="secondary"
              disabled={revokeInviteSubmitting}
              fullWidth
            />
          </View>
        </View>
      </ActionSheet>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  placeNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  placeName: {
    ...textStyles.h3,
    flex: 1,
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
    marginTop: theme.spacing.sm,
    marginBottom: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  memberName: {
    ...textStyles.body,
  },
  activeInviteBlock: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  activeInviteTitle: {
    ...textStyles.subtle,
    marginTop: theme.spacing.xs,
  },
  inviteCodeBox: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
  },
  inviteCodeText: {
    ...textStyles.h3,
    letterSpacing: 1.2,
  },
  inviteActions: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  confirmContent: {
    gap: theme.spacing.md,
  },
  confirmActions: {
    gap: theme.spacing.sm,
  },
  deleteButton: {
    borderWidth: 1,
    borderColor: theme.colors.danger,
    borderRadius: 999,
    paddingVertical: 12,
    paddingHorizontal: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  deleteButtonPressed: {
    opacity: 0.85,
  },
  deleteButtonDisabled: {
    opacity: 0.5,
  },
  deleteButtonText: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});