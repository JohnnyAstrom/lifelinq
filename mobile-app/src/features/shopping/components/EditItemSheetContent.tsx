import { useEffect, useState } from 'react';
import type { ShoppingCategoryKey } from '../utils/shoppingCategories';
import { Pressable, ScrollView, Text, View } from 'react-native';
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
  changeCategoryLabel: string;
  editCurrentCategoryLabel: string;
  editCategorySourceLabel: string | null;
  editProvenanceLabel: string | null;
  autoCategoryLabel: string;
  resetLearnedCategoryLabel: string;
  saveChangesLabel: string;
  saveChangesPendingLabel: string;
  removeItemLabel: string;
  removeItemPendingLabel: string;
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
  isSaving?: boolean;
  isRemoving?: boolean;
};

export function EditItemSheetContent({
  styles,
  title,
  editNamePlaceholder,
  editQuantityPlaceholder,
  editCategoryLabel,
  changeCategoryLabel,
  editCurrentCategoryLabel,
  editCategorySourceLabel,
  editProvenanceLabel,
  autoCategoryLabel,
  resetLearnedCategoryLabel,
  saveChangesLabel,
  saveChangesPendingLabel,
  removeItemLabel,
  removeItemPendingLabel,
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
  isSaving = false,
  isRemoving = false,
}: Props) {
  const [isCategoryExpanded, setIsCategoryExpanded] = useState(false);

  useEffect(() => {
    if (editCategoryOverride) {
      setIsCategoryExpanded(true);
    }
  }, [editCategoryOverride]);

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
        <View style={styles.editPrimarySection}>
          <AppInput placeholder={editNamePlaceholder} value={nameValue} onChangeText={onChangeName} />
          <AppInput
            placeholder={editQuantityPlaceholder}
            value={quantityValue}
            onChangeText={onChangeQuantity}
            keyboardType="decimal-pad"
          />
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
        </View>

        {(editProvenanceLabel || editCategorySourceLabel || showResetLearnedCategory || categoryOptions.length > 0) ? (
          <View style={styles.editSecondarySection}>
            {editProvenanceLabel ? (
              <Text style={styles.secondaryMetaText}>{editProvenanceLabel}</Text>
            ) : null}
            <View style={styles.quickEditInputs}>
              <Pressable
                style={({ pressed }) => [
                  styles.categorySummaryCard,
                  pressed ? styles.categorySummaryCardPressed : null,
                ]}
                onPress={() => setIsCategoryExpanded((prev) => !prev)}
              >
                <View style={styles.categorySummaryMain}>
                  <Text style={styles.secondarySectionLabel}>{editCategoryLabel}</Text>
                  <Text style={styles.categorySummaryValue}>{editCurrentCategoryLabel}</Text>
                  {editCategorySourceLabel ? (
                    <Text style={styles.categorySourceHint}>{editCategorySourceLabel}</Text>
                  ) : null}
                </View>
                <Text style={styles.categorySummaryAction}>
                  {isCategoryExpanded ? closeLabel : changeCategoryLabel}
                </Text>
              </Pressable>
              {isCategoryExpanded ? (
                <>
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
                </>
              ) : null}
            </View>
          </View>
        ) : null}
        {editError ? <Text style={styles.error}>{editError}</Text> : null}
        <View style={styles.editorActions}>
          <AppButton
            title={isSaving ? saveChangesPendingLabel : saveChangesLabel}
            onPress={onSave}
            disabled={isSaving || isRemoving}
            fullWidth
            accentKey="shopping"
          />
          <View style={styles.editorSecondaryActions}>
            <AppButton
              title={isRemoving ? removeItemPendingLabel : removeItemLabel}
              onPress={onRemove}
              disabled={isSaving || isRemoving}
              variant="ghost"
            />
            <Pressable onPress={onClose} style={({ pressed }) => [
              styles.editorCloseLink,
              isSaving || isRemoving ? styles.editorCloseLinkDisabled : null,
              pressed ? styles.editorCloseLinkPressed : null,
            ]} disabled={isSaving || isRemoving}>
              <Text style={styles.editorCloseText}>{closeLabel}</Text>
            </Pressable>
          </View>
        </View>
      </ScrollView>
    </>
  );
}
