import { StyleSheet, Text, View } from 'react-native';
import { useEffect, useMemo, useState } from 'react';
import { getWeekPlan, type WeekPlanResponse } from '../api/mealsApi';
import { formatApiError } from '../../../shared/api/client';
import { AppButton, AppCard, AppScreen, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

type LoadState = {
  loading: boolean;
  error: string | null;
  data: WeekPlanResponse | null;
};

function getIsoWeekParts(date: Date): { year: number; isoWeek: number } {
  const target = new Date(date.getTime());
  target.setHours(0, 0, 0, 0);
  const day = target.getDay() || 7;
  target.setDate(target.getDate() + 4 - day);
  const yearStart = new Date(target.getFullYear(), 0, 1);
  const diff = target.getTime() - yearStart.getTime();
  const dayMs = 24 * 60 * 60 * 1000;
  const week = Math.ceil((diff / dayMs + 1) / 7);
  return { year: target.getFullYear(), isoWeek: week };
}

export function MealsProofScreen({ token, onDone }: Props) {
  const { year, isoWeek } = useMemo(() => getIsoWeekParts(new Date()), []);
  const [state, setState] = useState<LoadState>({
    loading: true,
    error: null,
    data: null,
  });
  const strings = {
    title: 'Meals proof-screen',
    yearLabel: 'Year',
    weekLabel: 'Week',
    loading: 'Loading...',
    reload: 'Reload',
    back: 'Back',
  };

  const load = async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const data = await getWeekPlan(year, isoWeek, { token });
      console.log('Meals week plan', data);
      setState({ loading: false, error: null, data });
    } catch (err) {
      setState({ loading: false, error: formatApiError(err), data: null });
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <AppScreen>
      <AppCard style={styles.card}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>
          {strings.yearLabel}: {year} · {strings.weekLabel}: {isoWeek}
        </Subtle>
        {state.loading ? <Subtle>{strings.loading}</Subtle> : null}
        {state.error ? <Text style={styles.error}>{state.error}</Text> : null}
        {!state.loading && state.data ? (
          <Text style={styles.code}>{JSON.stringify(state.data, null, 2)}</Text>
        ) : null}
        <View style={styles.actions}>
          <AppButton title={strings.reload} onPress={load} fullWidth />
          <AppButton title={strings.back} onPress={onDone} variant="ghost" fullWidth />
        </View>
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  code: {
    fontFamily: 'Courier New',
    fontSize: 12,
    color: theme.colors.text,
    backgroundColor: theme.colors.surfaceAlt,
    padding: theme.spacing.sm,
    borderRadius: theme.radius.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  actions: {
    gap: theme.spacing.sm,
  },
});
