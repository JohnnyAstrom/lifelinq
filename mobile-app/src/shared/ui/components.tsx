import React from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  GestureResponderEvent,
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
import { type FeatureAccentKey, textStyles, theme } from './theme';

type ScreenProps = {
  children: React.ReactNode;
  header?: React.ReactNode;
  footer?: React.ReactNode;
  contentStyle?: StyleProp<ViewStyle>;
  scroll?: boolean;
  refreshControl?: React.ReactElement<RefreshControlProps>;
  stickyHeaderIndices?: number[];
};

function getTopLevelElementName(node: React.ReactNode): string | null {
  if (!React.isValidElement(node)) {
    return null;
  }

  if (typeof node.type === 'string') {
    return null;
  }

  if ('displayName' in node.type && typeof node.type.displayName === 'string') {
    return node.type.displayName;
  }

  if ('name' in node.type && typeof node.type.name === 'string') {
    return node.type.name;
  }

  return null;
}

function isOverlayChild(node: React.ReactNode): boolean {
  const name = getTopLevelElementName(node);
  if (!name) {
    return false;
  }

  return name.endsWith('Sheet') || name.endsWith('Modal');
}

export function AppScreen({
  children,
  header,
  footer,
  contentStyle,
  scroll = true,
  refreshControl,
  stickyHeaderIndices,
}: ScreenProps) {
  const allChildren = React.Children.toArray(children);
  const contentChildren = allChildren.filter((child) => !isOverlayChild(child));
  const overlayChildren = allChildren.filter((child) => isOverlayChild(child));

  if (scroll) {
    return (
      <View style={styles.screenRoot}>
        <SafeAreaView style={styles.screen}>
          {header ? <View style={styles.edgeSlot}>{header}</View> : null}
          <ScrollView
            style={styles.scrollView}
            contentContainerStyle={styles.scrollContent}
            refreshControl={refreshControl}
            stickyHeaderIndices={stickyHeaderIndices}
            keyboardShouldPersistTaps="handled"
            keyboardDismissMode="on-drag"
          >
            <View style={[styles.contentContainer, contentStyle]}>
              {contentChildren}
            </View>
          </ScrollView>
          {footer ? <View style={styles.edgeSlot}>{footer}</View> : null}
        </SafeAreaView>
        {overlayChildren}
      </View>
    );
  }

  return (
    <View style={styles.screenRoot}>
      <SafeAreaView style={styles.screen}>
        {header ? <View style={styles.edgeSlot}>{header}</View> : null}
        <KeyboardAvoidingView
          style={styles.contentNoScroll}
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        >
          <View style={styles.contentNoScroll}>
            <View style={[styles.contentContainer, styles.contentNoScroll, contentStyle]}>
              {contentChildren}
            </View>
            {footer ? <View style={styles.edgeSlot}>{footer}</View> : null}
          </View>
        </KeyboardAvoidingView>
      </SafeAreaView>
      {overlayChildren}
    </View>
  );
}

type CardProps = {
  children: React.ReactNode;
  style?: StyleProp<ViewStyle>;
};

export function AppCard({ children, style }: CardProps) {
  return <View style={[styles.card, style]}>{children}</View>;
}

type RowProps = {
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  leading?: React.ReactNode;
  trailing?: React.ReactNode;
  onPress?: () => void;
  onLongPress?: (event: GestureResponderEvent) => void;
  disabled?: boolean;
  style?: StyleProp<ViewStyle>;
  contentStyle?: StyleProp<ViewStyle>;
  titleStyle?: StyleProp<TextStyle>;
  subtitleStyle?: StyleProp<TextStyle>;
  accessibilityRole?: 'button' | 'switch' | 'link';
};

