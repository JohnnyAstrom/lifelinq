import { useEffect, useRef, type ReactNode } from 'react';
import {
  Animated as RNAnimated,
  Dimensions,
  Easing,
  LayoutChangeEvent,
  Pressable,
  StyleSheet,
  View,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import Animated, { runOnJS, useAnimatedStyle, useDerivedValue } from 'react-native-reanimated';
import { useReanimatedKeyboardAnimation } from 'react-native-keyboard-controller';
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
  const lastKeyboardHeightRef = useRef<number | null>(null);
  const lastTranslateYRef = useRef<number | null>(null);

  const logKeyboardHeight = (value: number) => {
    if (lastKeyboardHeightRef.current === value) {
      return;
    }
    lastKeyboardHeightRef.current = value;
    console.log(`[OverlaySheet][diag][keyboard] height=${value} ts=${Date.now()}`);
  };

  const logTranslateY = (value: number) => {
    if (lastTranslateYRef.current === value) {
      return;
    }
    lastTranslateYRef.current = value;
    console.log(`[OverlaySheet][diag][translateY] value=${value} ts=${Date.now()}`);
  };

  const onRootLayout = (event: LayoutChangeEvent) => {
    console.log(
      `[OverlaySheet][diag][layout] rootHeight=${event.nativeEvent.layout.height} ts=${Date.now()}`
    );
  };

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        {
          translateY: height.value,
        },
      ],
    };
  });

  useDerivedValue(() => {
    runOnJS(logKeyboardHeight)(height.value);
  }, [height]);

  useDerivedValue(() => {
    runOnJS(logTranslateY)(height.value);
  }, [height]);

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

  useEffect(() => {
    const subscription = Dimensions.addEventListener('change', ({ window }) => {
      console.log(
        `[OverlaySheet][diag][dimensions] windowHeight=${window.height} ts=${Date.now()}`
      );
    });
    return () => {
      subscription.remove();
    };
  }, []);

  return (
    <View style={styles.root} pointerEvents="box-none" onLayout={onRootLayout}>
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
        <Animated.View style={[styles.overlaySheet, animatedStyle]} pointerEvents="box-none">
          {aboveSheet ? <View style={styles.aboveSheet}>{aboveSheet}</View> : null}
          <View style={[styles.sheet, sheetStyle]}>{children}</View>
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
