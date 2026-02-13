import { useState } from 'react';
import {
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useTodos } from '../features/todo/hooks/useTodos';
import {
  AppButton,
  AppCard,
  AppChip,
  AppInput,
  AppScreen,
  SectionTitle,
  Subtle,
} from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

export function TodoListScreen({ token, onDone }: Props) {
  const [status, setStatus] = useState<'OPEN' | 'COMPLETED' | 'ALL'>('OPEN');
  const [text, setText] = useState('');
  const todos = useTodos(token, status);
  const strings = {
    title: 'Todos',
    subtitle: 'Keep the list moving.',
    filterTitle: 'Filter',
    open: 'Open',
    completed: 'Completed',
    all: 'All',
    listTitle: 'List',
    noTodos: 'No todos yet.',
    complete: 'Complete',
    reopen: 'Reopen',
    addTodoTitle: 'Add todo',
    addPlaceholder: 'What needs to be done?',
    addAction: 'Add',
    back: 'Back',
  };

  async function handleAdd() {
    if (!text.trim() || todos.loading) {
      return;
    }
    await todos.add(text.trim());
    setText('');
  }

  return (
    <AppScreen
      refreshControl={
        <RefreshControl refreshing={todos.loading} onRefresh={todos.reload} />
      }
    >
      <AppCard style={styles.headerCard}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
      </AppCard>

        <AppCard>
          <SectionTitle>{strings.filterTitle}</SectionTitle>
          <View style={styles.filters}>
            <AppChip label={strings.open} active={status === 'OPEN'} onPress={() => setStatus('OPEN')} />
            <AppChip label={strings.completed} active={status === 'COMPLETED'} onPress={() => setStatus('COMPLETED')} />
            <AppChip label={strings.all} active={status === 'ALL'} onPress={() => setStatus('ALL')} />
          </View>
        </AppCard>

        {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}

        <AppCard>
          <SectionTitle>{strings.listTitle}</SectionTitle>
          {todos.items.length === 0 && !todos.loading ? (
            <Subtle>{strings.noTodos}</Subtle>
          ) : null}
          <View style={styles.list}>
            {todos.items.map((item) => (
              <View key={item.id} style={styles.itemRow}>
                <View style={styles.itemInfo}>
                  <Text style={styles.itemText}>{item.text}</Text>
                  <Text style={styles.itemStatus}>{item.status}</Text>
                </View>
                <AppButton
                  title={item.status === 'OPEN' ? strings.complete : strings.reopen}
                  onPress={() => todos.complete(item.id)}
                  variant="secondary"
                />
              </View>
            ))}
          </View>
        </AppCard>

        <AppCard>
          <SectionTitle>{strings.addTodoTitle}</SectionTitle>
          <AppInput
            value={text}
            placeholder={strings.addPlaceholder}
            onChangeText={setText}
          />
          <AppButton title={strings.addAction} onPress={handleAdd} fullWidth />
        </AppCard>

      <AppCard>
        <AppButton title={strings.back} onPress={onDone} variant="ghost" fullWidth />
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  headerCard: {
    gap: theme.spacing.xs,
  },
  filters: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  list: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  itemRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  itemInfo: {
    flex: 1,
  },
  itemText: {
    ...textStyles.body,
  },
  itemStatus: {
    ...textStyles.subtle,
    marginTop: theme.spacing.xs,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
