import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Alert,
  GestureResponderEvent,
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
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
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
  const [orderedListIds, setOrderedListIds] = useState<string[]>([]);
  const [draggingListId, setDraggingListId] = useState<string | null>(null);
  const orderedListIdsRef = useRef<string[]>([]);
  const draggingListIdRef = useRef<string | null>(null);
  const dragStartIndexRef = useRef<number | null>(null);
  const dragMovedRef = useRef(false);
  const dragStartPageYRef = useRef<number | null>(null);
  const lastTouchPageYRef = useRef<number | null>(null);
  const listContainerTopRef = useRef(0);
  const rowHeightRef = useRef(92);
  const finishingDragRef = useRef(false);
  const pendingListReorderSyncRef = useRef(false);
  const strings = {
    title: 'Shopping lists',
    subtitle: 'Choose a list to see items and start shopping.',
    loading: 'Loading lists...',
    yourLists: 'Your lists',
    reorderHint: 'Hold and drag a list to reorder',
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

  useEffect(() => {
    if (draggingListId || pendingListReorderSyncRef.current) {
      return;
    }
    const next = shopping.lists.map((list) => list.id);
    const current = orderedListIdsRef.current;
    const same = current.length === next.length && current.every((value, index) => value === next[index]);
    if (same) {
      return;
    }
    orderedListIdsRef.current = next;
    setOrderedListIds(next);
  }, [shopping.lists, draggingListId]);

  const orderedLists = useMemo(() => {
    if (orderedListIds.length === 0) {
      return shopping.lists;
    }
    const byId = new Map(shopping.lists.map((list) => [list.id, list]));
    const result = orderedListIds
      .map((id) => byId.get(id))
      .filter((list): list is NonNullable<typeof list> => !!list);
    if (result.length === shopping.lists.length) {
      return result;
    }
    return shopping.lists;
  }, [orderedListIds, shopping.lists]);

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    await shopping.createList(newListName.trim());
    setNewListName('');
    setShowCreate(false);
    Keyboard.dismiss();
  }

  const canCreateList = newListName.trim().length > 0;

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
    Keyboard.dismiss();
  }

  function closeRename() {
    setRenameListId(null);
    setRenameListName('');
    Keyboard.dismiss();
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: showCreate || !!activeListId || !!renameListId,
    onCloseOverlay: () => {
      if (renameListId) {
        closeRename();
        return;
      }
      if (activeListId) {
        closeActions();
        return;
      }
      if (showCreate) {
        closeCreate();
      }
    },
  });

  function selectedActionList() {
    if (!activeListId) {
      return null;
    }
    return orderedLists.find((list) => list.id === activeListId) ?? null;
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

  function moveId(ids: string[], from: number, to: number) {
    if (from === to) {
      return ids;
    }
    const next = [...ids];
    const [moved] = next.splice(from, 1);
    next.splice(to, 0, moved);
    return next;
  }

  function startDrag(listId: string, pageY: number) {
    const currentIds = orderedListIds.length > 0 ? orderedListIds : orderedLists.map((list) => list.id);
    if (orderedListIds.length === 0) {
      orderedListIdsRef.current = currentIds;
      setOrderedListIds(currentIds);
    }
    const index = currentIds.indexOf(listId);
    if (index < 0) {
      return;
    }
    setDraggingListId(listId);
    draggingListIdRef.current = listId;
    dragStartIndexRef.current = index;
    dragMovedRef.current = false;
    dragStartPageYRef.current = pageY;
  }

  function handleListTouchMove(event: GestureResponderEvent) {
    if (!draggingListIdRef.current) {
      return;
    }
    const pageY = event?.nativeEvent?.pageY;
    if (typeof pageY !== 'number') {
      return;
    }
    const dragStartY = dragStartPageYRef.current;
    if (dragStartY !== null && Math.abs(pageY - dragStartY) < 12) {
      return;
    }
    const relativeY = pageY - listContainerTopRef.current;
    const currentIds = orderedListIdsRef.current;
    if (currentIds.length === 0) {
      return;
    }
    const rowHeight = rowHeightRef.current;
    const target = Math.max(
      0,
      Math.min(currentIds.length - 1, Math.floor(relativeY / rowHeight))
    );
    const currentIndex = currentIds.indexOf(draggingListIdRef.current);
    if (currentIndex < 0 || target === currentIndex) {
      return;
    }
    dragMovedRef.current = true;
    const next = moveId(currentIds, currentIndex, target);
    orderedListIdsRef.current = next;
    setOrderedListIds(next);
  }

  async function finishDrag() {
    if (finishingDragRef.current) {
      return;
    }
    finishingDragRef.current = true;
    const draggingId = draggingListIdRef.current;
    const startIndex = dragStartIndexRef.current;
    if (!draggingId || startIndex === null) {
      finishingDragRef.current = false;
      return;
    }
    const moved = dragMovedRef.current;
    const finalIndex = orderedListIdsRef.current.indexOf(draggingId);
    setDraggingListId(null);
    draggingListIdRef.current = null;
    dragStartIndexRef.current = null;
    dragMovedRef.current = false;
    dragStartPageYRef.current = null;
    if (!moved || finalIndex < 0 || finalIndex === startIndex) {
      finishingDragRef.current = false;
      return;
    }
    const direction = finalIndex > startIndex ? 'DOWN' : 'UP';
    const steps = Math.abs(finalIndex - startIndex);
    try {
      pendingListReorderSyncRef.current = true;
      await shopping.reorderList(draggingId, direction, steps);
    } finally {
      pendingListReorderSyncRef.current = false;
      finishingDragRef.current = false;
    }
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
              <Subtle>{strings.reorderHint}</Subtle>
              <View
                onTouchStart={(event: GestureResponderEvent) => {
                  const pageY = event.nativeEvent.pageY;
                  const locationY = event.nativeEvent.locationY;
                  lastTouchPageYRef.current = pageY;
                  listContainerTopRef.current = pageY - locationY;
                }}
                onTouchMove={handleListTouchMove}
                onTouchEnd={() => {
                  void finishDrag();
                }}
                onTouchCancel={() => {
                  void finishDrag();
                }}
                style={styles.listGrid}
              >
              {orderedLists.map((list) => {
                const openCount = list.items.filter((item) => item.status !== 'BOUGHT').length;
                const totalCount = list.items.length;
                const isDragging = draggingListId === list.id;
                return (
                  <View key={list.id} style={[styles.listCard, isDragging ? styles.listCardDragging : null]}>
                    <Pressable
                      style={({ pressed }) => [styles.listMainPressable, pressed ? styles.listCardPressed : null]}
                      onPress={() => {
                        if (!draggingListIdRef.current) {
                          onSelectList(list.id);
                        }
                      }}
                      onLongPress={(event) => {
                        startDrag(list.id, event.nativeEvent.pageY);
                      }}
                      delayLongPress={180}
                    >
                      <View style={styles.listMain}>
                        <Text style={styles.listTitle} numberOfLines={1} ellipsizeMode="tail">
                          {list.name}
                        </Text>
                        <Subtle>
                          {openCount} {strings.open} · {totalCount} {strings.total}
                        </Subtle>
                      </View>
                    </Pressable>
                    <Pressable
                      style={({ pressed }) => [styles.listMenuButton, pressed ? styles.listMenuButtonPressed : null]}
                      onPress={() => {
                        if (!draggingListId) {
                          setActiveListId(list.id);
                        }
                      }}
                    >
                      <Text style={styles.listMenuText}>⋮</Text>
                    </Pressable>
                  </View>
                );
              })}
              </View>
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
                onSubmitEditing={async () => {
                  if (canCreateList) {
                    await handleCreateList();
                  }
                }}
                returnKeyType="done"
                autoFocus
              />
              <View style={styles.sheetActions}>
                <AppButton
                  title={strings.createListAction}
                  onPress={handleCreateList}
                  disabled={!canCreateList}
                  fullWidth
                />
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
    minHeight: 84,
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
  listCardDragging: {
    opacity: 0.85,
    borderColor: theme.colors.primary,
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
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
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
