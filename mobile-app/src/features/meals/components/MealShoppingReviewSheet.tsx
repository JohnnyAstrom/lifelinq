import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  MEAL_INGREDIENT_UNIT_OPTIONS,
  type MealIngredientRow,
} from '../utils/ingredientRows';

type ShoppingListOption = {
  id: string;
  name: string;
};

type Strings = {
  title: string;
  ingredientsLabel: string;
  selectedListLabel: string;
  noShoppingLists: string;
  shoppingSyncFailed: string;
  confirm: string;
  close: string;
};

type Props = {
  ingredientRows: MealIngredientRow[];
  lists: ShoppingListOption[];
  effectiveListId: string | null;
  onSelectListId: (id: string) => void;
  shoppingSyncError: string | null;
  onConfirm: () => void;
  onClose: () => void;
  strings: Strings;
};

function formatIngredientAmount(row: MealIngredientRow) {
  if (!row.quantityText || !row.unit) {
    return null;
  }

  const unitLabel = MEAL_INGREDIENT_UNIT_OPTIONS.find((option) => option.value === row.unit)?.label;
  return unitLabel ? `${row.quantityText} ${unitLabel}` : row.quantityText;
}

export function MealShoppingReviewSheet({
  ingredientRows,
  lists,
  effectiveListId,
  onSelectListId,
  shoppingSyncError,
  onConfirm,
  onClose,
  strings,
}: Props) {
  const ingredients = ingredientRows
    .map((row) => ({
      id: row.id,
      name: row.name.trim(),
      amount: formatIngredientAmount(row),
    }))
    .filter((row) => row.name.length > 0);

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
              <View style={styles.ingredientList}>
                {ingredients.map((ingredient) => (
                  <View key={ingredient.id} style={styles.ingredientRow}>
                    <Text style={styles.ingredientName}>{ingredient.name}</Text>
                    {ingredient.amount ? (
                      <Text style={styles.ingredientAmount}>{ingredient.amount}</Text>
                    ) : null}
                  </View>
                ))}
              </View>
            </View>

            {shoppingSyncError ? (
              <Text style={styles.error}>{strings.shoppingSyncFailed} {shoppingSyncError}</Text>
            ) : null}

            <View style={styles.footer}>
              <AppButton
                title={strings.confirm}
                onPress={onConfirm}
                fullWidth
                accentKey="meals"
                disabled={!effectiveListId || ingredients.length === 0}
              />
              <AppButton title={strings.close} onPress={onClose} variant="ghost" fullWidth />
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
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surfaceAlt,
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
