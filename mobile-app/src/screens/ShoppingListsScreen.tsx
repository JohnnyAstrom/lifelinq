import { useState } from 'react';
import {
  Alert,
  Keyboard,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  Share,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
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
  const insets = useSafeAreaInsets();
  const [newListName, setNewListName] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [activeListId, setActiveListId] = useState<string | null>(null);
  const [renameListId, setRenameListId] = useState<string | null>(null);
  const [renameListName, setRenameListName] = useState('');
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
    removeListTitle: 'Remove list?',
    removeListBody: 'This will delete the list and all items.',
    removeConfirm: 'Remove',
    actionsTitle: 'List actions',
    actionShare: 'Share',
    actionEditName: 'Edit name',
    actionDelete: 'Delete',
    renameTitle: 'Edit list name',
    renameSave: 'Save',
  };

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    await shopping.createList(newListName.trim());
    setNewListName('');
    setShowCreate(false);
    Keyboard.dismiss();
  }

  function closeCreate() {
    setShowCreate(false);
    Keyboard.dismiss();
  }

  function requestRemoveList(listId: string) {
    Alert.alert(strings.removeListTitle, strings.removeListBody, [
      { text: strings.close, style: 'cancel' },
      {
        text: strings.removeConfirm,
        style: 'destructive',
        onPress: () => {
          void shopping.removeList(listId);
        },
      },
    ]);
  }

  function closeActions() {
    setActiveListId(null);
  }

  function closeRename() {
    setRenameListId(null);
    setRenameListName('');
    Keyboard.dismiss();
  }

  function selectedActionList() {
    if (!activeListId) {
      return null;
    }
    return shopping.lists.find((list) => list.id === activeListId) ?? null;
  }

  async function handleShareList() {
    const list = selectedActionList();
    if (!list) {
      return;
    }
    closeActions();
    await Share.share({
      message: `Shopping list: ${list.name}`,
      title: list.name,
    });
  }

  function openRename() {
    const list = selectedActionList();
    if (!list) {
      return;
    }
    setRenameListId(list.id);
    setRenameListName(list.name);
    closeActions();
  }

  async function handleRenameList() {
    if (!renameListId || !renameListName.trim()) {
      return;
    }
    await shopping.renameList(renameListId, renameListName.trim());
    closeRename();
  }

  return (
    <AppScreen scroll={false} contentStyle={styles.screenContent}>
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
                  <View key={list.id} style={styles.listCard}>
                    <Pressable
                      style={({ pressed }) => [styles.listMainPressable, pressed ? styles.listCardPressed : null]}
                      onPress={() => onSelectList(list.id)}
                    >
                      <View style={styles.listMain}>
                        <Text style={styles.listTitle}>{list.name}</Text>
                        <Subtle>
                          {openCount} {strings.open} · {totalCount} {strings.total}
                        </Subtle>
                      </View>
                    </Pressable>
                    <Pressable
                      style={({ pressed }) => [styles.listMenuButton, pressed ? styles.listMenuButtonPressed : null]}
                      onPress={() => setActiveListId(list.id)}
                    >
                      <Text style={styles.listMenuText}>⋮</Text>
                    </Pressable>
                  </View>
                );
              })}
            </View>
          )}
        </AppCard>
      </View>

      <Pressable
        style={[styles.fab, { bottom: Math.max(insets.bottom + 8, 12) }]}
        onPress={() => {
          setShowCreate(true);
        }}
      >
        <Text style={styles.fabText}>+</Text>
      </Pressable>

      <Modal
        visible={showCreate}
        transparent
        animationType="slide"
        onRequestClose={closeCreate}
      >
        <Pressable style={styles.backdrop} onPress={closeCreate}>
          <KeyboardAvoidingView
            style={styles.modalContent}
            behavior="padding"
            enabled={Platform.OS === 'ios'}
          >
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
                <AppButton title={strings.close} onPress={closeCreate} variant="ghost" fullWidth />
              </View>
            </Pressable>
          </KeyboardAvoidingView>
        </Pressable>
      </Modal>

      <Modal
        visible={!!activeListId}
        transparent
        animationType="fade"
        onRequestClose={closeActions}
      >
        <Pressable style={styles.backdrop} onPress={closeActions}>
          <KeyboardAvoidingView
            style={styles.modalContent}
            behavior="padding"
            enabled={Platform.OS === 'ios'}
          >
            <Pressable style={styles.sheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.actionsTitle}</Text>
              <View style={styles.sheetActions}>
                <AppButton title={strings.actionShare} onPress={() => void handleShareList()} fullWidth />
                <AppButton title={strings.actionEditName} onPress={openRename} variant="secondary" fullWidth />
                <AppButton
                  title={strings.actionDelete}
                  onPress={() => {
                    const list = selectedActionList();
                    closeActions();
                    if (list) {
                      requestRemoveList(list.id);
                    }
                  }}
                  variant="ghost"
                  fullWidth
                />
                <AppButton title={strings.close} onPress={closeActions} variant="ghost" fullWidth />
              </View>
            </Pressable>
          </KeyboardAvoidingView>
        </Pressable>
      </Modal>

      <Modal
        visible={!!renameListId}
        transparent
        animationType="slide"
        onRequestClose={closeRename}
      >
        <Pressable style={styles.backdrop} onPress={closeRename}>
          <KeyboardAvoidingView
            style={styles.modalContent}
            behavior="padding"
            enabled={Platform.OS === 'ios'}
          >
            <Pressable style={styles.sheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.renameTitle}</Text>
              <AppInput
                placeholder={strings.listNamePlaceholder}
                value={renameListName}
                onChangeText={setRenameListName}
                autoFocus
              />
              <View style={styles.sheetActions}>
                <AppButton title={strings.renameSave} onPress={() => void handleRenameList()} fullWidth />
                <AppButton title={strings.close} onPress={closeRename} variant="ghost" fullWidth />
              </View>
            </Pressable>
          </KeyboardAvoidingView>
        </Pressable>
      </Modal>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  screenContent: {
    flex: 1,
  },
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
    padding: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  listMainPressable: {
    flex: 1,
    minHeight: 56,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    borderRadius: theme.radius.md,
    justifyContent: 'center',
  },
  listMain: {
    flex: 1,
    gap: 2,
  },
  listCardPressed: {
    opacity: 0.9,
    transform: [{ scale: 0.99 }],
  },
  listTitle: {
    ...textStyles.h3,
    lineHeight: 26,
  },
  listMenuButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
  },
  listMenuButtonPressed: {
    opacity: 0.85,
  },
  listMenuText: {
    ...textStyles.h3,
    lineHeight: 22,
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
  modalContent: {
    flex: 1,
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
