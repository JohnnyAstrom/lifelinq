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
      <Text style={styles.header}>Shopping lists</Text>

      {shopping.loading ? <Text>Loading lists...</Text> : null}
      {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Lists</Text>
        {lists.length === 0 ? (
          <Text>No lists yet.</Text>
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

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Create list</Text>
        <TextInput
          style={styles.input}
          placeholder="List name"
          value={newListName}
          onChangeText={setNewListName}
        />
        <Button title="Create list" onPress={handleCreateList} />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Items</Text>
        {!selected ? <Text>Select or create a list.</Text> : null}
        {selected ? (
          <View style={styles.items}>
            {items.length === 0 ? <Text>No items yet.</Text> : null}
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

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Add item</Text>
        <TextInput
          style={styles.input}
          placeholder="Item name"
          value={newItemName}
          onChangeText={setNewItemName}
        />
        <Button title="Add item" onPress={handleAddItem} />
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
  section: {
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
  items: {
    gap: 8,
  },
  itemRow: {
    borderWidth: 1,
    borderColor: '#e1e1e1',
    borderRadius: 8,
    padding: 8,
    gap: 6,
  },
  itemText: {
    color: '#333',
  },
  itemActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  error: {
    color: '#b00020',
  },
});
