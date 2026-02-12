import { Button, Text, View } from 'react-native';
import { useMe } from '../features/auth/hooks/useMe';
import { useTodos } from '../features/todo/hooks/useTodos';

type Props = {
  token: string;
  onCreateTodo: () => void;
};

export function HomeScreen({ token, onCreateTodo }: Props) {
  const me = useMe(token);
  const todos = useTodos(token, 'OPEN');

  return (
    <View>
      <Text>Me:</Text>
      {me.loading ? <Text>Loading /me...</Text> : null}
      {me.error ? <Text>{me.error}</Text> : null}
      {me.data ? (
        <Text>
          userId: {me.data.userId} householdId: {me.data.householdId || 'null'}
        </Text>
      ) : null}

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
