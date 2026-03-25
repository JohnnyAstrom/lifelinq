import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type ShoppingListOption = {
  id: string;
  name: string;
};

type ShoppingReviewIngredient = {
  rowId: string;
  name: string;
  amount: string | null;
  note: string | null;
};

type Strings = {
  title: string;
  subtitle?: string;
  emptyIngredientsHint?: string;
  selectedListLabel: string;
  newListLabel: string;
  newListPlaceholder: string;
  createListAction: string;
  creatingListAction: string;
  alreadyOnListLabel: string;
  addNowLabel: string;
  onAnotherListLabel: string;
  everythingAlreadyOnListHint: string;
  checkingListHint: string;
  checkingListFailedHint: string;
  noShoppingLists: string;
  shoppingSyncFailed: string;
  confirm: string;
  confirming: string;
  close: string;
};

type Props = {
  ingredients: ShoppingReviewIngredient[];
  representedIngredients?: ShoppingReviewIngredient[];
  selectedIngredientRowIds: string[];
  lists: ShoppingListOption[];
  effectiveListId: string | null;
  hasProjection?: boolean;
  isLoadingProjection?: boolean;
  projectionError?: string | null;
  showCreateListForm?: boolean;
  newListName: string;
  isCreatingList: boolean;
  onSelectListId: (id: string) => void;
  onToggleIngredient: (rowId: string) => void;
  onOpenCreateList: () => void;
  onCloseCreateList: () => void;
  onChangeNewListName: (value: string) => void;
  onCreateList: () => void;
  shoppingSyncError: string | null;
  isSubmitting: boolean;
  onConfirm: () => void;
  onClose: () => void;
  strings: Strings;
};

