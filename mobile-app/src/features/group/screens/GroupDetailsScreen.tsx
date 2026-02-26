import { StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../../auth/api/meApi';
import { useGroupMembers } from '../hooks/useGroupMembers';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppScreen, SectionTitle, Subtle, TopBar } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
};

export function GroupDetailsScreen({ token, me, onDone }: Props) {
  const members = useGroupMembers(token);
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentRole = activeMembership?.role ?? 'MEMBER';
  const currentGroupName = activeMembership?.groupName ?? 'Unknown group';
  const isAdmin = currentRole === 'ADMIN';
  const strings = {
    title: 'Manage group',
    subtitle: 'View current group details and members.',
    detailsTitle: 'Current group',
    nameLabel: 'Group',
    roleLabel: 'Your role',
    membersTitle: 'Members',
    loadingMembers: 'Loading members...',
    noMembers: 'No members yet.',
    unknownMemberName: 'Member',
    inviteMember: 'Invite Member',
    inviteHint: 'Invite flow uses existing group endpoints and will be added here.',
    noInviteHint: 'Only admins can invite members.',
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
        <AppCard>
          <SectionTitle>{strings.detailsTitle}</SectionTitle>
          <View style={styles.metaList}>
            <View>
              <Subtle>{strings.nameLabel}</Subtle>
              <Text style={styles.metaValue}>{currentGroupName}</Text>
            </View>
            <View>
              <Subtle>{strings.roleLabel}</Subtle>
              <Text style={styles.metaValue}>{currentRole}</Text>
            </View>
          </View>
          {isAdmin ? (
            <>
              <AppButton title={strings.inviteMember} onPress={() => {}} fullWidth />
              <Subtle>{strings.inviteHint}</Subtle>
            </>
          ) : (
            <Subtle>{strings.noInviteHint}</Subtle>
          )}
        </AppCard>

        <AppCard>
          <SectionTitle>{strings.membersTitle}</SectionTitle>
          {members.loading ? <Subtle>{strings.loadingMembers}</Subtle> : null}
          {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
          {members.items.length === 0 && !members.loading ? (
            <Subtle>{strings.noMembers}</Subtle>
          ) : null}
          <View style={styles.list}>
            {members.items.map((member, index) => (
              <View key={member.userId} style={styles.memberRow}>
                <View style={styles.memberTexts}>
                  <Text style={styles.memberName}>{`${strings.unknownMemberName} ${index + 1}`}</Text>
                  <Subtle>{member.userId}</Subtle>
                </View>
                <Text style={styles.memberRole}>{member.role}</Text>
              </View>
            ))}
          </View>
        </AppCard>
      </View>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  metaList: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
    marginBottom: theme.spacing.sm,
  },
  metaValue: {
    ...textStyles.body,
  },
  list: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  memberRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: theme.spacing.sm,
    borderRadius: theme.radius.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceAlt,
    gap: theme.spacing.sm,
  },
  memberTexts: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  memberName: {
    ...textStyles.body,
  },
  memberRole: {
    ...textStyles.subtle,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
