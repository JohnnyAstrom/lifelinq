import { useEffect, useRef } from 'react';
import {
  Alert,
  GestureResponderEvent,
  Keyboard,
  Pressable,
  Share,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { CreateListSheetContent } from '../components/CreateListSheetContent';
import { ListActionsSheetContent } from '../components/ListActionsSheetContent';
import { RenameListSheetContent } from '../components/RenameListSheetContent';
import { ShoppingListRow } from '../components/ShoppingListRow';
import { useShoppingListsWorkflow } from '../hooks/useShoppingListsWorkflow';
import { useShoppingLists } from '../hooks/useShoppingLists';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppCard, AppInput, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onSelectList: (listId: string) => void;
  onDone: () => void;
};

export function ShoppingListsScreen({ token, onSelectList, onDone }: Props) {
  const shopping = useShoppingLists(token);
  const workflow = useShoppingListsWorkflow({ shopping });
  const { state: workflowState, actions: workflowActions } = workflow;
  const insets = useSafeAreaInsets();
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
    if (workflowState.draggingListId || pendingListReorderSyncRef.current) {
      return;
    }
    const next = shopping.lists.map((list) => list.id);
    const current = orderedListIdsRef.current;
    const same = current.length === next.length && current.every((value, index) => value === next[index]);
    if (same) {
      return;
    }
    orderedListIdsRef.current = next;
    workflowActions.setOrderedListIds(next);
  }, [shopping.lists, workflowActions, workflowState.draggingListId]);

  async function handleCreateList() {
    await workflowActions.handleCreateList();
    Keyboard.dismiss();
  }

  const canCreateList = workflowState.canCreateList;

  function closeCreate() {
    workflowActions.closeCreate();
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
    workflowActions.closeActions();
    Keyboard.dismiss();
  }

  function closeRename() {
    workflowActions.closeRename();
    Keyboard.dismiss();
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: workflowState.showCreate || !!workflowState.activeListId || !!workflowState.renameListId,
    onCloseOverlay: () => {
      if (workflowState.renameListId) {
        closeRename();
        return;
      }
      if (workflowState.activeListId) {
        closeActions();
        return;
      }
      if (workflowState.showCreate) {
        closeCreate();
      }
    },
  });

  async function handleShareList() {
    const list = workflowActions.selectedActionList();
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
    workflowActions.openRename();
    closeActions();
  }

  async function handleRenameList() {
    await workflowActions.handleRenameList();
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
    const currentIds = workflowState.orderedListIds.length > 0 ? workflowState.orderedListIds : workflowState.orderedLists.map((list) => list.id);
    if (workflowState.orderedListIds.length === 0) {
      orderedListIdsRef.current = currentIds;
      workflowActions.setOrderedListIds(currentIds);
    }
    const index = currentIds.indexOf(listId);
    if (index < 0) {
      return;
    }
    workflowActions.setDraggingListId(listId);
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
    workflowActions.setOrderedListIds(next);
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
    workflowActions.setDraggingListId(null);
    draggingListIdRef.current = null;
    dragStartIndexRef.current = null;
    dragMovedRef.current = false;
    dragStartPageYRef.current = null;
    if (!moved || finalIndex < 0 || finalIndex === startIndex) {
      finishingDragRef.current = false;
      return;
    }
    try {
      pendingListReorderSyncRef.current = true;
      await workflowActions.finishDragList({
        draggingId,
        startIndex,
        finalIds: orderedListIdsRef.current,
      });
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
        right={<BackIconButton onPress={onDone} />}
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
              {workflowState.orderedLists.map((list) => {
                const openCount = list.items.filter((item) => item.status !== 'BOUGHT').length;
                const totalCount = list.items.length;
                const isDragging = workflowState.draggingListId === list.id;
                return (
                  <ShoppingListRow
                    key={list.id}
                    list={list}
                    openCount={openCount}
                    totalCount={totalCount}
                    strings={{ open: strings.open, total: strings.total }}
                    styles={styles}
                    isDragging={isDragging}
                    onPress={() => {
                      if (!draggingListIdRef.current) {
                        onSelectList(list.id);
                      }
                    }}
                    onLongPress={(event) => {
                      startDrag(list.id, event.nativeEvent.pageY);
                    }}
                    onOpenActions={() => {
                      if (!workflowState.draggingListId) {
                        workflowActions.openActionsForList(list.id);
                      }
                    }}
                  />
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
          workflowActions.setShowCreate(true);
        }}
      >
        <Text style={styles.fabText}>+</Text>
      </Pressable>

      {workflowState.showCreate ? (
        <OverlaySheet onClose={closeCreate} sheetStyle={styles.sheet}>
          <CreateListSheetContent
            styles={styles}
            title={strings.createListTitle}
            subtitle={strings.createListSubtitle}
            placeholder={strings.listNamePlaceholder}
            createActionLabel={strings.createListAction}
            closeLabel={strings.close}
            value={workflowState.newListName}
            canCreate={canCreateList}
            onChangeText={workflowActions.setNewListName}
            onSubmitEditing={async () => {
              if (canCreateList) {
                await handleCreateList();
              }
            }}
            onCreate={handleCreateList}
            onClose={closeCreate}
          />
        </OverlaySheet>
      ) : null}

      {workflowState.activeListId ? (
        <OverlaySheet onClose={closeActions} sheetStyle={styles.sheet}>
          <ListActionsSheetContent
            styles={styles}
            title={strings.actionsTitle}
            shareLabel={strings.actionShare}
            editNameLabel={strings.actionEditName}
            deleteLabel={strings.actionDelete}
            closeLabel={strings.close}
            onShare={handleShareList}
            onEditName={openRename}
            onDelete={() => {
              const list = workflowActions.selectedActionList();
              closeActions();
              if (list) {
                requestRemoveList(list.id);
              }
            }}
            onClose={closeActions}
          />
        </OverlaySheet>
      ) : null}

      {workflowState.renameListId ? (
        <OverlaySheet onClose={closeRename} sheetStyle={styles.sheet}>
          <RenameListSheetContent
            styles={styles}
            title={strings.renameTitle}
            placeholder={strings.listNamePlaceholder}
            saveLabel={strings.renameSave}
            closeLabel={strings.close}
            value={workflowState.renameListName}
            onChangeText={workflowActions.setRenameListName}
            onSave={() => void handleRenameList()}
            onClose={closeRename}
          />
        </OverlaySheet>
      ) : null}
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
