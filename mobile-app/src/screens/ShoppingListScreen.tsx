import { useMemo, useState } from 'react';
import {
  Button,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';

type Props = {
  token: string;
  onDone: () => void;
};

export function ShoppingListScreen({ token, onDone }: Props) {
  const shopping = useShoppingLists(token);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);
  const [newListName, setNewListName] = useState('');
  const [newItemName, setNewItemName] = useState('');

  const lists = shopping.lists;
  const selected =
    lists.find((list) => list.id === selectedListId) ?? lists[0] ?? null;

  const items = useMemo(() => {
    return selected ? selected.items : [];
  }, [selected]);

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    await shopping.createList(newListName.trim());
    setNewListName('');
  }

  async function handleAddItem() {
    if (!selected || !newItemName.trim()) {
      return;
    }
    await shopping.addItem(selected.id, newItemName.trim());
    setNewItemName('');
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.headerCard}>
        <Text style={styles.header}>Shopping lists</Text>
        <Text style={styles.subtle}>Keep track of household items.</Text>
      </View>

      {shopping.loading ? <Text>Loading lists...</Text> : null}
      {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

      <View style={styles.sectionCard}>
        <Text style={styles.sectionTitle}>Lists</Text>
        {lists.length === 0 ? (
          <Text style={styles.subtle}>No lists yet.</Text>
        ) : (
          lists.map((list) => (
            <Button
              key={list.id}
              title={selected?.id === list.id ? `• ${list.name}` : list.name}
              onPress={() => setSelectedListId(list.id)}
            />
          ))
        )}
      </View>

      <View style={styles.sectionCard}>
        <Text style={styles.sectionTitle}>Create list</Text>
        <TextInput
          style={styles.input}
          placeholder="List name"
          value={newListName}
          onChangeText={setNewListName}
        />
        <Button title="Create list" onPress={handleCreateList} />
      </View>

      <View style={styles.sectionCard}>
        <Text style={styles.sectionTitle}>Items</Text>
        {!selected ? <Text>Select or create a list.</Text> : null}
        {selected ? (
          <View style={styles.items}>
            {items.length === 0 ? (
              <Text style={styles.subtle}>No items yet.</Text>
            ) : null}
            {items.map((item) => (
              <View key={item.id} style={styles.itemRow}>
                <Text style={styles.itemText}>
                  {item.name} ({item.status})
                </Text>
                <View style={styles.itemActions}>
                  <Button
                    title="Toggle"
                    onPress={() => shopping.toggleItem(selected.id, item.id)}
                  />
                  <Button
                    title="Delete"
                    onPress={() => shopping.removeItem(selected.id, item.id)}
                  />
                </View>
              </View>
            ))}
          </View>
        ) : null}
      </View>

      <View style={styles.sectionCard}>
        <Text style={styles.sectionTitle}>Add item</Text>
        <TextInput
          style={styles.input}
          placeholder="Item name"
          value={newItemName}
          onChangeText={setNewItemName}
        />
        <Button title="Add item" onPress={handleAddItem} />
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
  sectionCard: {
    borderWidth: 1,
    borderColor: '#e7e1d7',
    borderRadius: 12,
    padding: 12,
    gap: 8,
    backgroundColor: '#ffffff',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1e1c16',
  },
  input: {
    borderWidth: 1,
    borderColor: '#c9bfae',
    borderRadius: 8,
    padding: 8,
    backgroundColor: '#fffdf8',
  },
  items: {
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
  itemActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
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
