import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { useShopping } from '../features/shopping/hooks/useShopping';

type Props = {
  token: string;
  onDone: () => void;
};

export function CreateShoppingItemScreen({ token, onDone }: Props) {
  const [name, setName] = useState('');
  const shopping = useShopping(token);

  async function handleCreate() {
    if (!name.trim()) {
      return;
    }
    await shopping.add(name.trim());
    setName('');
    onDone();
  }

  return (
    <View>
      <Text>Create shopping item</Text>
      <TextInput value={name} onChangeText={setName} />
      {shopping.error ? <Text>{shopping.error}</Text> : null}
      <Button
        title={shopping.loading ? 'Creating...' : 'Create'}
        onPress={handleCreate}
      />
      <Button title="Back" onPress={onDone} />
    </View>
  );
}
