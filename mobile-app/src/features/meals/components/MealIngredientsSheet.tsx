import { useEffect, useRef, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  type MealIngredientUnit,
  type MealIngredientRow,
} from '../utils/ingredientRows';
import {
  isMealIngredientRowEffectivelyEmpty,
  MealIngredientEditorRow,
  type MealIngredientEditorRowStrings,
} from './MealIngredientEditorRow';

type Strings = MealIngredientEditorRowStrings & {
  title: string;
  addIngredient: string;
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
  onToggleIngredientUnit: (rowId: string, value: MealIngredientUnit) => void;
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
  const [activeRowId, setActiveRowId] = useState<string | null>(ingredientRows[0]?.id ?? null);
  const previousRowCountRef = useRef(ingredientRows.length);

  useEffect(() => {
    const previousCount = previousRowCountRef.current;
    previousRowCountRef.current = ingredientRows.length;

    if (ingredientRows.length === 0) {
      if (activeRowId !== null) {
        setActiveRowId(null);
      }
      return;
    }

    if (ingredientRows.length > previousCount) {
      setActiveRowId(ingredientRows[ingredientRows.length - 1]?.id ?? null);
      return;
    }

    const hasActiveRow = activeRowId != null && ingredientRows.some((row) => row.id === activeRowId);
    if (hasActiveRow) {
      return;
    }

    const firstEmptyRow = ingredientRows.find((row) => isMealIngredientRowEffectivelyEmpty(row));
    setActiveRowId(firstEmptyRow?.id ?? ingredientRows[0]?.id ?? null);
  }, [ingredientRows, activeRowId]);

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
            {ingredientRows.map((row) => (
              <MealIngredientEditorRow
                key={row.id}
                row={row}
                isActive={row.id === activeRowId}
                onActivate={() => setActiveRowId(row.id)}
                onRemove={() => onRemoveIngredientRow(row.id)}
                onChangeName={(value) => onChangeIngredientName(row.id, value)}
                onChangeQuantity={(value) => onChangeIngredientQuantity(row.id, value)}
                onToggleUnit={(value) => onToggleIngredientUnit(row.id, value)}
                strings={strings}
              />
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
  footer: {
    paddingTop: theme.spacing.sm,
  },
});
