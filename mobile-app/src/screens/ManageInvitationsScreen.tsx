import { useEffect, useState } from 'react';
import { Keyboard, Pressable, Share, StyleSheet, Text, View } from 'react-native';
import * as Clipboard from 'expo-clipboard';
import type { MeResponse } from '../features/auth/api/meApi';
import {
  createInvitationByEmail,
  createInvitationLink,
  getActiveInvitationLink,
  listInvitations,
  revokeInvitation,
} from '../features/group/api/groupApi';
import { WEB_BASE_URL } from '../shared/config/web';
import { formatApiError } from '../shared/api/client';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { ActionSheet } from '../shared/ui/ActionSheet';
import { AppButton, AppCard, AppInput, AppScreen, BackIconButton, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
  onShowToast: (message: string) => void;
};

type ActiveLinkInvite = {
  invitationId: string;
  token: string;
  shortCode: string;
  expiresAt: string;
};

type ActiveEmailInvite = {
  invitationId: string;
  inviteeEmail: string;
  shortCode: string;
  expiresAt: string;
};

export function ManageInvitationsScreen({ token, me, onDone, onShowToast }: Props) {
  const [activeInvite, setActiveInvite] = useState<ActiveLinkInvite | null>(null);
  const [activeEmailInvites, setActiveEmailInvites] = useState<ActiveEmailInvite[]>([]);
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [emailInviteSheetOpen, setEmailInviteSheetOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [emailInviteInputFocused, setEmailInviteInputFocused] = useState(false);
  const [emailInviteSubmitting, setEmailInviteSubmitting] = useState(false);
  const [emailInviteError, setEmailInviteError] = useState<string | null>(null);
  const [emailInvitesExpanded, setEmailInvitesExpanded] = useState(false);
  const [revokeInviteSheetOpen, setRevokeInviteSheetOpen] = useState(false);
  const [revokeInviteSubmitting, setRevokeInviteSubmitting] = useState(false);
  const [revokeInviteError, setRevokeInviteError] = useState<string | null>(null);
  const [revokeTarget, setRevokeTarget] = useState<{ invitationId: string; label: string } | null>(null);

  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const activeGroupId = activeMembership?.groupId ?? null;
  const isAdmin = activeMembership?.role === 'ADMIN';

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: emailInviteSheetOpen || revokeInviteSheetOpen,
    onCloseOverlay: () => {
      if (revokeInviteSubmitting || emailInviteSubmitting) {
        return;
      }
      if (emailInviteSheetOpen) {
        closeEmailInviteSheet();
      }
      if (revokeInviteSheetOpen) {
        setRevokeInviteSheetOpen(false);
        setRevokeInviteError(null);
        setRevokeTarget(null);
      }
    },
  });

  useEffect(() => {
    if (!isAdmin || !activeGroupId) {
      setActiveInvite(null);
      setActiveEmailInvites([]);
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
      const [linkInvitation, invitations] = await Promise.all([
        getActiveInvitationLink(token, activeGroupId),
        listInvitations(token, activeGroupId, 'ACTIVE'),
      ]);

      setActiveInvite(
        linkInvitation
          ? {
              invitationId: linkInvitation.invitationId,
              token: linkInvitation.token,
              shortCode: linkInvitation.shortCode,
              expiresAt: linkInvitation.expiresAt,
            }
          : null
      );

      const emails = invitations
        .filter((item) => item.type === 'EMAIL' && item.effectiveState === 'ACTIVE' && !!item.inviteeEmail)
        .map((item) => ({
          invitationId: item.invitationId,
          inviteeEmail: item.inviteeEmail as string,
          shortCode: item.shortCode,
          expiresAt: item.expiresAt,
        }));
      setActiveEmailInvites(emails);
    } catch (err) {
      setInviteError(formatApiError(err));
      setActiveInvite(null);
      setActiveEmailInvites([]);
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
        shortCode: invitation.shortCode,
        expiresAt: invitation.expiresAt,
      });
    } catch (err) {
      setInviteError(formatApiError(err));
    } finally {
      setInviteLoading(false);
    }
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
    onShowToast('Invite code copied');
  }

  async function handleConfirmRevokeInvite() {
    if (!revokeTarget || revokeInviteSubmitting) {
      return;
    }
    setRevokeInviteSubmitting(true);
    setRevokeInviteError(null);
    try {
      await revokeInvitation(token, revokeTarget.invitationId);
      await loadActiveInvite();
      setRevokeInviteSheetOpen(false);
      setRevokeInviteError(null);
      setRevokeTarget(null);
      setInviteError(null);
      onShowToast('Invite revoked.');
    } catch (err) {
      setRevokeInviteError(formatApiError(err));
    } finally {
      setRevokeInviteSubmitting(false);
    }
  }

  function openRevokeInvite(invitationId: string, label: string) {
    setRevokeInviteError(null);
    setRevokeTarget({ invitationId, label });
    setRevokeInviteSheetOpen(true);
  }

  function handleInviteByLink() {
    void handleCreateInvite();
  }

  function handleInviteByEmail() {
    if (inviteLoading || emailInviteSubmitting) {
      return;
    }
    setEmailInviteError(null);
    setInviteEmail('');
    setEmailInviteInputFocused(false);
    setEmailInviteSheetOpen(true);
  }

  function closeEmailInviteSheet() {
    if (emailInviteSubmitting) {
      return;
    }
    setEmailInviteSheetOpen(false);
    setEmailInviteError(null);
    setInviteEmail('');
    setEmailInviteInputFocused(false);
  }

  useEffect(() => {
    if (!emailInviteSheetOpen) {
      return;
    }
    const showSubscription = Keyboard.addListener('keyboardDidShow', () => {
      setEmailInviteInputFocused(true);
    });
    const hideSubscription = Keyboard.addListener('keyboardDidHide', () => {
      setEmailInviteInputFocused(false);
    });

    return () => {
      showSubscription.remove();
      hideSubscription.remove();
    };
  }, [emailInviteSheetOpen]);

  function isValidEmail(value: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  }

  async function handleSubmitEmailInvite() {
    if (emailInviteSubmitting) {
      return;
    }
    const normalizedEmail = inviteEmail.trim().toLowerCase();
    if (!normalizedEmail) {
      setEmailInviteError('Email address is required.');
      return;
    }
    if (!isValidEmail(normalizedEmail)) {
      setEmailInviteError('Enter a valid email address.');
      return;
    }

    setEmailInviteError(null);
    setEmailInviteSubmitting(true);
    try {
      await createInvitationByEmail(token, normalizedEmail);
      setEmailInviteSheetOpen(false);
      setInviteEmail('');
      setEmailInviteInputFocused(false);
      onShowToast(`Invitation sent to ${normalizedEmail}`);
      await loadActiveInvite();
    } catch (err) {
      setEmailInviteError(formatApiError(err));
    } finally {
      setEmailInviteSubmitting(false);
    }
  }

  function formatInviteExpiry(value: string): string {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    return parsed.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  }

  function toggleEmailInvitesExpanded() {
    setEmailInvitesExpanded((prev) => !prev);
  }

  if (!isAdmin) {
    return (
      <AppScreen>
        <TopBar title="Manage invitations" right={<BackIconButton onPress={onDone} />} />
        <View style={styles.contentOffset}>
          <AppCard>
            <Subtle>You don&apos;t have permission to manage invitations.</Subtle>
          </AppCard>
        </View>
      </AppScreen>
    );
  }

  return (
    <AppScreen>
      <TopBar title="Manage invitations" right={<BackIconButton onPress={onDone} />} />

      <View style={styles.contentOffset}>
        <AppCard>
          <View style={styles.sectionContent}>
            <Text style={styles.sectionTitle}>Invite people</Text>
            <View style={styles.invitePeopleActions}>
              <AppButton
                title={inviteLoading ? 'Creating link...' : 'Invite by link'}
                onPress={handleInviteByLink}
                disabled={inviteLoading || emailInviteSubmitting}
                fullWidth
              />
              <AppButton
                title="Invite by email"
                onPress={handleInviteByEmail}
                disabled={inviteLoading || emailInviteSubmitting}
                variant="ghost"
                fullWidth
              />
            </View>
          </View>
        </AppCard>

        <AppCard>
          <View style={styles.sectionContent}>
            <Text style={styles.sectionTitle}>Invite link</Text>
            {inviteLoading ? <Subtle>Loading invite...</Subtle> : null}
            {inviteError ? <Text style={styles.error}>{inviteError}</Text> : null}
            {activeInvite ? (
              <>
                <View style={styles.activeInviteHeader}>
                  <Subtle>Active · Expires {formatInviteExpiry(activeInvite.expiresAt)}</Subtle>
                </View>
                <View style={styles.invitePrimarySection}>
                  <AppButton
                    title="Share invite link"
                    onPress={() => {
                      void handleShareInviteLink();
                    }}
                    fullWidth
                  />
                </View>
                <View style={styles.inviteDetailsSection}>
                  <View style={styles.inviteCodeInlineRow}>
                    <Text style={styles.activeInviteTitle}>Invite code</Text>
                    <Pressable
                      onPress={() => {
                        void handleCopyInviteCode();
                      }}
                      accessibilityRole="button"
                      style={({ pressed }) => [
                        styles.inviteCodeBox,
                        pressed ? styles.inviteCodeBoxPressed : null,
                      ]}
                    >
                      <Text style={styles.inviteCodeText}>{activeInvite.shortCode}</Text>
                    </Pressable>
                    <AppButton
                      title="Copy"
                      onPress={() => {
                        void handleCopyInviteCode();
                      }}
                      variant="secondary"
                    />
                  </View>
                </View>
                <View style={styles.inviteDangerSection}>
                  <AppButton
                    title="Revoke invite"
                    onPress={() => {
                      openRevokeInvite(activeInvite.invitationId, 'this invite link');
                    }}
                    variant="ghost"
                    fullWidth
                  />
                </View>
              </>
            ) : null}
            {!activeInvite ? <Subtle>No active link invite</Subtle> : null}
          </View>
        </AppCard>

        <AppCard>
          <View style={styles.sectionContent}>
            <Pressable
              onPress={toggleEmailInvitesExpanded}
              style={({ pressed }) => [styles.collapsibleHeaderRow, pressed ? styles.collapsibleHeaderPressed : null]}
              accessibilityRole="button"
            >
              <Text style={styles.sectionTitle}>Pending email invitations ({activeEmailInvites.length})</Text>
              <Text style={styles.collapsibleChevron}>{emailInvitesExpanded ? '▲' : '▼'}</Text>
            </Pressable>
            {inviteLoading ? <Subtle>Loading invitations...</Subtle> : null}
            {inviteError ? <Text style={styles.error}>{inviteError}</Text> : null}
            {emailInvitesExpanded && activeEmailInvites.length > 0 ? (
              <View style={styles.emailInviteSection}>
                {activeEmailInvites.map((invite) => (
                  <View key={invite.invitationId} style={styles.emailInviteCard}>
                    <Text style={styles.emailInviteAddress} numberOfLines={2} ellipsizeMode="tail">
                      {invite.inviteeEmail}
                    </Text>
                    <Subtle style={styles.emailInviteExpiry}>
                      Pending · Expires {formatInviteExpiry(invite.expiresAt)}
                    </Subtle>
                    <AppButton
                      title="Revoke invitation"
                      onPress={() => {
                        openRevokeInvite(invite.invitationId, invite.inviteeEmail);
                      }}
                      variant="ghost"
                      fullWidth
                    />
                  </View>
                ))}
              </View>
            ) : null}
            {emailInvitesExpanded && !inviteLoading && activeEmailInvites.length === 0 ? (
              <Subtle>No active email invitations</Subtle>
            ) : null}
          </View>
        </AppCard>
      </View>

      <ActionSheet
        visible={emailInviteSheetOpen}
        onClose={closeEmailInviteSheet}
        presentation={emailInviteInputFocused ? 'large' : 'standard'}
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>Invite by email</Text>
          <AppInput
            value={inviteEmail}
            onChangeText={setInviteEmail}
            onFocus={() => setEmailInviteInputFocused(true)}
            onBlur={() => setEmailInviteInputFocused(false)}
            placeholder="Email address"
            autoFocus
            returnKeyType="send"
            onSubmitEditing={() => {
              void handleSubmitEmailInvite();
            }}
          />
          {emailInviteError ? <Text style={styles.error}>{emailInviteError}</Text> : null}
          <View style={styles.confirmActions}>
            <AppButton
              title={emailInviteSubmitting ? 'Sending...' : 'Send invite'}
              onPress={() => {
                void handleSubmitEmailInvite();
              }}
              disabled={emailInviteSubmitting}
              fullWidth
            />
            <AppButton
              title="Cancel"
              onPress={closeEmailInviteSheet}
              variant="ghost"
              disabled={emailInviteSubmitting}
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
          setRevokeTarget(null);
        }}
        presentation="standard"
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>Revoke this invite?</Text>
          <Subtle>
            {revokeTarget
              ? `Anyone with this invite (${revokeTarget.label}) will lose access to join.`
              : 'Anyone with this code will lose access to join.'}
          </Subtle>
          {revokeInviteError ? <Text style={styles.error}>{revokeInviteError}</Text> : null}
          <View style={styles.confirmActions}>
            <AppButton
              title="Cancel"
              onPress={() => {
                setRevokeInviteSheetOpen(false);
                setRevokeInviteError(null);
                setRevokeTarget(null);
              }}
              variant="ghost"
              disabled={revokeInviteSubmitting}
              fullWidth
            />
            <AppButton
              title="Revoke"
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
    gap: theme.spacing.xxl,
  },
  sectionContent: {
    gap: theme.spacing.md,
    paddingVertical: theme.spacing.xs,
  },
  invitePeopleActions: {
    gap: theme.spacing.sm,
  },
  sectionTitle: {
    ...textStyles.h3,
    fontWeight: '700',
  },
  collapsibleHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing.xs,
  },
  collapsibleHeaderPressed: {
    opacity: 0.8,
  },
  collapsibleChevron: {
    ...textStyles.subtle,
    fontWeight: '700',
  },
  activeInviteTitle: {
    ...textStyles.subtle,
  },
  activeInviteHeader: {
    gap: theme.spacing.xs,
  },
  invitePrimarySection: {
    marginTop: theme.spacing.sm,
  },
  inviteDetailsSection: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.lg,
  },
  inviteCodeInlineRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  inviteCodeBox: {
    borderWidth: 0,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    flex: 1,
  },
  inviteCodeBoxPressed: {
    opacity: 0.85,
  },
  inviteCodeText: {
    ...textStyles.body,
    textAlign: 'center',
    fontFamily: 'monospace',
    letterSpacing: 1,
  },
  inviteDangerSection: {
    marginTop: theme.spacing.md,
  },
  emailInviteSection: {
    marginTop: theme.spacing.md,
    gap: theme.spacing.sm,
  },
  emailInviteCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    gap: 4,
  },
  emailInviteAddress: {
    ...textStyles.body,
    fontWeight: '600',
    width: '100%',
    alignSelf: 'stretch',
  },
  emailInviteExpiry: {
    marginBottom: 2,
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
});
