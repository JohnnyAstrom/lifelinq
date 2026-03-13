import { Ionicons } from '@expo/vector-icons';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  MEAL_INGREDIENT_UNIT_OPTIONS,
  type MealIngredientRow,
} from '../utils/ingredientRows';

type Strings = {
  title: string;
  ingredientNamePlaceholder: string;
  quantityPlaceholder: string;
  addIngredient: string;
  removeIngredient: string;
  loadingIngredients: string;
  close: string;
};

type Props = {
  ingredientRows: MealIngredientRow[];
  isRecipeLoading: boolean;
  onAddIngredientRow: () => void;
  onRemoveIngredientRow: (rowId: string) => void;
  onChangeIngredientName: (rowId: string, value: string) => void;
  onChangeIngredientQuantity: (rowId: string, value: string) => void;
  onToggleIngredientUnit: (rowId: string, value: typeof MEAL_INGREDIENT_UNIT_OPTIONS[number]['value']) => void;
  onClose: () => void;
  strings: Strings;
};

export function MealIngredientsSheet({
  ingredientRows,
  isRecipeLoading,
  onAddIngredientRow,
  onRemoveIngredientRow,
  onChangeIngredientName,
  onChangeIngredientQuantity,
  onToggleIngredientUnit,
  onClose,
  strings,
}: Props) {
  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <AppButton title={strings.addIngredient} onPress={onAddIngredientRow} variant="ghost" />
        </View>
        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            {isRecipeLoading ? <Subtle>{strings.loadingIngredients}</Subtle> : null}
            {ingredientRows.map((row, index) => (
              <View key={row.id} style={styles.ingredientRowCard}>
                <View style={styles.ingredientRowHeader}>
                  <Text style={styles.ingredientRowIndex}>{index + 1}</Text>
                  <Pressable
                    onPress={() => onRemoveIngredientRow(row.id)}
                    style={styles.ingredientRowRemove}
                    accessibilityRole="button"
                    accessibilityLabel={`${strings.removeIngredient} ${index + 1}`}
                  >
                    <Ionicons
                      name="close-outline"
                      size={16}
                      color={theme.colors.textSecondary}
                    />
                    <Text style={styles.ingredientRowRemoveText}>{strings.removeIngredient}</Text>
                  </Pressable>
                </View>
                <View style={styles.ingredientMainRow}>
                  <AppInput
                    placeholder={strings.ingredientNamePlaceholder}
                    value={row.name}
                    onChangeText={(value) => onChangeIngredientName(row.id, value)}
                    style={styles.ingredientNameInput}
                  />
                  <AppInput
                    placeholder={strings.quantityPlaceholder}
                    value={row.quantityText}
                    onChangeText={(value) => onChangeIngredientQuantity(row.id, value)}
                    keyboardType="decimal-pad"
                    style={styles.quantityInput}
                  />
                </View>
                {row.quantityText.length > 0 ? (
                  <View style={styles.unitChipRow}>
                    {MEAL_INGREDIENT_UNIT_OPTIONS.map((option) => (
                      <AppChip
                        key={option.value}
                        label={option.label}
                        active={row.unit === option.value}
                        accentKey="meals"
                        onPress={() => onToggleIngredientUnit(row.id, option.value)}
                      />
                    ))}
                  </View>
                ) : null}
              </View>
            ))}
            <View style={styles.footer}>
              <AppButton title={strings.close} onPress={onClose} variant="secondary" fullWidth />
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
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
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
    gap: theme.spacing.sm,
    minWidth: 0,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
  },
  ingredientRowCard: {
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surface,
  },
  ingredientRowHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  ingredientRowIndex: {
    ...textStyles.subtle,
    fontWeight: '700',
    color: theme.colors.textSecondary,
  },
  ingredientRowRemove: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingVertical: 2,
  },
  ingredientRowRemoveText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  ingredientMainRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: theme.spacing.sm,
  },
  ingredientNameInput: {
    flex: 1,
  },
  quantityInput: {
    width: 104,
  },
  unitChipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    paddingTop: 2,
  },
  footer: {
    paddingTop: theme.spacing.sm,
  },
});
