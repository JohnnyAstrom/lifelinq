import { StyleSheet, View } from 'react-native';
import type { HomeFeatureModule } from '../utils/homeOverview';
import { FeatureModuleCard } from './FeatureModuleCard';

type Props = {
  modules: HomeFeatureModule[];
};

export function FeatureModulesGrid({ modules }: Props) {
  const rows: HomeFeatureModule[][] = [];
  for (let index = 0; index < modules.length; index += 2) {
    rows.push(modules.slice(index, index + 2));
  }

  return (
    <View style={styles.grid}>
      {rows.map((row, index) => (
        <View key={`row-${index}`} style={styles.row}>
          {row.map((module) => (
            <FeatureModuleCard key={module.id} {...module} />
          ))}
          {row.length === 1 ? <View style={styles.spacer} /> : null}
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  grid: {
    gap: 16,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'stretch',
    gap: 16,
  },
  spacer: {
    flex: 1,
  },
});