export function AppRow({
  title,
  subtitle,
  leading,
  trailing,
  onPress,
  onLongPress,
  disabled,
  style,
  contentStyle,
  titleStyle,
  subtitleStyle,
  accessibilityRole = 'button',
}: RowProps) {
  const body = (
    <>
      {leading ? <View style={styles.rowLeading}>{leading}</View> : null}
      <View style={[styles.rowContent, contentStyle]}>
        {typeof title === 'string'
          ? <Text style={[styles.rowTitle, titleStyle]}>{title}</Text>
          : title}
        {subtitle
          ? (typeof subtitle === 'string'
            ? <Text style={[styles.rowSubtitle, subtitleStyle]}>{subtitle}</Text>
            : subtitle)
          : null}
      </View>
      {trailing ? <View style={styles.rowTrailing}>{trailing}</View> : null}
    </>
  );

  if (!onPress && !onLongPress) {
    return <View style={[styles.rowBase, style]}>{body}</View>;
  }

  return (
    <Pressable
      onPress={onPress}
      onLongPress={onLongPress}
      disabled={disabled}
      accessibilityRole={accessibilityRole}
      style={({ pressed }) => [
        styles.rowBase,
        style,
        disabled ? styles.rowDisabled : null,
        pressed ? styles.rowPressed : null,
      ]}
    >
      {body}
    </Pressable>
  );
}

type ButtonProps = {
  title: string;
  onPress: () => void;
  onLongPress?: () => void;
  variant?: 'primary' | 'secondary' | 'ghost';
  fullWidth?: boolean;
  disabled?: boolean;
  accentKey?: FeatureAccentKey;
  accentColor?: string;
};

export function AppButton({
  title,
  onPress,
  onLongPress,
  variant = 'primary',
  fullWidth,
  disabled,
  accentKey,
  accentColor,
}: ButtonProps) {
  const resolvedAccentColor = accentColor ?? (accentKey ? theme.colors.feature[accentKey] : null);
  return (
    <Pressable
      style={({ pressed }) => [
        styles.buttonBase,
        styles[`button_${variant}`],
        variant === 'primary' && resolvedAccentColor ? { backgroundColor: resolvedAccentColor } : null,
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
  accentKey?: FeatureAccentKey;
  accentColor?: string;
};

export function AppChip({ label, active, onPress, style, textStyle, accentKey, accentColor }: ChipProps) {
  const resolvedAccentColor = accentColor ?? (accentKey ? theme.colors.feature[accentKey] : null);
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.chip,
        style,
        active
          ? (resolvedAccentColor
            ? { backgroundColor: resolvedAccentColor, borderColor: resolvedAccentColor }
            : styles.chipActive)
          : null,
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
  editable?: boolean;
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
  editable = true,
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
        editable={editable}
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
  return <Text style={textStyles.h2}>{children}</Text>;
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
  accentKey?: FeatureAccentKey;
  icon?: React.ReactNode;
};

export function TopBar({ title, subtitle, left, right, accentKey, icon }: TopBarProps) {
  const iconColor = accentKey ? theme.colors.feature[accentKey] : theme.colors.textSecondary;
  const renderedIcon = React.isValidElement(icon)
    ? React.cloneElement(icon as React.ReactElement<any>, {
        color: (icon as React.ReactElement<any>).props.color ?? iconColor,
        size: (icon as React.ReactElement<any>).props.size ?? 20,
      })
    : icon;
  return (
    <View>
      <View style={styles.topBar}>
        {left ? <View style={[styles.topBarSide, styles.topBarSideLeft]}>{left}</View> : null}
        <View style={styles.topBarCenter}>
          <View style={styles.topBarTitleRow}>
            {renderedIcon ? <View style={styles.topBarIcon}>{renderedIcon}</View> : null}
            <Text style={textStyles.h2}>{title}</Text>
          </View>
          {subtitle ? <Text style={textStyles.subtle}>{subtitle}</Text> : null}
        </View>
        {right ? <View style={[styles.topBarSide, styles.topBarSideRight]}>{right}</View> : null}
      </View>
      {accentKey ? (
        <View style={[styles.topBarAccent, { backgroundColor: theme.colors.feature[accentKey] }]} />
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  screenRoot: {
    flex: 1,
  },
  screen: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  scrollContent: {
    flexGrow: 1,
  },
  scrollView: {
    flex: 1,
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
  edgeSlot: {
    width: '100%',
  },
  card: {
    backgroundColor: theme.colors.card,
    borderRadius: theme.radius.cardRadius,
    padding: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    ...theme.elevation.card,
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
  topBarTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  topBarIcon: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  topBarAccent: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    height: 2,
    zIndex: 6,
  },
  rowBase: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  rowLeading: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowContent: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  rowTitle: {
    ...textStyles.body,
  },
  rowSubtitle: {
    ...textStyles.subtle,
  },
  rowTrailing: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  rowPressed: {
    opacity: 0.8,
  },
  rowDisabled: {
    opacity: 0.5,
  },
});

