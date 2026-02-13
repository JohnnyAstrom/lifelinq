import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useHouseholdMembers } from '../features/household/hooks/useHouseholdMembers';
import { AppButton, AppCard, AppInput, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

export function HouseholdMembersScreen({ token, onDone }: Props) {
  const members = useHouseholdMembers(token);
  const [userId, setUserId] = useState('');
  const strings = {
    title: 'Household members',
    subtitle: 'Invite, view, and manage who can access this household.',
    membersTitle: 'Members',
    loadingMembers: 'Loading members...',
    noMembers: 'No members yet.',
    remove: 'Remove',
    addMemberTitle: 'Add member',
    userIdPlaceholder: 'User ID',
    addMemberAction: 'Add member',
    back: 'Back',
  };

  async function handleAdd() {
    if (!userId.trim()) {
      return;
    }
    await members.add(userId.trim());
    if (!members.error) {
      setUserId('');
    }
  }

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
      />

      <View style={styles.contentOffset}>
      <AppCard>
        <SectionTitle>{strings.membersTitle}</SectionTitle>
        {members.loading ? <Subtle>{strings.loadingMembers}</Subtle> : null}
        {members.error ? <Text style={styles.error}>{members.error}</Text> : null}
        {members.items.length === 0 && !members.loading ? (
          <Subtle>{strings.noMembers}</Subtle>
        ) : null}
        <View style={styles.list}>
          {members.items.map((member) => (
            <View key={member.userId} style={styles.memberRow}>
              <View>
                <Text style={styles.memberId}>{member.userId}</Text>
                <Text style={styles.memberRole}>{member.role}</Text>
              </View>
              <AppButton
                title={strings.remove}
                onPress={() => members.remove(member.userId)}
                variant="ghost"
              />
            </View>
          ))}
        </View>
      </AppCard>

      <AppCard>
        <SectionTitle>{strings.addMemberTitle}</SectionTitle>
        <AppInput
          value={userId}
          onChangeText={setUserId}
          placeholder={strings.userIdPlaceholder}
        />
        <AppButton title={strings.addMemberAction} onPress={handleAdd} fullWidth />
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
  },
  memberId: {
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
