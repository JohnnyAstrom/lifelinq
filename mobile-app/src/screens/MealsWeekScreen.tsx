import { useMemo, useState } from 'react';
import {
  Button,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useWeekPlan } from '../features/meals/hooks/useWeekPlan';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';

type Props = {
  token: string;
  onDone: () => void;
};

const DAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const MONTH_LABELS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function addDays(date: Date, days: number) {
  const next = new Date(date.getTime());
  next.setDate(next.getDate() + days);
  return next;
}

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

function getWeekStartDate(year: number, isoWeek: number): Date {
  const date = new Date(Date.UTC(year, 0, 4));
  const day = date.getUTCDay() || 7;
  date.setUTCDate(date.getUTCDate() + 1 - day);
  date.setUTCDate(date.getUTCDate() + (isoWeek - 1) * 7);
  return date;
}

function formatDayLabel(date: Date, dayIndex: number) {
  const dayLabel = DAY_LABELS[dayIndex];
  const day = String(date.getUTCDate()).padStart(2, '0');
  const month = MONTH_LABELS[date.getUTCMonth()];
  return `${dayLabel} ${day} ${month}`;
}

export function MealsWeekScreen({ token, onDone }: Props) {
  const [anchorDate, setAnchorDate] = useState(() => new Date());
  const { year, isoWeek } = useMemo(
    () => getIsoWeekParts(anchorDate),
    [anchorDate]
  );
  const weekStart = useMemo(() => getWeekStartDate(year, isoWeek), [year, isoWeek]);
  const plan = useWeekPlan(token, year, isoWeek);
  const shopping = useShoppingLists(token);

  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  const [recipeTitle, setRecipeTitle] = useState('');
  const [pushToShopping, setPushToShopping] = useState(true);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);

  const mealsByDay = useMemo(() => {
    const map = new Map<number, string>();
    if (plan.data) {
      for (const meal of plan.data.meals) {
        map.set(meal.dayOfWeek, meal.recipeTitle);
      }
    }
    return map;
  }, [plan.data]);

  const lists = shopping.lists;
  const effectiveListId =
    selectedListId ?? (lists.length > 0 ? lists[0].id : null);

  async function handleSave() {
    if (!selectedDay) {
      return;
    }
    if (!recipeTitle.trim()) {
      return;
    }
    await plan.addMeal(selectedDay, {
      recipeId: uuidv4(),
      recipeTitle: recipeTitle.trim(),
      targetShoppingListId: pushToShopping ? effectiveListId : null,
    });
    setRecipeTitle('');
    setSelectedDay(null);
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.headerRow}>
        <Button title="Prev" onPress={() => setAnchorDate(addDays(anchorDate, -7))} />
        <Text style={styles.headerText}>
          Week {isoWeek} · {year}
        </Text>
        <Button title="Next" onPress={() => setAnchorDate(addDays(anchorDate, 7))} />
      </View>

      {plan.loading ? <Text>Loading week plan...</Text> : null}
      {plan.error ? <Text style={styles.error}>{plan.error}</Text> : null}

      <View style={styles.list}>
        {DAY_LABELS.map((_, index) => {
          const day = index + 1;
          const date = new Date(weekStart.getTime());
          date.setUTCDate(weekStart.getUTCDate() + index);
          const label = formatDayLabel(date, index);
          const meal = mealsByDay.get(day) || 'Empty';
          return (
            <View key={label} style={styles.row}>
              <View style={styles.rowInfo}>
                <Text style={styles.dayLabel}>{label}</Text>
                <Text style={styles.mealText}>{meal}</Text>
              </View>
              <View style={styles.rowActions}>
                <Button title="Edit" onPress={() => setSelectedDay(day)} />
                <Button title="Remove" onPress={() => plan.removeMeal(day)} />
              </View>
            </View>
          );
        })}
      </View>

      <View style={styles.editor}>
        <Text style={styles.sectionTitle}>Add / Replace</Text>
        <Text>Selected day: {selectedDay ?? 'None'}</Text>
        <TextInput
          style={styles.input}
          placeholder="Recipe title"
          value={recipeTitle}
          onChangeText={setRecipeTitle}
        />
        <View style={styles.toggleRow}>
          <Button
            title={pushToShopping ? 'Push: ON' : 'Push: OFF'}
            onPress={() => setPushToShopping((prev) => !prev)}
          />
        </View>
        <View style={styles.lists}>
          {lists.length === 0 ? (
            <Text>No shopping lists yet.</Text>
          ) : (
            lists.map((list) => (
              <Button
                key={list.id}
                title={list.id === effectiveListId ? `• ${list.name}` : list.name}
                onPress={() => setSelectedListId(list.id)}
              />
            ))
          )}
        </View>
        <Button title="Save meal" onPress={handleSave} />
      </View>

      <Button title="Back" onPress={onDone} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 12,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerText: {
    fontSize: 18,
    fontWeight: '600',
  },
  list: {
    gap: 8,
  },
  row: {
    borderWidth: 1,
    borderColor: '#d2d2d2',
    padding: 12,
    borderRadius: 8,
    gap: 8,
  },
  rowInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  dayLabel: {
    fontWeight: '600',
  },
  mealText: {
    color: '#333',
  },
  rowActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  editor: {
    borderWidth: 1,
    borderColor: '#c7c7c7',
    padding: 12,
    borderRadius: 10,
    gap: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
  },
  input: {
    borderWidth: 1,
    borderColor: '#999',
    borderRadius: 8,
    padding: 8,
  },
  toggleRow: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },
  lists: {
    gap: 6,
  },
  error: {
    color: '#b00020',
  },
});
