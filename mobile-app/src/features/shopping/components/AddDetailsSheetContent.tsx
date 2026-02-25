import { Keyboard, Text, View } from 'react-native';
import { AppButton, AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type UnitOption = { label: string; value: string };

type Props = {
  styles: any;
  title: string;
  addPlaceholderExtended: string;
  addQuantityPlaceholder: string;
  addItemTitle: string;
  closeLabel: string;
  unitNoneLabel: string;
  unitToggleMoreLabel: string;
  unitToggleLessLabel: string;
  nameValue: string;
  quantityValue: string;
  addUnit: string | null;
  addError: string | null;
  showMoreAddUnits: boolean;
  inputRef: any;
  quantityRef: any;
  primaryUnitOptions: UnitOption[];
  moreUnitOptions: UnitOption[];
  onChangeName: (value: string) => void;
  onSubmitName: () => void | Promise<void>;
  onChangeQuantity: (value: string) => void;
  onSelectUnit: (value: string | null) => void;
  onToggleMoreUnits: () => void;
  onAddItem: () => void | Promise<void>;
  onClose: () => void;
};

export function AddDetailsSheetContent({
  styles,
  title,
  addPlaceholderExtended,
  addQuantityPlaceholder,
  addItemTitle,
  closeLabel,
  unitNoneLabel,
  unitToggleMoreLabel,
  unitToggleLessLabel,
  nameValue,
  quantityValue,
  addUnit,
  addError,
  showMoreAddUnits,
  inputRef,
  quantityRef,
  primaryUnitOptions,
  moreUnitOptions,
  onChangeName,
  onSubmitName,
  onChangeQuantity,
  onSelectUnit,
  onToggleMoreUnits,
  onAddItem,
  onClose,
}: Props) {
  return (
    <>
      <View style={styles.sheetHandle} />
      <View style={styles.quickAddHeader}>
        <Text style={textStyles.h3}>{title}</Text>
      </View>
      <AppInput
        ref={inputRef}
        placeholder={addPlaceholderExtended}
        value={nameValue}
        onChangeText={onChangeName}
        onSubmitEditing={onSubmitName}
        returnKeyType="done"
      />
      <AppInput
        ref={quantityRef}
        value={quantityValue}
        onChangeText={onChangeQuantity}
        placeholder={addQuantityPlaceholder}
        keyboardType="decimal-pad"
        style={styles.addQuantityInput}
      />
      <View style={styles.addUnitRow}>
        {primaryUnitOptions.map((unit) => (
          <AppChip
            key={unit.value}
            label={unit.label}
            active={addUnit === unit.value}
            onPress={() => {
              Keyboard.dismiss();
              onSelectUnit(unit.value);
            }}
          />
        ))}
        <AppChip
          label={unitNoneLabel}
          active={!addUnit}
          onPress={() => {
            Keyboard.dismiss();
            onSelectUnit(null);
          }}
        />
        <AppChip
          label={showMoreAddUnits ? unitToggleLessLabel : unitToggleMoreLabel}
          active={showMoreAddUnits}
          onPress={onToggleMoreUnits}
        />
      </View>
      {showMoreAddUnits ? (
        <View style={styles.addUnitRow}>
          {moreUnitOptions.map((unit) => (
            <AppChip
              key={unit.value}
              label={unit.label}
              active={addUnit === unit.value}
              onPress={() => {
                Keyboard.dismiss();
                onSelectUnit(unit.value);
              }}
            />
          ))}
        </View>
      ) : null}
      {addError ? <Text style={styles.error}>{addError}</Text> : null}
      <View style={styles.sheetActions}>
        <AppButton
          title={addItemTitle}
          onPress={onAddItem}
          disabled={nameValue.trim().length === 0}
          fullWidth
        />
        <AppButton title={closeLabel} onPress={onClose} variant="ghost" fullWidth />
      </View>
    </>
  );
}
