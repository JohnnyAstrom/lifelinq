import { useEffect, useRef } from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  Alert,
  GestureResponderEvent,
  Keyboard,
  Pressable,
  RefreshControl,
  Share,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { CreateListSheetContent } from '../components/CreateListSheetContent';
import { ListActionsSheetContent } from '../components/ListActionsSheetContent';
import { ShoppingListRow } from '../components/ShoppingListRow';
import { useShoppingListsWorkflow } from '../hooks/useShoppingListsWorkflow';
import { useShoppingLists } from '../hooks/useShoppingLists';
import { getShoppingListTypeDefinitions } from '../utils/shoppingListTypes';
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
    noLists: 'No lists yet.',
    createListTitle: 'Create list',
    createListSubtitle: 'Give your list a name so everyone can add items.',
    createListTypeLabel: 'List type',
    listNamePlaceholder: 'List name',
    createListAction: 'Create list',
    creatingListAction: 'Creating...',
    back: 'Back',
    toBuy: 'to buy',
    allBought: 'Everything bought',
    emptyList: 'No items yet',
    newList: 'New list',
    close: 'Close',
    removeListTitle: 'Remove list?',
    removeListBody: 'This will delete the list and all items.',
    removeConfirm: 'Remove',
    actionsTitle: 'List actions',
    actionShare: 'Share',
    actionEditList: 'Edit list',
    actionDelete: 'Delete',
    editListTitle: 'Edit list',
    editListSubtitle: 'Update the list name and type.',
    editListSave: 'Save changes',
    editListSaving: 'Saving...',
  };
  const listTypeOptions = getShoppingListTypeDefinitions();
  const showInitialLoad = shopping.isInitialLoading && shopping.lists.length === 0;
  const isCreatePending = shopping.pendingMutation?.kind === 'create-list';
  const isEditPending = shopping.pendingMutation?.kind === 'update-list'
    && shopping.pendingMutation.listId === workflowState.editListId;

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
    if (isCreatePending) {
      return;
    }
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

  function closeEdit() {
    if (isEditPending) {
      return;
    }
    workflowActions.closeEdit();
    Keyboard.dismiss();
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: workflowState.showCreate || !!workflowState.activeListId || !!workflowState.editListId,
    onCloseOverlay: () => {
      if (workflowState.editListId) {
        closeEdit();
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

  function openEdit() {
    workflowActions.openEdit();
    closeActions();
  }

  async function handleEditList() {
    await workflowActions.handleEditList();
    closeEdit();
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
    <AppScreen
      scroll={false}
      contentStyle={styles.screenContent}
      header={(
        <TopBar
          title={strings.title}
          subtitle={strings.subtitle}
          icon={<Ionicons name="cart-outline" />}
          accentKey="shopping"
          right={<BackIconButton onPress={onDone} />}
        />
      )}
    >
      <View style={styles.contentOffset}>
        <View style={styles.mainLayout}>
          <ScrollView
            style={styles.mainScroll}
            contentContainerStyle={styles.mainScrollContent}
            keyboardShouldPersistTaps="handled"
            refreshControl={(
              <RefreshControl
                refreshing={shopping.isRefreshing}
                onRefresh={() => {
                  void shopping.reload();
                }}
              />
            )}
          >
            {showInitialLoad ? <Subtle>{strings.loading}</Subtle> : null}
            {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

            <AppCard style={styles.listsCard}>
              <SectionTitle>{strings.yourLists}</SectionTitle>
              {showInitialLoad ? null : shopping.lists.length === 0 ? (
                <Subtle>{strings.noLists}</Subtle>
              ) : (
                <View style={styles.listSection}>
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
                          strings={{
                            toBuy: strings.toBuy,
                            allBought: strings.allBought,
                            empty: strings.emptyList,
                          }}
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
          </ScrollView>
        </View>
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
            typeLabel={strings.createListTypeLabel}
            selectedType={workflowState.newListType}
            typeOptions={listTypeOptions}
            placeholder={strings.listNamePlaceholder}
            createActionLabel={strings.createListAction}
            createActionPendingLabel={strings.creatingListAction}
            closeLabel={strings.close}
            value={workflowState.newListName}
            canCreate={canCreateList}
            isSubmitting={isCreatePending}
            onChangeText={workflowActions.setNewListName}
            onSelectType={workflowActions.setNewListType}
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
            editNameLabel={strings.actionEditList}
            deleteLabel={strings.actionDelete}
            closeLabel={strings.close}
            onShare={handleShareList}
            onEditName={openEdit}
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

      {workflowState.editListId ? (
        <OverlaySheet onClose={closeEdit} sheetStyle={styles.sheet}>
          <CreateListSheetContent
            styles={styles}
            title={strings.editListTitle}
            subtitle={strings.editListSubtitle}
            typeLabel={strings.createListTypeLabel}
            selectedType={workflowState.editListType}
            typeOptions={listTypeOptions}
            placeholder={strings.listNamePlaceholder}
            createActionLabel={strings.editListSave}
            createActionPendingLabel={strings.editListSaving}
            closeLabel={strings.close}
            value={workflowState.editListName}
            canCreate={workflowState.canEditList}
            isSubmitting={isEditPending}
            onChangeText={workflowActions.setEditListName}
            onSelectType={workflowActions.setEditListType}
            onSubmitEditing={async () => {
              if (workflowState.canEditList) {
                await handleEditList();
              }
            }}
            onCreate={handleEditList}
            onClose={closeEdit}
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
    flex: 1,
    paddingTop: theme.layout.topBarOffset + theme.spacing.md,
  },
  mainLayout: {
    flex: 1,
  },
  mainScroll: {
    flex: 1,
  },
  mainScrollContent: {
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.md,
  },
  listsCard: {
    gap: theme.spacing.sm,
  },
  listSection: {
    gap: theme.spacing.sm,
  },
  listGrid: {
    gap: theme.spacing.xs,
  },
  listCard: {
    minHeight: 92,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  listCardDragging: {
    opacity: 0.85,
    borderColor: theme.colors.feature.shopping,
  },
  listMainPressable: {
    flex: 1,
    minHeight: 60,
    paddingVertical: 0,
    paddingHorizontal: 0,
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
    ...textStyles.body,
    fontWeight: '600',
    lineHeight: 24,
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
    ...textStyles.body,
    lineHeight: 22,
  },
  fab: {
    position: 'absolute',
    right: theme.spacing.lg,
    bottom: theme.spacing.lg,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: theme.colors.feature.shopping,
    alignItems: 'center',
    justifyContent: 'center',
    ...theme.elevation.floating,
  },
  fabText: {
    color: '#ffffff',
    fontSize: 28,
    lineHeight: 28,
    fontFamily: theme.typography.heading,
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    padding: theme.layout.sheetPadding,
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  sheetActions: {
    gap: theme.spacing.xs,
  },
  sheetTypeSection: {
    gap: theme.spacing.xs,
  },
  sheetTypeLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  sheetTypeOptions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});

