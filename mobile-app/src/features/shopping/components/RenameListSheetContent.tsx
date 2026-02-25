import { Text, View } from 'react-native';
import { AppButton, AppInput } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type Props = {
  styles: any;
  title: string;
  placeholder: string;
  saveLabel: string;
  closeLabel: string;
  value: string;
  onChangeText: (value: string) => void;
  onSave: () => void | Promise<void>;
  onClose: () => void;
};

export function RenameListSheetContent({
  styles,
  title,
  placeholder,
  saveLabel,
  closeLabel,
  value,
  onChangeText,
  onSave,
  onClose,
}: Props) {
  return (
    <>
      <View style={styles.sheetHandle} />
      <Text style={textStyles.h3}>{title}</Text>
      <AppInput placeholder={placeholder} value={value} onChangeText={onChangeText} autoFocus />
      <View style={styles.sheetActions}>
        <AppButton title={saveLabel} onPress={onSave} fullWidth />
        <AppButton title={closeLabel} onPress={onClose} variant="ghost" fullWidth />
      </View>
    </>
  );
}
