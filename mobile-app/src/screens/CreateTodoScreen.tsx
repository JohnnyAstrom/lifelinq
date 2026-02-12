import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { useTodos } from '../features/todo/hooks/useTodos';

type Props = {
  token: string;
  onDone: () => void;
};

export function CreateTodoScreen({ token, onDone }: Props) {
  const [text, setText] = useState('');
  const todos = useTodos(token, 'OPEN');

  async function handleCreate() {
    if (!text.trim()) {
      return;
    }
    if (todos.loading) {
      return;
    }
    await todos.add(text.trim());
    setText('');
    if (!todos.error) {
      onDone();
    }
  }

  return (
    <View>
      <Text>New todo:</Text>
      <TextInput value={text} onChangeText={setText} />
      {todos.error ? <Text>{todos.error}</Text> : null}
      <Button title={todos.loading ? 'Saving...' : 'Save'} onPress={handleCreate} />
    </View>
  );
}
