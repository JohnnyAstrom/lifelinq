import { Text, View } from 'react-native';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';
import type { ShoppingListType } from '../api/shoppingApi';

type Props = {
  styles: any;
  title: string;
  subtitle: string;
  typeLabel: string;
  selectedType: ShoppingListType;
  typeOptions: { key: ShoppingListType; label: string }[];
  placeholder: string;
  createActionLabel: string;
  createActionPendingLabel: string;
  closeLabel: string;
  value: string;
  canCreate: boolean;
  isSubmitting: boolean;
  onChangeText: (value: string) => void;
  onSelectType: (type: ShoppingListType) => void;
  onSubmitEditing: () => void | Promise<void>;
  onCreate: () => void | Promise<void>;
  onClose: () => void;
};

export function CreateListSheetContent({
  styles,
  title,
  subtitle,
  typeLabel,
  selectedType,
  typeOptions,
  placeholder,
  createActionLabel,
  createActionPendingLabel,
  closeLabel,
  value,
  canCreate,
  isSubmitting,
  onChangeText,
  onSelectType,
  onSubmitEditing,
  onCreate,
  onClose,
}: Props) {
  return (
    <>
      <Text style={textStyles.h2}>{title}</Text>
      <Subtle>{subtitle}</Subtle>
      <View style={styles.sheetTypeSection}>
        <Text style={styles.sheetTypeLabel}>{typeLabel}</Text>
        <View style={styles.sheetTypeOptions}>
          {typeOptions.map((option) => (
            <AppChip
              key={option.key}
              label={option.label}
              active={selectedType === option.key}
              onPress={() => onSelectType(option.key)}
              accentKey="shopping"
            />
          ))}
        </View>
      </View>
      <AppInput
        placeholder={placeholder}
        value={value}
        onChangeText={onChangeText}
        onSubmitEditing={onSubmitEditing}
        returnKeyType="done"
        autoFocus
      />
      <View style={styles.sheetActions}>
        <AppButton
          title={isSubmitting ? createActionPendingLabel : createActionLabel}
          onPress={onCreate}
          disabled={!canCreate || isSubmitting}
          fullWidth
          accentKey="shopping"
        />
        <AppButton title={closeLabel} onPress={onClose} variant="ghost" fullWidth disabled={isSubmitting} />
      </View>
    </>
  );
}
