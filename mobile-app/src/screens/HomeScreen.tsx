import { Button, RefreshControl, ScrollView, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { useTodos } from '../features/todo/hooks/useTodos';
import { useState } from 'react';

type Props = {
  token: string;
  me: MeResponse;
  onCreateTodo: () => void;
  onManageMembers: () => void;
  onCreateShopping: () => void;
  onMealsProof: () => void;
  onLogout: () => void;
};

export function HomeScreen({
  token,
  me,
  onCreateTodo,
  onManageMembers,
  onCreateShopping,
  onMealsProof,
  onLogout,
}: Props) {
  const [status, setStatus] = useState<'OPEN' | 'COMPLETED' | 'ALL'>('OPEN');
  const todos = useTodos(token, status);

  return (
    <ScrollView
      refreshControl={
        <RefreshControl refreshing={todos.loading} onRefresh={todos.reload} />
      }
    >
      <View>
        <Text>Me:</Text>
        <Text>
          userId: {me.userId} householdId: {me.householdId || 'null'}
        </Text>

        <Text>Todos:</Text>
        <View>
          <Button title="OPEN" onPress={() => setStatus('OPEN')} />
          <Button title="COMPLETED" onPress={() => setStatus('COMPLETED')} />
          <Button title="ALL" onPress={() => setStatus('ALL')} />
        </View>
        {todos.loading ? <Text>Loading todos...</Text> : null}
        {todos.error ? <Text>{todos.error}</Text> : null}
        {!todos.loading && !todos.error && todos.items.length === 0 ? (
          <Text>No todos yet.</Text>
        ) : null}
        {todos.items.map((item) => (
          <View key={item.id}>
            <Text>
              {item.text} ({item.status})
            </Text>
            {item.status === 'OPEN' ? (
              <Button title="Complete" onPress={() => todos.complete(item.id)} />
            ) : null}
          </View>
        ))}

        <Button title="Create todo" onPress={onCreateTodo} />
        <Button title="Household members" onPress={onManageMembers} />
        <Button title="Add shopping item" onPress={onCreateShopping} />
        <Button title="Meals proof" onPress={onMealsProof} />
        <Button title="Logout" onPress={onLogout} />
      </View>
    </ScrollView>
  );
}
