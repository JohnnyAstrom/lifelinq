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
  const screenHeight = Dimensions.get('window').height;
  const backdropOpacity = useRef(new RNAnimated.Value(0)).current;
  const sheetTranslateY = useRef(new RNAnimated.Value(28)).current;
  const { height } = useReanimatedKeyboardAnimation();
  const insets = useSafeAreaInsets();
  const [sheetHeight, setSheetHeight] = useState(0);

  const sheetAnimatedStyle = useAnimatedStyle(() => {
    return {
      bottom: -height.value - insets.bottom,
    };
  });

  const aboveSheetAnimatedStyle = useAnimatedStyle(() => {
    return {
      bottom: sheetHeight + theme.spacing.sm - height.value - insets.bottom,
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

  useEffect(() => {
    console.log('[OverlaySheet]', {
      screenHeight,
      sheetHeight,
      insetsTop: insets.top,
      insetsBottom: insets.bottom,
      hasAboveSheet: !!aboveSheet,
    });
  }, [aboveSheet, insets.bottom, insets.top, screenHeight, sheetHeight]);

  return (
    <View
      style={styles.root}
      pointerEvents="box-none"
      onLayout={(event) => {
        console.log('[OverlaySheet] root layout', event.nativeEvent.layout);
      }}
    >
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
          {aboveSheet ? (
            <Animated.View style={[styles.aboveSheetFloating, aboveSheetAnimatedStyle]} pointerEvents="box-none">
              <View style={styles.aboveSheet}>{aboveSheet}</View>
            </Animated.View>
          ) : null}
          <Animated.View
            style={[styles.sheet, sheetStyle, { maxHeight: screenHeight - insets.top }, sheetAnimatedStyle]}
            onLayout={(event) => {
              const nextHeight = event.nativeEvent.layout.height;
              console.log('[OverlaySheet] sheet layout', event.nativeEvent.layout);
              setSheetHeight(nextHeight);
            }}
          >
            {children}
          </Animated.View>
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
    backgroundColor: theme.colors.scrim,
    zIndex: 1,
  },
  overlaySheet: {
    flex: 1,
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
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    width: '100%',
    alignSelf: 'stretch',
  },
});

