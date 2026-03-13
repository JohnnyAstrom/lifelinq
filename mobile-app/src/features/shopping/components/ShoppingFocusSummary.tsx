import { Ionicons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  title: string;
  subtitle: string;
};

export function ShoppingFocusSummary({ title, subtitle }: Props) {
  return (
    <View style={styles.summary}>
      <View style={styles.iconWrap}>
        <Ionicons name="checkmark-done" size={16} color={theme.colors.feature.shopping} />
      </View>
      <View style={styles.content}>
        <Text style={styles.title}>{title}</Text>
        <Text style={styles.subtitle}>{subtitle}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  summary: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: theme.spacing.xs,
    marginBottom: theme.spacing.xs,
  },
  iconWrap: {
    width: 30,
    height: 30,
    borderRadius: 15,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(76, 126, 96, 0.12)',
  },
  content: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  title: {
    ...textStyles.subtle,
    fontWeight: '700',
    color: theme.colors.text,
  },
  subtitle: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
});
