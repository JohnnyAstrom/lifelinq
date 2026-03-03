import { useEffect, useState } from 'react';
import { Pressable, Share, StyleSheet, Text, View } from 'react-native';
import * as Clipboard from 'expo-clipboard';
import type { MeResponse } from '../features/auth/api/meApi';
import { createInvitationLink, getActiveInvitationLink, revokeInvitation } from '../features/group/api/groupApi';
import { WEB_BASE_URL } from '../shared/config/web';
import { formatApiError } from '../shared/api/client';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { ActionSheet } from '../shared/ui/ActionSheet';
import { AppButton, AppCard, AppScreen, BackIconButton, Subtle, TopBar } from '../shared/ui/components';
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

export function ManageInvitationsScreen({ token, me, onDone, onShowToast }: Props) {
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
  const activeGroupId = activeMembership?.groupId ?? null;
  const isAdmin = activeMembership?.role === 'ADMIN';

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: showInviteOptions || revokeInviteSheetOpen,
    onCloseOverlay: () => {
      if (revokeInviteSubmitting) {
        return;
      }
      if (showInviteOptions) {
        setShowInviteOptions(false);
      }
      if (revokeInviteSheetOpen) {
        setRevokeInviteSheetOpen(false);
        setRevokeInviteError(null);
      }
    },
  });

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
        invitationId: invitation.invitationId,
        token: invitation.token,
        shortCode: invitation.shortCode,
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

  function formatInviteExpiry(value: string): string {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    return parsed.toLocaleString();
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
            <AppButton title="Invite someone" onPress={openInviteOptions} fullWidth />
          </View>
        </AppCard>

        <AppCard>
          <View style={styles.sectionContent}>
            <Text style={styles.sectionTitle}>Active invites</Text>
            {inviteLoading ? <Subtle>Loading invite...</Subtle> : null}
            {inviteError ? <Text style={styles.error}>{inviteError}</Text> : null}
            {activeInvite ? (
              <>
                <Subtle>Expires: {formatInviteExpiry(activeInvite.expiresAt)}</Subtle>
                <View style={styles.inviteActions}>
                  <AppButton
                    title="Share link"
                    onPress={() => {
                      void handleShareInviteLink();
                    }}
                    fullWidth
                  />
                </View>
                <Text style={styles.activeInviteTitle}>Invite code (manual entry)</Text>
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
                <Subtle style={styles.tapToCopyHint}>Tap to copy</Subtle>
                <View style={styles.inviteActions}>
                  <AppButton
                    title="Revoke invite"
                    onPress={() => {
                      setRevokeInviteError(null);
                      setRevokeInviteSheetOpen(true);
                    }}
                    variant="secondary"
                    fullWidth
                  />
                </View>
              </>
            ) : (
              <Subtle>No active invites</Subtle>
            )}
          </View>
        </AppCard>
      </View>

      <ActionSheet
        visible={showInviteOptions}
        onClose={() => {
          setShowInviteOptions(false);
        }}
        presentation="standard"
      >
        <View style={styles.confirmContent}>
          <Text style={textStyles.h3}>Invite someone</Text>
          <View style={styles.confirmActions}>
            <AppButton
              title="Invite by link"
              onPress={handleInviteByLink}
              disabled={inviteLoading}
              fullWidth
            />
            <AppButton
              title="Invite by email (Coming soon)"
              onPress={() => {}}
              disabled
              variant="ghost"
              fullWidth
            />
            <AppButton
              title="Cancel"
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
          <Text style={textStyles.h3}>Revoke this invite?</Text>
          <Subtle>Anyone with this code will lose access to join.</Subtle>
          {revokeInviteError ? <Text style={styles.error}>{revokeInviteError}</Text> : null}
          <View style={styles.confirmActions}>
            <AppButton
              title="Cancel"
              onPress={() => {
                setRevokeInviteSheetOpen(false);
                setRevokeInviteError(null);
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
  sectionTitle: {
    ...textStyles.h3,
    fontWeight: '700',
    marginBottom: theme.spacing.xs,
  },
  activeInviteTitle: {
    ...textStyles.subtle,
    marginTop: theme.spacing.xs,
  },
  inviteCodeBox: {
    borderWidth: 0,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingVertical: theme.spacing.md,
    paddingHorizontal: theme.spacing.md,
  },
  inviteCodeBoxPressed: {
    opacity: 0.85,
  },
  inviteCodeText: {
    ...textStyles.h3,
    textAlign: 'center',
    fontFamily: 'monospace',
    letterSpacing: 1.2,
  },
  tapToCopyHint: {
    textAlign: 'center',
    fontSize: 12,
    color: theme.colors.subtle,
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
});
