import { StyleSheet, View } from 'react-native';
import { FeatureModulesGrid, PlaceHeader, TodaySummaryCard } from '../features/home/components';
import { useHomeOverview } from '../features/home/hooks/useHomeOverview';
import { AppScreen } from '../shared/ui/components';

type Props = {
  token: string;
  spaceName?: string;
  canSwitchSpaces: boolean;
  onContextInvalidated: () => void;
  onOpenSwitchSheet: () => void;
  onCreateTodo: () => void;
  onCreateShopping: () => void;
  onMeals: () => void;
  onDocuments: () => void;
  onEconomy: () => void;
  onSettings: () => void;
};

export function HomeScreen({
  token,
  spaceName,
  canSwitchSpaces,
  onContextInvalidated,
  onOpenSwitchSheet,
  onCreateTodo,
  onCreateShopping,
  onMeals,
  onDocuments,
  onEconomy,
  onSettings,
}: Props) {
  const overview = useHomeOverview({
    token,
    spaceName,
    canSwitchSpaces,
    onContextInvalidated,
    onCreateTodo,
    onCreateShopping,
    onMeals,
    onDocuments,
    onEconomy,
  });

  return (
    <AppScreen
      header={(
        <PlaceHeader
          title={overview.place.title}
          subtitle={overview.place.subtitle}
          canSwitch={overview.place.canSwitch}
          onPressPlace={onOpenSwitchSheet}
          onPressSettings={onSettings}
        />
      )}
      contentStyle={styles.content}
    >
      <TodaySummaryCard {...overview.today} />
      <View style={styles.modulesSection}>
        <FeatureModulesGrid modules={overview.modules} />
      </View>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 24,
    paddingVertical: 24,
    gap: 24,
  },
  modulesSection: {
    gap: 16,
  },
});
