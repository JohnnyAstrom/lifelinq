import type { ShoppingCategoryKey } from '../utils/shoppingCategories';
import { ScrollView, Text, View } from 'react-native';
import { AppButton, AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type UnitOption = { label: string; value: string };
type CategoryOption = { label: string; value: ShoppingCategoryKey };

type Props = {
  styles: any;
  title: string;
  editNamePlaceholder: string;
  editQuantityPlaceholder: string;
  editCategoryLabel: string;
  editCategorySourceLabel: string | null;
  editProvenanceLabel: string | null;
  autoCategoryLabel: string;
  resetLearnedCategoryLabel: string;
  saveChangesLabel: string;
  removeItemLabel: string;
  closeLabel: string;
  unitNoneLabel: string;
  unitToggleMoreLabel: string;
  unitToggleLessLabel: string;
  nameValue: string;
  quantityValue: string;
  editUnit: string | null;
  editCategoryOverride: ShoppingCategoryKey | null;
  showResetLearnedCategory: boolean;
  editError: string | null;
  showMoreEditUnits: boolean;
  primaryUnitOptions: UnitOption[];
  moreUnitOptions: UnitOption[];
  categoryOptions: CategoryOption[];
  onChangeName: (value: string) => void;
  onChangeQuantity: (value: string) => void;
  onSelectUnit: (value: string | null) => void;
  onSelectCategoryOverride: (value: ShoppingCategoryKey | null) => void;
  onResetLearnedCategory: () => void | Promise<void>;
  onToggleMoreUnits: () => void;
  onSave: () => void | Promise<void>;
  onRemove: () => void | Promise<void>;
  onClose: () => void;
};

export function EditItemSheetContent({
  styles,
  title,
  editNamePlaceholder,
  editQuantityPlaceholder,
  editCategoryLabel,
  editCategorySourceLabel,
  editProvenanceLabel,
  autoCategoryLabel,
  resetLearnedCategoryLabel,
  saveChangesLabel,
  removeItemLabel,
  closeLabel,
  unitNoneLabel,
  unitToggleMoreLabel,
  unitToggleLessLabel,
  nameValue,
  quantityValue,
  editUnit,
  editCategoryOverride,
  showResetLearnedCategory,
  editError,
  showMoreEditUnits,
  primaryUnitOptions,
  moreUnitOptions,
  categoryOptions,
  onChangeName,
  onChangeQuantity,
  onSelectUnit,
  onSelectCategoryOverride,
  onResetLearnedCategory,
  onToggleMoreUnits,
  onSave,
  onRemove,
  onClose,
}: Props) {
  return (
    <>
      <ScrollView
        style={styles.editorScroll}
        contentContainerStyle={styles.editorScrollContent}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.quickAddHeader}>
          <Text style={textStyles.h2}>{title}</Text>
        </View>
        <AppInput placeholder={editNamePlaceholder} value={nameValue} onChangeText={onChangeName} />
        <AppInput
          placeholder={editQuantityPlaceholder}
          value={quantityValue}
          onChangeText={onChangeQuantity}
          keyboardType="decimal-pad"
        />
        {editProvenanceLabel ? (
          <Text style={styles.categorySourceHint}>{editProvenanceLabel}</Text>
        ) : null}
        <View style={styles.unitRow}>
          {primaryUnitOptions.map((unit) => (
            <AppChip
              key={unit.value}
              label={unit.label}
              active={editUnit === unit.value}
              accentKey="shopping"
              onPress={() => onSelectUnit(unit.value)}
            />
          ))}
          <AppChip label={unitNoneLabel} active={!editUnit} accentKey="shopping" onPress={() => onSelectUnit(null)} />
          <AppChip
            label={showMoreEditUnits ? unitToggleLessLabel : unitToggleMoreLabel}
            active={showMoreEditUnits}
            accentKey="shopping"
            onPress={onToggleMoreUnits}
          />
        </View>
        {showMoreEditUnits ? (
          <View style={styles.addUnitRow}>
            {moreUnitOptions.map((unit) => (
              <AppChip
                key={unit.value}
                label={unit.label}
                active={editUnit === unit.value}
                accentKey="shopping"
                onPress={() => onSelectUnit(unit.value)}
              />
            ))}
          </View>
        ) : null}
        <View style={styles.quickEditInputs}>
          <Text style={textStyles.subtle}>{editCategoryLabel}</Text>
          {editCategorySourceLabel ? (
            <Text style={styles.categorySourceHint}>{editCategorySourceLabel}</Text>
          ) : null}
          <View style={styles.unitRow}>
            <AppChip
              label={autoCategoryLabel}
              active={!editCategoryOverride}
              accentKey="shopping"
              onPress={() => onSelectCategoryOverride(null)}
            />
            {categoryOptions.map((category) => (
              <AppChip
                key={category.value}
                label={category.label}
                active={editCategoryOverride === category.value}
                accentKey="shopping"
                onPress={() => onSelectCategoryOverride(category.value)}
              />
            ))}
          </View>
          {showResetLearnedCategory ? (
            <View style={styles.editCategoryActions}>
              <AppButton title={resetLearnedCategoryLabel} onPress={onResetLearnedCategory} variant="ghost" fullWidth />
            </View>
          ) : null}
        </View>
        {editError ? <Text style={styles.error}>{editError}</Text> : null}
        <View style={styles.editorActions}>
          <AppButton title={saveChangesLabel} onPress={onSave} fullWidth accentKey="shopping" />
          <AppButton title={removeItemLabel} onPress={onRemove} variant="ghost" fullWidth />
          <AppButton title={closeLabel} onPress={onClose} variant="secondary" fullWidth />
        </View>
      </ScrollView>
    </>
  );
}
