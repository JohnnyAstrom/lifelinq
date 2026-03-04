import { ActivityIndicator } from 'react-native';
import { AppScreen } from '../shared/ui/components';
import { theme } from '../shared/ui/theme';

export function HydratingSplashScreen() {
  return (
    <AppScreen scroll={false} contentStyle={{ justifyContent: 'center', alignItems: 'center' }}>
      <ActivityIndicator color={theme.colors.primary} />
    </AppScreen>
  );
}

