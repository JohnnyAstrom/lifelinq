import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  MEAL_INGREDIENT_UNIT_OPTIONS,
  type MealIngredientRow,
} from '../utils/ingredientRows';

export type MealIngredientEditorRowStrings = {
  ingredientNamePlaceholder: string;
  quantityPlaceholder: string;
  removeIngredient: string;
};

type Props = {
  row: MealIngredientRow;
  isActive: boolean;
  isReadOnly?: boolean;
  onActivate: () => void;
  onRemove: () => void;
  onChangeName: (value: string) => void;
  onChangeQuantity: (value: string) => void;
  onToggleUnit: (value: typeof MEAL_INGREDIENT_UNIT_OPTIONS[number]['value']) => void;
  strings: MealIngredientEditorRowStrings;
};

function formatIngredientMeta(row: MealIngredientRow) {
  if (!row.quantityText) {
    return null;
  }

  if (!row.unit) {
    return row.quantityText;
  }

  const unitLabel = MEAL_INGREDIENT_UNIT_OPTIONS.find((option) => option.value === row.unit)?.label;
  return unitLabel ? `${row.quantityText} ${unitLabel}` : row.quantityText;
}

export function isMealIngredientRowEffectivelyEmpty(row: MealIngredientRow) {
  return row.name.trim().length === 0;
}

export function MealIngredientEditorRow({
  row,
  isActive,
  isReadOnly = false,
  onActivate,
  onRemove,
  onChangeName,
  onChangeQuantity,
  onToggleUnit,
  strings,
}: Props) {
  const isEffectivelyEmpty = isMealIngredientRowEffectivelyEmpty(row);
  const isExpanded = isActive || isEffectivelyEmpty;
  const meta = formatIngredientMeta(row);

  if (isReadOnly) {
    return (
      <View style={styles.compactRow}>
        <View style={styles.compactMain}>
          <Text style={styles.compactTitle} numberOfLines={1}>
            {row.name.trim()}
          </Text>
          {meta ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {meta}
            </Text>
          ) : null}
        </View>
      </View>
    );
  }

  if (!isExpanded) {
    return (
      <Pressable
        onPress={onActivate}
        accessibilityRole="button"
        style={({ pressed }) => [
          styles.compactRow,
          pressed ? styles.compactRowPressed : null,
        ]}
      >
        <View style={styles.compactMain}>
          <Text style={styles.compactTitle} numberOfLines={1}>
            {row.name.trim()}
          </Text>
          {meta ? (
            <Text style={styles.compactMeta} numberOfLines={1}>
              {meta}
            </Text>
          ) : null}
        </View>
        <Pressable
          onPress={onRemove}
          accessibilityRole="button"
          accessibilityLabel={strings.removeIngredient}
          style={({ pressed }) => [
            styles.removeIconButton,
            pressed ? styles.removeIconButtonPressed : null,
          ]}
        >
          <Ionicons name="close-outline" size={16} color={theme.colors.textSecondary} />
        </Pressable>
      </Pressable>
    );
  }

  return (
    <View style={[styles.expandedRow, isActive ? styles.expandedRowActive : null]}>
      <View style={styles.expandedTopRow}>
        <AppInput
          placeholder={strings.ingredientNamePlaceholder}
          value={row.name}
          onChangeText={onChangeName}
          onFocus={onActivate}
          style={styles.ingredientNameInput}
        />
        <Pressable
          onPress={onRemove}
          accessibilityRole="button"
          accessibilityLabel={strings.removeIngredient}
          style={({ pressed }) => [
            styles.removeIconButton,
            pressed ? styles.removeIconButtonPressed : null,
          ]}
        >
          <Ionicons name="close-outline" size={16} color={theme.colors.textSecondary} />
        </Pressable>
      </View>

      <View style={styles.expandedSecondaryRow}>
        <AppInput
          placeholder={strings.quantityPlaceholder}
          value={row.quantityText}
          onChangeText={onChangeQuantity}
          onFocus={onActivate}
          keyboardType="decimal-pad"
          style={styles.quantityInput}
        />
      </View>

      {row.quantityText.length > 0 && isActive ? (
        <View style={styles.unitChipRow}>
          {MEAL_INGREDIENT_UNIT_OPTIONS.map((option) => (
            <AppChip
              key={option.value}
              label={option.label}
              active={row.unit === option.value}
              accentKey="meals"
              onPress={() => onToggleUnit(option.value)}
            />
          ))}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  compactRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surfaceAlt,
  },
  compactRowPressed: {
    opacity: 0.9,
  },
  compactMain: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  compactTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  compactMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  expandedRow: {
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surface,
  },
  expandedRowActive: {
    borderColor: theme.colors.feature.meals,
  },
  expandedTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  expandedSecondaryRow: {
    flexDirection: 'row',
    gap: theme.spacing.sm,
  },
  ingredientNameInput: {
    flex: 1,
  },
  quantityInput: {
    width: 112,
  },
  unitChipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    paddingTop: 2,
  },
  removeIconButton: {
    width: 28,
    height: 28,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeIconButtonPressed: {
    backgroundColor: theme.colors.surfaceAlt,
  },
});
