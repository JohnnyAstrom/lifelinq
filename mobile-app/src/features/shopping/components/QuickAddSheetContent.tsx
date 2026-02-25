import { Text, View } from 'react-native';
import { AppInput } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type Props = {
  styles: any;
  title: string;
  placeholder: string;
  value: string;
  inputRef: any;
  onChangeText: (value: string) => void;
  onSubmitEditing: () => void | Promise<void>;
};

export function QuickAddSheetContent({
  styles,
  title,
  placeholder,
  value,
  inputRef,
  onChangeText,
  onSubmitEditing,
}: Props) {
  return (
    <>
      <View style={styles.sheetHandle} />
      <View style={styles.quickAddHeader}>
        <Text style={textStyles.h3}>{title}</Text>
      </View>
      <AppInput
        ref={inputRef}
        placeholder={placeholder}
        value={value}
        onChangeText={onChangeText}
        autoFocus
        blurOnSubmit={false}
        onSubmitEditing={onSubmitEditing}
        returnKeyType="done"
      />
    </>
  );
}
