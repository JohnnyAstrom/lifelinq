import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';
import { AppButton, AppCard, AppInput, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onSelectList: (listId: string) => void;
  onDone: () => void;
};

export function ShoppingListsScreen({ token, onSelectList, onDone }: Props) {
  const shopping = useShoppingLists(token);
  const [newListName, setNewListName] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const strings = {
    title: 'Shopping lists',
    subtitle: 'Choose a list to see items and start shopping.',
    loading: 'Loading lists...',
    yourLists: 'Your lists',
    noLists: 'No lists yet.',
    createListTitle: 'Create list',
    createListSubtitle: 'Give your list a name so everyone can add items.',
    listNamePlaceholder: 'List name',
    createListAction: 'Create list',
    back: 'Back',
    open: 'open',
    total: 'total',
    newList: 'New list',
    close: 'Close',
  };

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    await shopping.createList(newListName.trim());
    setNewListName('');
    setShowCreate(false);
  }

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
      />

      <View style={styles.contentOffset}>
        {shopping.loading ? <Subtle>{strings.loading}</Subtle> : null}
        {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

        <AppCard>
          <SectionTitle>{strings.yourLists}</SectionTitle>
          {shopping.lists.length === 0 ? (
            <Subtle>{strings.noLists}</Subtle>
          ) : (
            <View style={styles.listGrid}>
              {shopping.lists.map((list) => {
                const openCount = list.items.filter((item) => item.status !== 'BOUGHT').length;
                const totalCount = list.items.length;
                return (
                  <Pressable
                    key={list.id}
                    style={({ pressed }) => [styles.listCard, pressed ? styles.listCardPressed : null]}
                    onPress={() => onSelectList(list.id)}
                  >
                    <View>
                      <Text style={styles.listTitle}>{list.name}</Text>
                      <Subtle>
                        {openCount} {strings.open} · {totalCount} {strings.total}
                      </Subtle>
                    </View>
                    <Text style={styles.listChevron}>›</Text>
                  </Pressable>
                );
              })}
            </View>
          )}
        </AppCard>
      </View>

      <Pressable style={styles.fab} onPress={() => setShowCreate(true)}>
        <Text style={styles.fabText}>+</Text>
      </Pressable>

      {showCreate ? (
        <Pressable style={styles.backdrop} onPress={() => setShowCreate(false)}>
          <Pressable style={styles.sheet} onPress={() => null}>
            <View style={styles.sheetHandle} />
            <Text style={textStyles.h3}>{strings.createListTitle}</Text>
            <Subtle>{strings.createListSubtitle}</Subtle>
            <AppInput
              placeholder={strings.listNamePlaceholder}
              value={newListName}
              onChangeText={setNewListName}
              autoFocus
            />
            <View style={styles.sheetActions}>
              <AppButton title={strings.createListAction} onPress={handleCreateList} fullWidth />
              <AppButton title={strings.close} onPress={() => setShowCreate(false)} variant="ghost" fullWidth />
            </View>
          </Pressable>
        </Pressable>
      ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  listGrid: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  listCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  listCardPressed: {
    opacity: 0.9,
    transform: [{ scale: 0.99 }],
  },
  listTitle: {
    ...textStyles.h3,
  },
  listChevron: {
    ...textStyles.subtle,
    fontSize: 20,
    lineHeight: 20,
  },
  fab: {
    position: 'absolute',
    right: theme.spacing.lg,
    bottom: theme.spacing.lg,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOpacity: 0.2,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 6 },
    elevation: 6,
  },
  fabText: {
    color: '#ffffff',
    fontSize: 28,
    lineHeight: 28,
    fontFamily: theme.typography.heading,
  },
  backdrop: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    padding: theme.spacing.lg,
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  sheetHandle: {
    alignSelf: 'center',
    width: 48,
    height: 5,
    borderRadius: 999,
    backgroundColor: theme.colors.borderStrong,
    marginBottom: theme.spacing.sm,
  },
  sheetActions: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
