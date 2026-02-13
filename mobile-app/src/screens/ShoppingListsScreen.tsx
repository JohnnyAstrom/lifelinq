import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';
import { AppButton, AppCard, AppInput, AppScreen, SectionTitle, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onSelectList: (listId: string) => void;
  onDone: () => void;
};

export function ShoppingListsScreen({ token, onSelectList, onDone }: Props) {
  const shopping = useShoppingLists(token);
  const [newListName, setNewListName] = useState('');
  const strings = {
    title: 'Shopping lists',
    subtitle: 'Choose a list to see items and start shopping.',
    loading: 'Loading lists...',
    yourLists: 'Your lists',
    noLists: 'No lists yet.',
    createListTitle: 'Create list',
    listNamePlaceholder: 'List name',
    createListAction: 'Create list',
    back: 'Back',
  };

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    await shopping.createList(newListName.trim());
    setNewListName('');
  }

  return (
    <AppScreen>
      <AppCard style={styles.headerCard}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
      </AppCard>

      {shopping.loading ? <Subtle>{strings.loading}</Subtle> : null}
      {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

      <AppCard>
        <SectionTitle>{strings.yourLists}</SectionTitle>
        {shopping.lists.length === 0 ? (
          <Subtle>{strings.noLists}</Subtle>
        ) : (
          <View style={styles.listGrid}>
            {shopping.lists.map((list) => (
              <AppButton
                key={list.id}
                title={list.name}
                onPress={() => onSelectList(list.id)}
                variant="secondary"
                fullWidth
              />
            ))}
          </View>
        )}
      </AppCard>

      <AppCard>
        <SectionTitle>{strings.createListTitle}</SectionTitle>
        <AppInput
          placeholder={strings.listNamePlaceholder}
          value={newListName}
          onChangeText={setNewListName}
        />
        <AppButton title={strings.createListAction} onPress={handleCreateList} fullWidth />
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
  listGrid: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
