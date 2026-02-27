import { StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { useGroupMembers } from '../features/group/hooks/useGroupMembers';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { AppButton, AppCard, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  me: MeResponse;
  onDone: () => void;
};

export function ManagePlaceScreen({ token, me, onDone }: Props) {
  const members = useGroupMembers(token);
  const activeMembership = me.activeGroupId
    ? me.memberships.find((membership) => membership.groupId === me.activeGroupId) ?? null
    : null;
  const currentRole = activeMembership?.role ?? 'MEMBER';
  const currentPlaceName = activeMembership?.groupName?.trim() || 'My place';
  const isAdmin = currentRole === 'ADMIN';
  const isSolo = me.memberships.length <= 1;
  const memberNames = members.items
    .map((member) => member.displayName?.trim() ?? '')
    .filter((name) => name.length > 0);

  const strings = {
    title: 'Manage place',
    placeTitle: 'Place',
    membersTitle: 'Members',
    loadingMembers: 'Loading members...',
    inviteSomeone: 'Invite someone',
    leavePlace: 'Leave place',
    deletePlace: 'Delete place',
  };

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
  });

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        <AppCard>
          <SectionTitle>{strings.placeTitle}</SectionTitle>
          <View style={styles.placeNameRow}>
            <Text style={styles.placeName}>{currentPlaceName}</Text>
            <Subtle>✎</Subtle>
          </View>
        </AppCard>

        <AppCard>
          <SectionTitle>{strings.membersTitle}</SectionTitle>
          {members.loading ? <Subtle>{strings.loadingMembers}</Subtle> : null}
          {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
          <View style={styles.membersList}>
            {memberNames.map((name) => (
              <Text key={name} style={styles.memberName}>{name}</Text>
            ))}
          </View>
          {isAdmin ? <AppButton title={strings.inviteSomeone} onPress={() => {}} fullWidth /> : null}
          <AppButton title={strings.leavePlace} onPress={() => {}} variant="ghost" disabled={isSolo} fullWidth />
          <AppButton title={strings.deletePlace} onPress={() => {}} variant="ghost" disabled fullWidth />
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
  membersList: {
    marginTop: theme.spacing.sm,
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
