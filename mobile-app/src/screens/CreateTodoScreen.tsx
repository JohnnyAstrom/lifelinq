import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useTodos } from '../features/todo/hooks/useTodos';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

export function CreateTodoScreen({ token, onDone }: Props) {
  const [text, setText] = useState('');
  const todos = useTodos(token, 'OPEN');
  const strings = {
    title: 'New todo',
    subtitle: 'Add something you want to remember today.',
    placeholder: 'Buy milk',
    save: 'Save',
    saving: 'Saving...',
    back: 'Back',
  };

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
    <AppScreen scroll={false} contentStyle={styles.container}>
      <AppCard style={styles.card}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
        <AppInput value={text} onChangeText={setText} placeholder={strings.placeholder} />
        {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}
        <AppButton
          title={todos.loading ? strings.saving : strings.save}
          onPress={handleCreate}
          fullWidth
          disabled={todos.loading}
        />
        <AppButton title={strings.back} onPress={onDone} variant="ghost" fullWidth />
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  card: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
