import { useEffect, useRef } from 'react';
import { Animated, Modal, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { theme } from './theme';

type Props = {
  message: string | null;
  toastKey: number;
  onDone?: () => void;
  durationMs?: number;
};

export function AppToast({ message, toastKey, onDone, durationMs = 2000 }: Props) {
  const opacity = useRef(new Animated.Value(0)).current;
  const insets = useSafeAreaInsets();

  useEffect(() => {
    if (!message) {
      return;
    }
    opacity.setValue(0);
    Animated.timing(opacity, {
      toValue: 1,
      duration: 180,
      useNativeDriver: true,
    }).start();

    const hideTimer = setTimeout(() => {
      Animated.timing(opacity, {
        toValue: 0,
        duration: 220,
        useNativeDriver: true,
      }).start(({ finished }) => {
        if (finished) {
          onDone?.();
        }
      });
    }, durationMs);

    return () => {
      clearTimeout(hideTimer);
    };
  }, [durationMs, message, onDone, opacity, toastKey]);

  if (!message) {
    return null;
  }

  return (
    <Modal visible transparent animationType="none" onRequestClose={() => undefined}>
      <View pointerEvents="none" style={[styles.host, { bottom: insets.bottom + 20 }]}>
        <Animated.View style={[styles.toast, { opacity }]}>
          <Text style={styles.text}>{message}</Text>
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  host: {
    position: 'absolute',
    left: 0,
    right: 0,
    alignItems: 'center',
    zIndex: 10000,
    elevation: 10000,
  },
  toast: {
    maxWidth: '88%',
    backgroundColor: 'rgba(20,20,24,0.92)',
    borderRadius: theme.radius.cardRadius,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
  },
  text: {
    color: theme.colors.card,
    fontSize: 14,
    fontFamily: theme.typography.body,
  },
});
