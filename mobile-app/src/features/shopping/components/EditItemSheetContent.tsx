import { ScrollView, Text, View } from 'react-native';
import { AppButton, AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type UnitOption = { label: string; value: string };

type Props = {
  styles: any;
  title: string;
  editNamePlaceholder: string;
  editQuantityPlaceholder: string;
  saveChangesLabel: string;
  removeItemLabel: string;
  closeLabel: string;
  unitNoneLabel: string;
  unitToggleMoreLabel: string;
  unitToggleLessLabel: string;
  nameValue: string;
  quantityValue: string;
  editUnit: string | null;
  editError: string | null;
  showMoreEditUnits: boolean;
  primaryUnitOptions: UnitOption[];
  moreUnitOptions: UnitOption[];
  onChangeName: (value: string) => void;
  onChangeQuantity: (value: string) => void;
  onSelectUnit: (value: string | null) => void;
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
  saveChangesLabel,
  removeItemLabel,
  closeLabel,
  unitNoneLabel,
  unitToggleMoreLabel,
  unitToggleLessLabel,
  nameValue,
  quantityValue,
  editUnit,
  editError,
  showMoreEditUnits,
  primaryUnitOptions,
  moreUnitOptions,
  onChangeName,
  onChangeQuantity,
  onSelectUnit,
  onToggleMoreUnits,
  onSave,
  onRemove,
  onClose,
}: Props) {
  return (
    <>
      <View style={styles.sheetHandle} />
      <ScrollView
        style={styles.editorScroll}
        contentContainerStyle={styles.editorScrollContent}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.quickAddHeader}>
          <Text style={textStyles.h3}>{title}</Text>
        </View>
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
              onPress={() => onSelectUnit(unit.value)}
            />
          ))}
          <AppChip label={unitNoneLabel} active={!editUnit} onPress={() => onSelectUnit(null)} />
          <AppChip
            label={showMoreEditUnits ? unitToggleLessLabel : unitToggleMoreLabel}
            active={showMoreEditUnits}
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
                onPress={() => onSelectUnit(unit.value)}
              />
            ))}
          </View>
        ) : null}
        {editError ? <Text style={styles.error}>{editError}</Text> : null}
        <View style={styles.editorActions}>
          <AppButton title={saveChangesLabel} onPress={onSave} fullWidth />
          <AppButton title={removeItemLabel} onPress={onRemove} variant="ghost" fullWidth />
          <AppButton title={closeLabel} onPress={onClose} variant="secondary" fullWidth />
        </View>
      </ScrollView>
    </>
  );
}
