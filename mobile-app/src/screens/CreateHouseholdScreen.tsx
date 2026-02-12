import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { createHousehold } from '../features/household/api/householdApi';

type Props = {
  token: string;
  onCreated: () => void;
};

export function CreateHouseholdScreen({ token, onCreated }: Props) {
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleCreate() {
    if (!name.trim() || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await createHousehold(token, name.trim());
      onCreated();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <View>
      <Text>Create household</Text>
      <TextInput value={name} onChangeText={setName} />
      {error ? <Text>{error}</Text> : null}
      <Button title={loading ? 'Creating...' : 'Create'} onPress={handleCreate} />
    </View>
  );
}
