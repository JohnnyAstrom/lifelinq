import { Button, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { useTodos } from '../features/todo/hooks/useTodos';

type Props = {
  token: string;
  me: MeResponse;
  onCreateTodo: () => void;
};

export function HomeScreen({ token, me, onCreateTodo }: Props) {
  const todos = useTodos(token, 'OPEN');

  return (
    <View>
      <Text>Me:</Text>
      <Text>
        userId: {me.userId} householdId: {me.householdId || 'null'}
      </Text>

      <Text>Todos:</Text>
      {todos.loading ? <Text>Loading todos...</Text> : null}
      {todos.error ? <Text>{todos.error}</Text> : null}
      {todos.items.map((item) => (
        <Text key={item.id}>{item.text}</Text>
      ))}

      <Button title="Create todo" onPress={onCreateTodo} />
    </View>
  );
}
