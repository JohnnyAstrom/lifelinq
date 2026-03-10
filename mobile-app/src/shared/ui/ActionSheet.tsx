import type { ReactNode } from 'react';
import { Modal, Pressable, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { theme } from './theme';

type Presentation = 'compact' | 'standard' | 'large' | 'fullscreen';

type ActionSheetProps = {
  visible: boolean;
  onClose: () => void;
  presentation: Presentation;
  children: ReactNode;
};

export function ActionSheet({ visible, onClose, presentation, children }: ActionSheetProps) {
  const insets = useSafeAreaInsets();
  const isFullscreen = presentation === 'fullscreen';
  const minHeight =
    presentation === 'compact'
      ? '30%'
      : presentation === 'large'
        ? '68%'
        : '45%';

  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={onClose}
    >
      <View style={styles.root}>
        <Pressable style={styles.backdrop} onPress={onClose} />
        <View style={styles.container} pointerEvents="box-none">
          <Pressable
            style={[
              styles.sheet,
              isFullscreen ? styles.sheetFullscreen : styles.sheetStandard,
              !isFullscreen ? { minHeight } : null,
              { paddingBottom: insets.bottom + theme.spacing.md },
            ]}
            onPress={() => null}
          >
            {children}
          </Pressable>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: theme.colors.scrim,
  },
  container: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    paddingTop: theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
  },
  sheetStandard: {
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
  },
  sheetFullscreen: {
    height: '100%',
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
  },
});

