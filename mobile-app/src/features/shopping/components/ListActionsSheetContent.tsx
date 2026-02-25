import { Text, View } from 'react-native';
import { AppButton } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type Props = {
  styles: any;
  title: string;
  shareLabel: string;
  editNameLabel: string;
  deleteLabel: string;
  closeLabel: string;
  onShare: () => void | Promise<void>;
  onEditName: () => void;
  onDelete: () => void;
  onClose: () => void;
};

export function ListActionsSheetContent({
  styles,
  title,
  shareLabel,
  editNameLabel,
  deleteLabel,
  closeLabel,
  onShare,
  onEditName,
  onDelete,
  onClose,
}: Props) {
  return (
    <>
      <View style={styles.sheetHandle} />
      <Text style={textStyles.h3}>{title}</Text>
      <View style={styles.sheetActions}>
        <AppButton title={shareLabel} onPress={() => void onShare()} fullWidth />
        <AppButton title={editNameLabel} onPress={onEditName} variant="secondary" fullWidth />
        <AppButton title={deleteLabel} onPress={onDelete} variant="ghost" fullWidth />
        <AppButton title={closeLabel} onPress={onClose} variant="ghost" fullWidth />
      </View>
    </>
  );
}
