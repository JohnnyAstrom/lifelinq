import { useEffect, useRef, type ReactNode } from 'react';
import {
  Animated,
  Easing,
  Keyboard,
  Platform,
  Pressable,
  StyleSheet,
  View,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import { theme } from './theme';

type OverlaySheetProps = {
  children: ReactNode;
  onClose: () => void;
  sheetStyle?: StyleProp<ViewStyle>;
  aboveSheet?: ReactNode;
};

export function OverlaySheet({ children, onClose, sheetStyle, aboveSheet }: OverlaySheetProps) {
  const backdropOpacity = useRef(new Animated.Value(0)).current;
  const sheetTranslateY = useRef(new Animated.Value(28)).current;
  const keyboardInset = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(backdropOpacity, {
        toValue: 1,
        duration: 70,
        easing: Easing.out(Easing.quad),
        useNativeDriver: false,
      }),
      Animated.timing(sheetTranslateY, {
        toValue: 0,
        duration: 85,
        easing: Easing.out(Easing.quad),
        useNativeDriver: false,
      }),
    ]).start();
  }, [backdropOpacity, sheetTranslateY]);

  useEffect(() => {
    const showEvent = Platform.OS === 'ios' ? 'keyboardWillShow' : 'keyboardDidShow';
    const hideEvent = Platform.OS === 'ios' ? 'keyboardWillHide' : 'keyboardDidHide';

    const showSub = Keyboard.addListener(showEvent, (event) => {
      Animated.timing(keyboardInset, {
        toValue: event.endCoordinates?.height ?? 0,
        duration: 50,
        easing: Easing.linear,
        useNativeDriver: false,
      }).start();
    });

    const hideSub = Keyboard.addListener(hideEvent, () => {
      Animated.timing(keyboardInset, {
        toValue: 0,
        duration: 50,
        easing: Easing.linear,
        useNativeDriver: false,
      }).start();
    });

    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, [keyboardInset]);

  return (
    <View style={styles.root} pointerEvents="box-none">
      <Animated.View style={[styles.backdrop, { opacity: backdropOpacity }]}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
      </Animated.View>

      <Animated.View
        style={[
          styles.sheetContainer,
          {
            paddingBottom: keyboardInset,
            transform: [{ translateY: sheetTranslateY }],
          },
        ]}
        pointerEvents="box-none"
      >
        {aboveSheet ? <View style={styles.aboveSheet}>{aboveSheet}</View> : null}
        <View style={[styles.sheet, sheetStyle]}>{children}</View>
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 9999,
    elevation: 9999,
    position: 'absolute',
    overflow: 'visible',
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.4)',
    zIndex: 1,
  },
  sheetContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    zIndex: 2,
    elevation: 2,
    overflow: 'visible',
  },
  aboveSheet: {
    paddingHorizontal: theme.spacing.lg,
    marginBottom: theme.spacing.sm,
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    width: '100%',
    alignSelf: 'stretch',
  },
});
