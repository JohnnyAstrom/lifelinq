import React from 'react';
import {
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  type StyleProp,
  type TextStyle,
  type ViewStyle,
} from 'react-native';
import { shadow, textStyles, theme } from './theme';

type ScreenProps = {
  children: React.ReactNode;
  contentStyle?: StyleProp<ViewStyle>;
  scroll?: boolean;
  refreshControl?: React.ReactElement;
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
        <View style={styles.decorOne} />
        <View style={styles.decorTwo} />
        <ScrollView
          contentContainerStyle={[styles.content, contentStyle]}
          refreshControl={refreshControl}
          stickyHeaderIndices={stickyHeaderIndices}
        >
          {children}
        </ScrollView>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <View style={styles.decorOne} />
      <View style={styles.decorTwo} />
      <View style={[styles.content, styles.contentNoScroll, contentStyle]}>
        {children}
      </View>
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
};

export function AppChip({ label, active, onPress }: ChipProps) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.chip,
        active ? styles.chipActive : null,
        pressed ? styles.chipPressed : null,
      ]}
    >
      <Text style={[styles.chipText, active ? styles.chipTextActive : null]}>
        {label}
      </Text>
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
};

export function AppInput({
  value,
  placeholder,
  onChangeText,
  multiline,
  style,
  autoFocus,
}: InputProps) {
  return (
    <TextInput
      value={value}
      placeholder={placeholder}
      onChangeText={onChangeText}
      multiline={multiline}
      autoFocus={autoFocus}
      style={[styles.input, style]}
      placeholderTextColor={theme.colors.subtle}
    />
  );
}

export function SectionTitle({ children }: { children: React.ReactNode }) {
  return <Text style={textStyles.h3}>{children}</Text>;
}

export function Subtle({ children }: { children: React.ReactNode }) {
  return <Text style={textStyles.subtle}>{children}</Text>;
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: theme.colors.bg,
  },
  content: {
    padding: theme.spacing.lg,
    gap: theme.spacing.md,
  },
  contentNoScroll: {
    flex: 1,
  },
  decorOne: {
    position: 'absolute',
    top: -120,
    right: -80,
    width: 220,
    height: 220,
    borderRadius: 110,
    backgroundColor: theme.colors.accentSoft,
    opacity: 0.5,
  },
  decorTwo: {
    position: 'absolute',
    bottom: -140,
    left: -90,
    width: 260,
    height: 260,
    borderRadius: 130,
    backgroundColor: theme.colors.primarySoft,
    opacity: 0.6,
  },
  card: {
    backgroundColor: theme.colors.surface,
    borderRadius: theme.radius.lg,
    padding: theme.spacing.lg,
    borderWidth: 1,
    borderColor: theme.colors.border,
    ...shadow,
  },
  buttonBase: {
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button_primary: {
    backgroundColor: theme.colors.primary,
  },
  button_secondary: {
    backgroundColor: theme.colors.surfaceAlt,
    borderWidth: 1,
    borderColor: theme.colors.borderStrong,
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
    color: '#ffffff',
  },
  buttonText_secondary: {
    color: theme.colors.text,
  },
  buttonText_ghost: {
    color: theme.colors.text,
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
  chip: {
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceAlt,
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
    color: theme.colors.text,
  },
  chipTextActive: {
    color: '#ffffff',
  },
  input: {
    borderWidth: 1,
    borderColor: theme.colors.borderStrong,
    borderRadius: theme.radius.md,
    paddingVertical: 10,
    paddingHorizontal: 12,
    fontFamily: theme.typography.body,
    fontSize: 15,
    backgroundColor: theme.colors.surfaceAlt,
    color: theme.colors.text,
  },
});
