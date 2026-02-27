import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { BackIconButton, AppCard, AppScreen, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  me: MeResponse;
  onDone: () => void;
  onOpenSpace: () => void;
};

export function SpacesScreen({ me, onDone, onOpenSpace }: Props) {
  const strings = {
    title: 'Spaces',
    subtitle: 'Choose a space to manage members.',
    createSpace: 'Create new space',
    createSpaceHint: 'Coming soon',
    unnamedSpace: 'My space',
  };
  const spaces = me.memberships.map((membership) => ({
    id: membership.groupId,
    name: membership.groupName?.trim() ? membership.groupName : strings.unnamedSpace,
  }));

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
  });

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        {spaces.map((space) => (
          <AppCard key={space.id}>
            <Pressable style={styles.spaceRow} onPress={onOpenSpace} accessibilityRole="button">
              <Text style={styles.spaceName}>{space.name}</Text>
              <Text style={styles.chevron}>→</Text>
            </Pressable>
          </AppCard>
        ))}

        <View style={styles.createRow}>
          <Subtle>{strings.createSpace}</Subtle>
          <Subtle>{strings.createSpaceHint}</Subtle>
        </View>
      </View>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  spaceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  spaceName: {
    ...textStyles.h3,
    flex: 1,
  },
  chevron: {
    ...textStyles.h3,
  },
  createRow: {
    alignItems: 'flex-start',
    gap: theme.spacing.xs,
    paddingHorizontal: theme.spacing.xs,
  },
});
