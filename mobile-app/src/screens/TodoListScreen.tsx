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
      <View style={styles.headerCard}>
        <Text style={styles.header}>Todos</Text>
        <Text style={styles.subtle}>Keep the list moving.</Text>
      </View>

      <View style={styles.filtersCard}>
        <Text style={styles.sectionTitle}>Filter</Text>
        <View style={styles.filters}>
          <Button title="OPEN" onPress={() => setStatus('OPEN')} />
          <Button title="COMPLETED" onPress={() => setStatus('COMPLETED')} />
          <Button title="ALL" onPress={() => setStatus('ALL')} />
        </View>
      </View>

      {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}

      <View style={styles.listCard}>
        {todos.items.length === 0 && !todos.loading ? (
          <Text style={styles.subtle}>No todos yet.</Text>
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

      <View style={styles.editorCard}>
        <Text style={styles.sectionTitle}>Add todo</Text>
        <TextInput
          style={styles.input}
          value={text}
          placeholder="What needs to be done?"
          onChangeText={setText}
        />
        <Button title="Add" onPress={handleAdd} />
      </View>

      <View style={styles.footerCard}>
        <Button title="Back" onPress={onDone} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 12,
    backgroundColor: '#f6f5f2',
  },
  headerCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 6,
  },
  header: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1e1c16',
  },
  filtersCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 8,
  },
  filters: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  listCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 8,
  },
  itemRow: {
    borderWidth: 1,
    borderColor: '#efe7da',
    borderRadius: 10,
    padding: 10,
    gap: 6,
    backgroundColor: '#fffaf0',
  },
  itemText: {
    color: '#40372c',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1e1c16',
  },
  editorCard: {
    borderWidth: 1,
    borderColor: '#e7e1d7',
    borderRadius: 12,
    padding: 12,
    gap: 8,
    backgroundColor: '#ffffff',
  },
  input: {
    borderWidth: 1,
    borderColor: '#c9bfae',
    borderRadius: 8,
    padding: 8,
    backgroundColor: '#fffdf8',
  },
  subtle: {
    color: '#6f675b',
  },
  error: {
    color: '#b00020',
  },
  footerCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e7e1d7',
  },
});