export function MealShoppingReviewSheet({
  ingredients,
  representedIngredients = [],
  selectedIngredientRowIds,
  lists,
  effectiveListId,
  hasProjection = false,
  isLoadingProjection = false,
  projectionError = null,
  showCreateListForm = false,
  newListName,
  isCreatingList,
  onSelectListId,
  onToggleIngredient,
  onOpenCreateList,
  onCloseCreateList,
  onChangeNewListName,
  onCreateList,
  shoppingSyncError,
  isSubmitting,
  onConfirm,
  onClose,
  strings,
}: Props) {
  const selectedIds = new Set(selectedIngredientRowIds);
  const selectedIngredientCount = ingredients.filter((ingredient) => selectedIds.has(ingredient.rowId)).length;
  const showEmptyIngredients = !isLoadingProjection
    && !projectionError
    && hasProjection
    && ingredients.length === 0
    && representedIngredients.length === 0;
  const showEverythingAlreadyHint = !isLoadingProjection
    && !projectionError
    && hasProjection
    && ingredients.length === 0
    && representedIngredients.length > 0;
  const showProjectionPending = !!effectiveListId && !hasProjection && !isLoadingProjection && !projectionError;
  const canCreateList = newListName.trim().length > 0;

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          {strings.subtitle ? (
            <Subtle>{strings.subtitle}</Subtle>
          ) : null}
        </View>
        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.section}>
              <Text style={styles.sectionLabel}>{strings.selectedListLabel}</Text>
              {lists.length > 0 ? (
                <View style={styles.chipRow}>
                  {lists.map((list) => (
                    <AppChip
                      key={list.id}
                      label={list.name}
                      active={list.id === effectiveListId}
                      accentKey="meals"
                      onPress={() => onSelectListId(list.id)}
                    />
                  ))}
                  {!showCreateListForm ? (
                    <AppChip
                      label={strings.newListLabel}
                      active={false}
                      accentKey="meals"
                      onPress={onOpenCreateList}
                    />
                  ) : null}
                </View>
              ) : (
                <Subtle>{strings.noShoppingLists}</Subtle>
              )}

              {showCreateListForm ? (
                <View style={styles.createListCard}>
                  <AppInput
                    placeholder={strings.newListPlaceholder}
                    value={newListName}
                    onChangeText={onChangeNewListName}
                    returnKeyType="done"
                    onSubmitEditing={onCreateList}
                    autoFocus={lists.length === 0}
                  />
                  <View style={styles.createListActions}>
                    <AppButton
                      title={isCreatingList ? strings.creatingListAction : strings.createListAction}
                      onPress={onCreateList}
                      accentKey="meals"
                      disabled={!canCreateList || isCreatingList}
                    />
                    {lists.length > 0 ? (
                      <AppButton
                        title={strings.close}
                        onPress={onCloseCreateList}
                        variant="ghost"
                        disabled={isCreatingList}
                      />
                    ) : null}
                  </View>
                </View>
              ) : null}
            </View>

            <View style={styles.section}>
              {representedIngredients.length > 0 ? (
                <View style={styles.section}>
                  <Text style={styles.sectionLabel}>{strings.alreadyOnListLabel}</Text>
                  <View style={styles.ingredientList}>
                    {representedIngredients.map((ingredient) => (
                      <View
                        key={ingredient.rowId}
                        style={[styles.ingredientRow, styles.representedIngredientRow]}
                      >
                        <View style={[styles.checkbox, styles.checkboxSelected]}>
                          <Ionicons name="checkmark" size={14} color="#fff" />
                        </View>
                        <View style={styles.ingredientCopy}>
                          <Text style={styles.ingredientName}>{ingredient.name}</Text>
                          {ingredient.note ? (
                            <Text style={styles.ingredientNote}>{ingredient.note}</Text>
                          ) : null}
                        </View>
                        <View style={styles.ingredientMeta}>
                          {ingredient.amount ? (
                            <Text style={styles.ingredientAmount}>{ingredient.amount}</Text>
                          ) : null}
                        </View>
                      </View>
                    ))}
                  </View>
                </View>
              ) : null}

              <Text style={styles.sectionLabel}>{strings.addNowLabel}</Text>
              {isLoadingProjection || showProjectionPending ? (
                <Subtle>{strings.checkingListHint}</Subtle>
              ) : projectionError ? (
                <Text style={styles.error}>{strings.checkingListFailedHint}</Text>
              ) : showEmptyIngredients ? (
                <Subtle>{strings.emptyIngredientsHint ?? 'No ingredients saved for this meal yet.'}</Subtle>
              ) : showEverythingAlreadyHint ? (
                <Subtle>{strings.everythingAlreadyOnListHint}</Subtle>
              ) : (
                <View style={styles.ingredientList}>
                  {ingredients.map((ingredient) => (
                    <Pressable
                      key={ingredient.rowId}
                      onPress={() => onToggleIngredient(ingredient.rowId)}
                      style={({ pressed }) => [
                        styles.ingredientRow,
                        !selectedIds.has(ingredient.rowId) ? styles.ingredientRowUnselected : null,
                        pressed ? styles.ingredientRowPressed : null,
                      ]}
                    >
                      <View style={[
                        styles.checkbox,
                        selectedIds.has(ingredient.rowId) ? styles.checkboxSelected : null,
                      ]}>
                        {selectedIds.has(ingredient.rowId) ? (
                          <Ionicons name="checkmark" size={14} color="#fff" />
                        ) : null}
                      </View>
                      <View style={styles.ingredientCopy}>
                        <Text style={styles.ingredientName}>{ingredient.name}</Text>
                        {ingredient.note ? (
                          <Text style={styles.ingredientNote}>{ingredient.note}</Text>
                        ) : null}
                      </View>
                      <View style={styles.ingredientMeta}>
                        {ingredient.amount ? (
                          <Text style={styles.ingredientAmount}>{ingredient.amount}</Text>
                        ) : null}
                      </View>
                    </Pressable>
                  ))}
                </View>
              )}
            </View>

            {shoppingSyncError ? (
              <Text style={styles.error}>{strings.shoppingSyncFailed} {shoppingSyncError}</Text>
            ) : null}

            <View style={styles.footer}>
              <AppButton
                title={isSubmitting ? strings.confirming : strings.confirm}
                onPress={onConfirm}
                fullWidth
                accentKey="meals"
                disabled={!effectiveListId || selectedIngredientCount === 0 || isSubmitting || isLoadingProjection || !!projectionError || !hasProjection}
              />
              <AppButton
                title={strings.close}
                onPress={onClose}
                variant="secondary"
                fullWidth
                disabled={isSubmitting}
              />
            </View>
          </ScrollView>
        </View>
      </View>
    </OverlaySheet>
  );
}

const styles = StyleSheet.create({
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    paddingTop: theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.layout.sheetPadding,
    borderWidth: 1,
    borderColor: theme.colors.border,
    overflow: 'hidden',
  },
  layout: {
    maxHeight: '100%',
    flexShrink: 1,
    minHeight: 0,
  },
  header: {
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  body: {
    flexShrink: 1,
    minHeight: 0,
  },
  scroll: {
    minHeight: 0,
    maxHeight: '100%',
    marginTop: theme.spacing.sm,
  },
  scrollContent: {
    gap: theme.spacing.md,
    minWidth: 0,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
  },
  section: {
    gap: theme.spacing.xs,
  },
  sectionLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  createListCard: {
    gap: theme.spacing.sm,
    paddingTop: theme.spacing.xs,
  },
  createListActions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  ingredientList: {
    gap: theme.spacing.xs,
  },
  ingredientRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surfaceAlt,
  },
  ingredientRowPressed: {
    opacity: 0.82,
  },
  ingredientRowUnselected: {
    backgroundColor: theme.colors.surface,
  },
  representedIngredientRow: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: theme.colors.border,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.surface,
  },
  checkboxSelected: {
    backgroundColor: theme.colors.feature.meals,
    borderColor: theme.colors.feature.meals,
  },
  ingredientName: {
    ...textStyles.body,
  },
  ingredientCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  ingredientMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    flexShrink: 0,
    marginLeft: 'auto',
  },
  ingredientNote: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  ingredientAmount: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  footer: {
    gap: theme.spacing.sm,
    paddingTop: theme.spacing.xs,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
