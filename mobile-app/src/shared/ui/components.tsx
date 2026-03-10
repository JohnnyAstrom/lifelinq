import React from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  LayoutChangeEvent,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  type KeyboardTypeOptions,
  type RefreshControlProps,
  type StyleProp,
  type TextStyle,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { shadow, textStyles, theme } from './theme';

type ScreenProps = {
  children: React.ReactNode;
  contentStyle?: StyleProp<ViewStyle>;
  scroll?: boolean;
  refreshControl?: React.ReactElement<RefreshControlProps>;
  stickyHeaderIndices?: number[];
};

export function AppScreen({
  children,
  contentStyle,
  scroll = true,
  refreshControl,
  stickyHeaderIndices,
}: ScreenProps) {
  if (scroll) {
    return (
      <SafeAreaView style={styles.screen}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          refreshControl={refreshControl}
          stickyHeaderIndices={stickyHeaderIndices}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
        >
          <View style={[styles.contentContainer, contentStyle]}>
            {children}
          </View>
        </ScrollView>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <KeyboardAvoidingView
        style={styles.contentNoScroll}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <View style={styles.contentNoScroll}>
          <View style={[styles.contentContainer, styles.contentNoScroll, contentStyle]}>
            {children}
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

type CardProps = {
  children: React.ReactNode;
  style?: StyleProp<ViewStyle>;
};

export function AppCard({ children, style }: CardProps) {
  return <View style={[styles.card, style]}>{children}</View>;
}

type ButtonProps = {
  title: string;
  onPress: () => void;
  onLongPress?: () => void;
  variant?: 'primary' | 'secondary' | 'ghost';
  fullWidth?: boolean;
  disabled?: boolean;
};

export function AppButton({
  title,
  onPress,
  onLongPress,
  variant = 'primary',
  fullWidth,
  disabled,
}: ButtonProps) {
  return (
    <Pressable
      style={({ pressed }) => [
        styles.buttonBase,
        styles[`button_${variant}`],
        fullWidth ? styles.buttonFull : null,
        pressed ? styles.buttonPressed : null,
        disabled ? styles.buttonDisabled : null,
      ]}
      onPress={onPress}
      onLongPress={onLongPress}
      disabled={disabled}
    >
      <Text style={[styles.buttonText, styles[`buttonText_${variant}`]]}>
        {title}
      </Text>
    </Pressable>
  );
}

type ChipProps = {
  label: string;
  active?: boolean;
  onPress: () => void;
  style?: StyleProp<ViewStyle>;
  textStyle?: StyleProp<TextStyle>;
};

export function AppChip({ label, active, onPress, style, textStyle }: ChipProps) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.chip,
        style,
        active ? styles.chipActive : null,
        pressed ? styles.chipPressed : null,
      ]}
    >
      <Text style={[styles.chipText, textStyle, active ? styles.chipTextActive : null]}>
        {label}
      </Text>
    </Pressable>
  );
}

export function BackIconButton({ onPress }: { onPress: () => void }) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel="Back"
      style={({ pressed }) => [
        styles.iconButton,
        pressed ? styles.iconButtonPressed : null,
      ]}
    >
      <Ionicons name="arrow-back-circle-outline" size={29} color={theme.colors.textPrimary} />
    </Pressable>
  );
}

type InputProps = {
  value: string;
  placeholder?: string;
  onChangeText: (value: string) => void;
  multiline?: boolean;
  style?: StyleProp<TextStyle>;
  autoFocus?: boolean;
  blurOnSubmit?: boolean;
  keyboardType?: KeyboardTypeOptions;
  onFocus?: () => void;
  onBlur?: () => void;
  onSubmitEditing?: () => void;
  returnKeyType?: 'done' | 'next' | 'search' | 'send';
  showSoftInputOnFocus?: boolean;
  onLayout?: (event: LayoutChangeEvent) => void;
};

