import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type ShoppingListOption = {
  id: string;
  name: string;
};

type ShoppingReviewIngredient = {
  rowId: string;
  name: string;
  amount: string | null;
};

type Strings = {
  title: string;
  ingredientsLabel: string;
  ingredientsHint: string;
  selectedListLabel: string;
  noShoppingLists: string;
  shoppingSyncFailed: string;
  confirm: string;
  confirming: string;
  close: string;
};

type Props = {
  ingredients: ShoppingReviewIngredient[];
  selectedIngredientRowIds: string[];
  lists: ShoppingListOption[];
  effectiveListId: string | null;
  onSelectListId: (id: string) => void;
  onToggleIngredient: (rowId: string) => void;
  shoppingSyncError: string | null;
  isSubmitting: boolean;
  onConfirm: () => void;
  onClose: () => void;
  strings: Strings;
};

export function MealShoppingReviewSheet({
  ingredients,
  selectedIngredientRowIds,
  lists,
  effectiveListId,
  onSelectListId,
  onToggleIngredient,
  shoppingSyncError,
  isSubmitting,
  onConfirm,
  onClose,
  strings,
}: Props) {
  const selectedIds = new Set(selectedIngredientRowIds);
  const selectedIngredientCount = ingredients.filter((ingredient) => selectedIds.has(ingredient.rowId)).length;

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
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
              {lists.length === 0 ? (
                <Subtle>{strings.noShoppingLists}</Subtle>
              ) : (
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
                </View>
              )}
            </View>

            <View style={styles.section}>
              <Text style={styles.sectionLabel}>{strings.ingredientsLabel}</Text>
              <Subtle>{strings.ingredientsHint}</Subtle>
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
                    <Text style={styles.ingredientName}>{ingredient.name}</Text>
                    {ingredient.amount ? (
                      <Text style={styles.ingredientAmount}>{ingredient.amount}</Text>
                    ) : null}
                  </Pressable>
                ))}
              </View>
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
                disabled={!effectiveListId || selectedIngredientCount === 0 || isSubmitting}
              />
              <AppButton
                title={strings.close}
                onPress={onClose}
                variant="ghost"
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
    opacity: 0.72,
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
    flex: 1,
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
