import { useEffect, useRef, useState, type ReactNode } from 'react';
import {
  Animated as RNAnimated,
  Dimensions,
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
import { removeOverlayNode, setOverlayNode, useOverlayHostHeight } from './OverlayHost';
import { theme } from './theme';

type OverlaySheetProps = {
  children: ReactNode;
  onClose: () => void;
  sheetStyle?: StyleProp<ViewStyle>;
  aboveSheet?: ReactNode;
};

export function OverlaySheet({
  children,
  onClose,
  sheetStyle,
  aboveSheet,
}: OverlaySheetProps) {
  const overlayId = useRef(`overlay-sheet-${Math.random().toString(36).slice(2)}`).current;
  const screenHeight = Dimensions.get('window').height;
  const hostHeight = useOverlayHostHeight();
  const backdropOpacity = useRef(new RNAnimated.Value(0)).current;
  const sheetTranslateY = useRef(new RNAnimated.Value(28)).current;
  const { height } = useReanimatedKeyboardAnimation();
  const insets = useSafeAreaInsets();
  const viewportHeight = hostHeight
    ? hostHeight - insets.top
    : screenHeight;
  const [sheetHeight, setSheetHeight] = useState(0);

  const keyboardHeight = useAnimatedStyle(() => {
    return {
      paddingBottom: Math.max(0, -height.value),
    };
  });

  const sheetAnimatedStyle = useAnimatedStyle(() => {
    const keyboardOffset = Math.max(0, -height.value);
    const availableHeight = Math.max(0, viewportHeight - keyboardOffset);

    return {
      maxHeight: availableHeight,
    };
  });

  const aboveSheetAnimatedStyle = useAnimatedStyle(() => {
    return {
      bottom: sheetHeight + theme.spacing.sm,
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

  const overlayNode = (
    <View
      style={styles.root}
      pointerEvents="box-none"
    >
      <RNAnimated.View style={[styles.backdrop, { opacity: backdropOpacity }]}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
      </RNAnimated.View>

      <RNAnimated.View
        style={[
          {
            transform: [{ translateY: sheetTranslateY }],
          },
          styles.sheetRail,
        ]}
        pointerEvents="box-none"
      >
        <Animated.View style={[styles.sheetRail, keyboardHeight]} pointerEvents="box-none">
          {aboveSheet ? (
            <Animated.View style={[styles.aboveSheetFloating, aboveSheetAnimatedStyle]} pointerEvents="box-none">
              <View style={styles.aboveSheet}>{aboveSheet}</View>
            </Animated.View>
          ) : null}
          <Animated.View
            style={[styles.sheet, sheetStyle, sheetAnimatedStyle]}
            onLayout={(event) => {
              const nextHeight = event.nativeEvent.layout.height;
              setSheetHeight(nextHeight);
            }}
          >
            {children}
          </Animated.View>
        </Animated.View>
      </RNAnimated.View>
    </View>
  );

  useEffect(() => {
    setOverlayNode(overlayId, overlayNode);
  }, [overlayId, overlayNode]);

  useEffect(() => {
    return () => {
      removeOverlayNode(overlayId);
    };
  }, [overlayId]);

  return null;
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
    backgroundColor: theme.colors.scrim,
    zIndex: 1,
  },
  sheetRail: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'flex-end',
    zIndex: 2,
    elevation: 0,
    overflow: 'visible',
  },
  aboveSheet: {
    paddingHorizontal: theme.layout.sheetPadding,
    marginBottom: theme.spacing.sm,
  },
  aboveSheetFloating: {
    position: 'absolute',
    left: 0,
    right: 0,
    zIndex: 3,
    elevation: 0,
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    width: '100%',
    alignSelf: 'stretch',
    flexShrink: 1,
    minHeight: 0,
  },
});

