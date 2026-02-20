import { useEffect, useRef, type ReactNode } from 'react';
import {
  Animated as RNAnimated,
  Easing,
  Pressable,
  StyleSheet,
  View,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import Animated, { useAnimatedStyle } from 'react-native-reanimated';
import { useReanimatedKeyboardAnimation } from 'react-native-keyboard-controller';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { theme } from './theme';

type OverlaySheetProps = {
  children: ReactNode;
  onClose: () => void;
  sheetStyle?: StyleProp<ViewStyle>;
  aboveSheet?: ReactNode;
};

export function OverlaySheet({ children, onClose, sheetStyle, aboveSheet }: OverlaySheetProps) {
  const backdropOpacity = useRef(new RNAnimated.Value(0)).current;
  const sheetTranslateY = useRef(new RNAnimated.Value(28)).current;
  const { height } = useReanimatedKeyboardAnimation();
  const insets = useSafeAreaInsets();

  const animatedStyle = useAnimatedStyle(() => {
    return {
      bottom: -height.value - insets.bottom,
    };
  });

  useEffect(() => {
    RNAnimated.parallel([
      RNAnimated.timing(backdropOpacity, {
        toValue: 1,
        duration: 70,
        easing: Easing.out(Easing.quad),
        useNativeDriver: false,
      }),
      RNAnimated.timing(sheetTranslateY, {
        toValue: 0,
        duration: 85,
        easing: Easing.out(Easing.quad),
        useNativeDriver: false,
      }),
    ]).start();
  }, [backdropOpacity, sheetTranslateY]);

  return (
    <View style={styles.root} pointerEvents="box-none">
      <RNAnimated.View style={[styles.backdrop, { opacity: backdropOpacity }]}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
      </RNAnimated.View>

      <RNAnimated.View
        style={[
          {
            transform: [{ translateY: sheetTranslateY }],
          },
          styles.overlaySheet,
        ]}
        pointerEvents="box-none"
      >
        <Animated.View style={styles.overlaySheet} pointerEvents="box-none">
          {aboveSheet ? <View style={styles.aboveSheet}>{aboveSheet}</View> : null}
          <Animated.View style={[styles.sheet, sheetStyle, animatedStyle]}>{children}</Animated.View>
        </Animated.View>
      </RNAnimated.View>
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
  overlaySheet: {
    flex: 1,
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
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    width: '100%',
    alignSelf: 'stretch',
  },
});
