import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { useHouseholdMembers } from '../features/household/hooks/useHouseholdMembers';

type Props = {
  token: string;
  onDone: () => void;
};

export function HouseholdMembersScreen({ token, onDone }: Props) {
  const members = useHouseholdMembers(token);
  const [userId, setUserId] = useState('');

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
    <View>
      <Text>Household members</Text>
      {members.loading ? <Text>Loading members...</Text> : null}
      {members.error ? <Text>{members.error}</Text> : null}

      {members.items.map((member) => (
        <View key={member.userId}>
          <Text>{member.userId} ({member.role})</Text>
          <Button title="Remove" onPress={() => members.remove(member.userId)} />
        </View>
      ))}

      <Text>Add member by userId</Text>
      <TextInput value={userId} onChangeText={setUserId} />
      <Button title="Add member" onPress={handleAdd} />

      <Button title="Back" onPress={onDone} />
    </View>
  );
}