export const AppInput = React.forwardRef<TextInput, InputProps>(function AppInput({
  value,
  placeholder,
  onChangeText,
  multiline,
  style,
  autoFocus,
  blurOnSubmit,
  keyboardType,
  onFocus,
  onBlur,
  onSubmitEditing,
  returnKeyType,
  showSoftInputOnFocus,
  onLayout,
}, ref) {
  return (
    <TextInput
      ref={ref}
      value={value}
      placeholder={placeholder}
      onChangeText={onChangeText}
      multiline={multiline}
      autoFocus={autoFocus}
      blurOnSubmit={blurOnSubmit}
      keyboardType={keyboardType}
      onFocus={onFocus}
      onBlur={onBlur}
      onSubmitEditing={onSubmitEditing}
      returnKeyType={returnKeyType}
      showSoftInputOnFocus={showSoftInputOnFocus}
      onLayout={onLayout}
      style={[styles.input, style]}
      placeholderTextColor={theme.colors.textSecondary}
    />
  );
});

export function SectionTitle({ children }: { children: React.ReactNode }) {
  return <Text style={textStyles.h3}>{children}</Text>;
}

export function Subtle(
  { children, style }: { children: React.ReactNode; style?: StyleProp<TextStyle> }
) {
  return <Text style={[textStyles.subtle, style]}>{children}</Text>;
}

type TopBarProps = {
  title: string;
  subtitle?: string;
  left?: React.ReactNode;
  right?: React.ReactNode;
};

export function TopBar({ title, subtitle, left, right }: TopBarProps) {
  return (
    <View style={styles.topBar}>
      {left ? <View style={[styles.topBarSide, styles.topBarSideLeft]}>{left}</View> : null}
      <View style={styles.topBarCenter}>
        <Text style={textStyles.h2}>{title}</Text>
        {subtitle ? <Text style={textStyles.subtle}>{subtitle}</Text> : null}
      </View>
      {right ? <View style={[styles.topBarSide, styles.topBarSideRight]}>{right}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  scrollContent: {
    flexGrow: 1,
  },
  contentContainer: {
    width: '100%',
    maxWidth: theme.layout.maxContentWidth,
    alignSelf: 'center',
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.md,
    gap: theme.spacing.sm,
  },
  contentNoScroll: {
    flex: 1,
  },
  card: {
    backgroundColor: theme.colors.card,
    borderRadius: theme.radius.cardRadius,
    padding: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    ...shadow,
  },
  buttonBase: {
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    borderRadius: theme.radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button_primary: {
    backgroundColor: theme.colors.primary,
  },
  button_secondary: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  button_ghost: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  buttonText: {
    fontFamily: theme.typography.heading,
    fontSize: 14,
    fontWeight: '600',
  },
  buttonText_primary: {
    color: theme.colors.card,
  },
  buttonText_secondary: {
    color: theme.colors.textPrimary,
  },
  buttonText_ghost: {
    color: theme.colors.textPrimary,
  },
  buttonFull: {
    width: '100%',
  },
  buttonPressed: {
    opacity: 0.85,
    transform: [{ scale: 0.99 }],
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  iconButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  iconButtonPressed: {
    opacity: 0.75,
  },
  chip: {
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    borderRadius: theme.radius.pill,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceSubtle,
  },
  chipActive: {
    backgroundColor: theme.colors.primary,
    borderColor: theme.colors.primary,
  },
  chipPressed: {
    opacity: 0.8,
  },
  chipText: {
    fontFamily: theme.typography.body,
    fontSize: 13,
    color: theme.colors.textPrimary,
  },
  chipTextActive: {
    color: theme.colors.card,
  },
  input: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    fontFamily: theme.typography.body,
    fontSize: 15,
    backgroundColor: theme.colors.surfaceSubtle,
    color: theme.colors.textPrimary,
  },
  topBar: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.md,
    paddingHorizontal: theme.spacing.lg,
    backgroundColor: theme.colors.card,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    zIndex: 5,
  },
  topBarSide: {
    minWidth: 0,
  },
  topBarSideLeft: {
    marginRight: theme.spacing.sm,
  },
  topBarSideRight: {
    marginLeft: theme.spacing.sm,
    alignItems: 'flex-end',
  },
  topBarCenter: {
    flex: 1,
    gap: theme.spacing.xs,
  },
});

