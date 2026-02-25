import { Pressable, Text, View } from 'react-native';
import { AppButton } from '../../../shared/ui/components';

type Props = {
  styles: any;
  placeholder: string;
  actionTitle: string;
  onPressInput: () => void;
  onPressAction: () => void;
};

export function ShoppingAddBar({
  styles,
  placeholder,
  actionTitle,
  onPressInput,
  onPressAction,
}: Props) {
  return (
    <View style={styles.bottomContainer}>
      <View style={styles.bottomBar}>
        <Pressable style={styles.bottomInputPressable} onPress={onPressInput}>
          <Text style={styles.bottomInputPlaceholder}>{placeholder}</Text>
        </Pressable>
        <AppButton title={actionTitle} onPress={onPressAction} />
      </View>
    </View>
  );
}
