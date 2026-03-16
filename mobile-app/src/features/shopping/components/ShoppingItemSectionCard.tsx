import { type ReactNode } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  title: string;
  countLabel?: string | null;
  variant?: 'open' | 'bought';
  hint?: string | null;
  emptyState?: string | null;
  actionLabel?: string;
  onActionPress?: () => void;
  children?: ReactNode;
};

export function ShoppingItemSectionCard({
  title,
  countLabel,
  variant = 'open',
  hint,
  emptyState,
  actionLabel,
  onActionPress,
  children,
}: Props) {
  const isBought = variant === 'bought';
  const hasContent = !!children;

  return (
    <View style={[styles.section, isBought ? styles.sectionBought : null]}>
      <View style={styles.header}>
        <View style={styles.headerMain}>
          <Text style={[styles.title, isBought ? styles.titleBought : null]}>{title}</Text>
          {hint ? (
            <Text style={[styles.hint, isBought ? styles.hintBought : null]}>{hint}</Text>
          ) : null}
        </View>
        {countLabel || (actionLabel && onActionPress) ? (
          <View style={styles.headerAside}>
            {countLabel ? (
              <Text style={[styles.countLabel, isBought ? styles.countLabelBought : null]}>
                {countLabel}
              </Text>
            ) : null}
            {actionLabel && onActionPress ? (
              <Pressable
                onPress={onActionPress}
                style={({ pressed }) => [
                  styles.actionButton,
                  isBought ? styles.actionButtonBought : null,
                  pressed ? styles.actionButtonPressed : null,
                ]}
              >
                <Text style={[styles.actionLabel, isBought ? styles.actionLabelBought : null]}>
                  {actionLabel}
                </Text>
              </Pressable>
            ) : null}
          </View>
        ) : null}
      </View>

      {emptyState ? (
        <Text style={[styles.emptyState, isBought ? styles.emptyStateBought : null]}>{emptyState}</Text>
      ) : hasContent ? (
        <View style={styles.content}>{children}</View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    gap: theme.spacing.xs,
  },
  sectionBought: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.sm,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  headerMain: {
    flex: 1,
    minWidth: 0,
    gap: 4,
  },
  headerAside: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
  },
  title: {
    ...textStyles.body,
    fontWeight: '700',
  },
  titleBought: {
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  countLabel: {
    ...textStyles.subtle,
    fontWeight: '600',
  },
  countLabelBought: {
    color: theme.colors.textSecondary,
  },
  hint: {
    ...textStyles.subtle,
  },
  hintBought: {
    color: theme.colors.textSecondary,
  },
  actionButton: {
    minHeight: 28,
    justifyContent: 'center',
  },
  actionButtonBought: {
    opacity: 0.9,
  },
  actionButtonPressed: {
    opacity: 0.65,
  },
  actionLabel: {
    ...textStyles.subtle,
    fontWeight: '600',
    color: theme.colors.feature.shopping,
  },
  actionLabelBought: {
    color: theme.colors.textSecondary,
  },
  emptyState: {
    ...textStyles.subtle,
    paddingTop: 4,
  },
  emptyStateBought: {
    color: theme.colors.textSecondary,
  },
  content: {
    gap: theme.spacing.xs,
  },
});
