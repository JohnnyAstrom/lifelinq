import { useState } from 'react';
import {
  Button,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useTodos } from '../features/todo/hooks/useTodos';

type Props = {
  token: string;
  onDone: () => void;
};

export function TodoListScreen({ token, onDone }: Props) {
  const [status, setStatus] = useState<'OPEN' | 'COMPLETED' | 'ALL'>('OPEN');
  const [text, setText] = useState('');
  const todos = useTodos(token, status);

  async function handleAdd() {
    if (!text.trim() || todos.loading) {
      return;
    }
    await todos.add(text.trim());
    setText('');
  }

  return (
    <ScrollView
      contentContainerStyle={styles.container}
      refreshControl={
        <RefreshControl refreshing={todos.loading} onRefresh={todos.reload} />
      }
    >
      <Text style={styles.header}>Todos</Text>

      <View style={styles.filters}>
        <Button title="OPEN" onPress={() => setStatus('OPEN')} />
        <Button title="COMPLETED" onPress={() => setStatus('COMPLETED')} />
        <Button title="ALL" onPress={() => setStatus('ALL')} />
      </View>

      {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}

      <View style={styles.list}>
        {todos.items.length === 0 && !todos.loading ? (
          <Text>No todos yet.</Text>
        ) : null}
        {todos.items.map((item) => (
          <View key={item.id} style={styles.itemRow}>
            <Text style={styles.itemText}>
              {item.text} ({item.status})
            </Text>
            <Button
              title={item.status === 'OPEN' ? 'Complete' : 'Reopen'}
              onPress={() => todos.complete(item.id)}
            />
          </View>
        ))}
      </View>

      <View style={styles.editor}>
        <Text style={styles.sectionTitle}>Add todo</Text>
        <TextInput
          style={styles.input}
          value={text}
          placeholder="What needs to be done?"
          onChangeText={setText}
        />
        <Button title="Add" onPress={handleAdd} />
      </View>

      <Button title="Back" onPress={onDone} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 12,
  },
  header: {
    fontSize: 18,
    fontWeight: '600',
  },
  filters: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  list: {
    gap: 8,
  },
  itemRow: {
    borderWidth: 1,
    borderColor: '#e0e0e0',
    borderRadius: 8,
    padding: 10,
    gap: 6,
  },
  itemText: {
    color: '#333',
  },
  editor: {
    borderWidth: 1,
    borderColor: '#d3d3d3',
    borderRadius: 10,
    padding: 12,
    gap: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
  },
  input: {
    borderWidth: 1,
    borderColor: '#999',
    borderRadius: 8,
    padding: 8,
  },
  error: {
    color: '#b00020',
  },
});
