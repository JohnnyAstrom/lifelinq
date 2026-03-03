import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../../auth/api/meApi';
import type { MemberItemResponse } from '../api/groupMembersApi';
import { useGroupMembers } from '../hooks/useGroupMembers';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { ActionSheet } from '../../../shared/ui/ActionSheet';
import { AppButton, AppCard, AppScreen, BackIconButton, Subtle, TopBar } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
};

export function GroupDetailsScreen({ token, me, onDone }: Props) {
  const members = useGroupMembers(token);
  const [selectedMember, setSelectedMember] = useState<MemberItemResponse | null>(null);
  const [actionsOpen, setActionsOpen] = useState(false);
  const [removeSubmitting, setRemoveSubmitting] = useState(false);
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentRole = activeMembership?.role ?? 'MEMBER';
  const isAdmin = currentRole === 'ADMIN';
  const currentUserId = me.userId;
  const currentGroupName = activeMembership?.groupName ?? 'My place';

  const sortedMembers = useMemo(
    () => [...members.items].sort((a, b) => (a.displayName ?? '').localeCompare(b.displayName ?? '')),
    [members.items]
  );

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: actionsOpen,
    onCloseOverlay: () => {
      if (removeSubmitting) {
        return;
      }
      setActionsOpen(false);
      setSelectedMember(null);
    },
  });

  function openMemberActions(member: MemberItemResponse) {
    if (!isAdmin || removeSubmitting) {
      return;
    }
    setSelectedMember(member);
    setActionsOpen(true);
  }

  async function handleRemoveSelectedMember() {
    if (!selectedMember || removeSubmitting) {
      return;
    }
    setRemoveSubmitting(true);
    try {
      await members.remove(selectedMember.userId);
      setActionsOpen(false);
      setSelectedMember(null);
    } finally {
      setRemoveSubmitting(false);
    }
  }

  function roleLabel(role: MemberItemResponse['role']): string {
    return role === 'ADMIN' ? 'Admin' : 'Member';
  }

  return (
    <AppScreen>
      <TopBar title="Manage members" subtitle={currentGroupName} right={<BackIconButton onPress={onDone} />} />

      <View style={styles.contentOffset}>
        <AppCard>
          {members.loading ? <Subtle>Loading members...</Subtle> : null}
          {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
          {sortedMembers.length === 0 && !members.loading ? <Subtle>No members yet.</Subtle> : null}

          <View style={styles.list}>
            {sortedMembers.map((member, index) => {
              const name = member.displayName?.trim() || 'Unknown member';
              const isCurrentUser = member.userId === currentUserId;
              const showDivider = index < sortedMembers.length - 1;

              return (
                <View key={member.userId}>
                  <View style={styles.memberRow}>
                    <View style={styles.memberIdentity}>
                      <Text style={styles.memberName}>{name}</Text>
                      <View style={styles.metaRow}>
                        <Text style={styles.memberRole}>{roleLabel(member.role)}</Text>
                        {isCurrentUser ? (
                          <View style={styles.youBadge}>
                            <Text style={styles.youBadgeText}>You</Text>
                          </View>
                        ) : null}
                      </View>
                    </View>

                    {isAdmin ? (
                      <Pressable
                        accessibilityRole="button"
                        onPress={() => openMemberActions(member)}
                        style={({ pressed }) => [styles.overflowButton, pressed ? styles.overflowPressed : null]}
                      >
                        <Text style={styles.overflowText}>⋯</Text>
                      </Pressable>
                    ) : null}
                  </View>
                  {showDivider ? <View style={styles.divider} /> : null}
                </View>
              );
            })}
          </View>
        </AppCard>
      </View>

      <ActionSheet
        visible={actionsOpen}
        onClose={() => {
          if (removeSubmitting) {
            return;
          }
          setActionsOpen(false);
          setSelectedMember(null);
        }}
        presentation="standard"
      >
        <View style={styles.sheetContent}>
          {selectedMember?.role === 'MEMBER' ? (
            <AppButton title="Make admin" onPress={() => {}} variant="ghost" disabled fullWidth />
          ) : null}

          <Pressable
            accessibilityRole="button"
            onPress={() => {
              void handleRemoveSelectedMember();
            }}
            disabled={removeSubmitting}
            style={({ pressed }) => [
              styles.removeAction,
              pressed ? styles.removeActionPressed : null,
              removeSubmitting ? styles.removeActionDisabled : null,
            ]}
          >
            <Text style={styles.removeActionText}>Remove from place</Text>
          </Pressable>

          <AppButton
            title="Close"
            onPress={() => {
              setActionsOpen(false);
              setSelectedMember(null);
            }}
            variant="ghost"
            disabled={removeSubmitting}
            fullWidth
          />
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
  list: {
    marginTop: theme.spacing.xs,
  },
  memberRow: {
    minHeight: 56,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
  },
  memberIdentity: {
    flex: 1,
    gap: 4,
  },
  memberName: {
    ...textStyles.body,
    fontWeight: '500',
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  memberRole: {
    ...textStyles.subtle,
  },
  youBadge: {
    borderRadius: 999,
    backgroundColor: theme.colors.surfaceAlt,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  youBadgeText: {
    ...textStyles.subtle,
    fontSize: 11,
  },
  divider: {
    height: 1,
    backgroundColor: theme.colors.border,
  },
  overflowButton: {
    minWidth: 36,
    minHeight: 36,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 999,
  },
  overflowPressed: {
    backgroundColor: theme.colors.surfaceAlt,
  },
  overflowText: {
    ...textStyles.h3,
    lineHeight: 16,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  sheetContent: {
    gap: theme.spacing.sm,
  },
  removeAction: {
    minHeight: 44,
    borderRadius: theme.radius.md,
    borderWidth: 1,
    borderColor: theme.colors.danger,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: theme.spacing.md,
  },
  removeActionPressed: {
    opacity: 0.85,
  },
  removeActionDisabled: {
    opacity: 0.5,
  },
  removeActionText: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
