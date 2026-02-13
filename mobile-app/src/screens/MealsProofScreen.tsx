import { Button, ScrollView, Text, View } from 'react-native';
import { useEffect, useMemo, useState } from 'react';
import { getWeekPlan, type WeekPlanResponse } from '../shared/api/meals';
import { formatApiError } from '../shared/api/client';

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
    <ScrollView>
      <View>
        <Text>Meals proof-screen</Text>
        <Text>
          Year: {year} Week: {isoWeek}
        </Text>
        {state.loading ? <Text>Loading...</Text> : null}
        {state.error ? <Text>{state.error}</Text> : null}
        {!state.loading && state.data ? (
          <Text>{JSON.stringify(state.data, null, 2)}</Text>
        ) : null}
        <Button title="Reload" onPress={load} />
        <Button title="Back" onPress={onDone} />
      </View>
    </ScrollView>
  );
}
