import { Text, View } from 'react-native';
import { AppButton, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type Props = {
  styles: any;
  title: string;
  subtitle: string;
  placeholder: string;
  createActionLabel: string;
  closeLabel: string;
  value: string;
  canCreate: boolean;
  onChangeText: (value: string) => void;
  onSubmitEditing: () => void | Promise<void>;
  onCreate: () => void | Promise<void>;
  onClose: () => void;
};

export function CreateListSheetContent({
  styles,
  title,
  subtitle,
  placeholder,
  createActionLabel,
  closeLabel,
  value,
  canCreate,
  onChangeText,
  onSubmitEditing,
  onCreate,
  onClose,
}: Props) {
  return (
    <>
      <View style={styles.sheetHandle} />
      <Text style={textStyles.h3}>{title}</Text>
      <Subtle>{subtitle}</Subtle>
      <AppInput
        placeholder={placeholder}
        value={value}
        onChangeText={onChangeText}
        onSubmitEditing={onSubmitEditing}
        returnKeyType="done"
        autoFocus
      />
      <View style={styles.sheetActions}>
        <AppButton title={createActionLabel} onPress={onCreate} disabled={!canCreate} fullWidth />
        <AppButton title={closeLabel} onPress={onClose} variant="ghost" fullWidth />
      </View>
    </>
  );
}
