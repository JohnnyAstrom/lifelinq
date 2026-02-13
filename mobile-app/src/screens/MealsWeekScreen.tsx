import { useMemo, useState } from 'react';
import {
  Button,
  Pressable,
  ScrollView,
  Switch,
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
const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER'] as const;
const MEAL_TYPE_LABELS: Record<(typeof MEAL_TYPES)[number], string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
};
const MONTH_LABELS = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

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
  const weekStart = useMemo(
    () => getWeekStartDate(year, isoWeek),
    [year, isoWeek]
  );
  const plan = useWeekPlan(token, year, isoWeek);
  const shopping = useShoppingLists(token);

  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  const [selectedMealType, setSelectedMealType] = useState<(typeof MEAL_TYPES)[number] | null>('DINNER');
  const [recipeTitle, setRecipeTitle] = useState('');
  const [pushToShopping, setPushToShopping] = useState(true);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);

  const mealsByDay = useMemo(() => {
    const map = new Map<string, string>();
    if (plan.data) {
      for (const meal of plan.data.meals) {
        map.set(`${meal.dayOfWeek}:${meal.mealType}`, meal.recipeTitle);
      }
    }
    return map;
  }, [plan.data]);

  const lists = shopping.lists;
  const effectiveListId =
    selectedListId ?? (lists.length > 0 ? lists[0].id : null);

  const selectedMealTitle = useMemo(() => {
    if (!selectedDay || !selectedMealType) {
      return '';
    }
    return mealsByDay.get(`${selectedDay}:${selectedMealType}`) || '';
  }, [selectedDay, selectedMealType, mealsByDay]);

  async function handleSave() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    if (!recipeTitle.trim()) {
      return;
    }
    await plan.addMeal(selectedDay, selectedMealType, {
      recipeId: uuidv4(),
      recipeTitle: recipeTitle.trim(),
      mealType: selectedMealType,
      targetShoppingListId: pushToShopping ? effectiveListId : null,
    });
    setRecipeTitle('');
    setSelectedDay(null);
    setSelectedMealType('DINNER');
  }

  function handleSelectDay(day: number, mealType: (typeof MEAL_TYPES)[number]) {
    setSelectedDay(day);
    setSelectedMealType(mealType);
    const existing = mealsByDay.get(`${day}:${mealType}`);
    setRecipeTitle(existing ?? '');
  }

  const weekEnd = useMemo(() => {
    const end = new Date(weekStart.getTime());
    end.setUTCDate(weekStart.getUTCDate() + 6);
    return end;
  }, [weekStart]);

  return (
    <View style={styles.root}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.headerCard}>
        <View style={styles.headerRow}>
          <Button
            title="Prev"
            onPress={() => setAnchorDate(addDays(anchorDate, -7))}
          />
          <Text style={styles.headerText}>
            Week {isoWeek} · {year}
          </Text>
          <Button
            title="Next"
            onPress={() => setAnchorDate(addDays(anchorDate, 7))}
          />
        </View>
        <Text style={styles.subtle}>
          {formatDayLabel(weekStart, 0)} — {formatDayLabel(weekEnd, 6)}
        </Text>
        </View>

        {plan.loading ? <Text>Loading week plan...</Text> : null}
        {plan.error ? <Text style={styles.error}>{plan.error}</Text> : null}

        <View style={styles.listCard}>
        {DAY_LABELS.map((_, index) => {
          const day = index + 1;
          const date = new Date(weekStart.getTime());
          date.setUTCDate(weekStart.getUTCDate() + index);
          const label = formatDayLabel(date, index);
          return (
            <Pressable
              key={label}
              style={[
                styles.row,
                selectedDay === day ? styles.rowActive : null,
              ]}
            >
              <View style={styles.rowHeader}>
                <Text style={styles.dayLabel}>{label}</Text>
              </View>
              <View style={styles.mealSlots}>
                {MEAL_TYPES.map((mealType) => {
                  const meal = mealsByDay.get(`${day}:${mealType}`) || 'Empty';
                  const hasMeal = meal !== 'Empty';
                  return (
                    <Pressable
                      key={mealType}
                      style={[styles.slotRow, hasMeal ? styles.slotFilled : styles.slotEmpty]}
                      onPress={() => handleSelectDay(day, mealType)}
                    >
                      <Text style={styles.slotLabel}>{MEAL_TYPE_LABELS[mealType]}</Text>
                      <Text style={styles.slotValue}>{meal}</Text>
                    </Pressable>
                  );
                })}
              </View>
            </Pressable>
          );
        })}
      </View>

        <View style={styles.footerCard}>
          <Button title="Back" onPress={onDone} />
        </View>
      </ScrollView>

      {selectedDay && selectedMealType ? (
        <Pressable style={styles.backdrop} onPress={() => setSelectedDay(null)}>
          <Pressable style={styles.modalCard} onPress={() => null}>
            <Text style={styles.sectionTitle}>Plan a meal</Text>
            <Text style={styles.subtle}>
              {`Selected day: ${formatDayLabel(
                new Date(weekStart.getTime() + (selectedDay - 1) * 86400000),
                selectedDay - 1
              )}`}
            </Text>
            <View style={styles.mealTypeRow}>
              {MEAL_TYPES.map((mealType) => {
                const active = mealType === selectedMealType;
                return (
                  <Pressable
                    key={mealType}
                    style={[styles.mealTypeChip, active ? styles.mealTypeChipActive : null]}
                    onPress={() => {
                      setSelectedMealType(mealType);
                      const existing = mealsByDay.get(`${selectedDay}:${mealType}`);
                      setRecipeTitle(existing ?? '');
                    }}
                  >
                    <Text style={[styles.mealTypeText, active ? styles.mealTypeTextActive : null]}>
                      {MEAL_TYPE_LABELS[mealType]}
                    </Text>
                  </Pressable>
                );
              })}
            </View>
            <TextInput
              style={styles.input}
              placeholder="Recipe title"
              value={recipeTitle}
              onChangeText={setRecipeTitle}
              autoFocus
            />
            <View style={styles.toggleRow}>
              <Text style={styles.toggleLabel}>Push to shopping</Text>
              <Switch value={pushToShopping} onValueChange={setPushToShopping} />
            </View>
            <View style={styles.lists}>
              {lists.length === 0 ? (
                <Text style={styles.subtle}>No shopping lists yet.</Text>
              ) : (
                <View style={styles.chipRow}>
                  {lists.map((list) => {
                    const active = list.id === effectiveListId;
                    return (
                      <Pressable
                        key={list.id}
                        style={[styles.chip, active ? styles.chipActive : null]}
                        onPress={() => setSelectedListId(list.id)}
                      >
                        <Text
                          style={[
                            styles.chipText,
                            active ? styles.chipTextActive : null,
                          ]}
                        >
                          {list.name}
                        </Text>
                      </Pressable>
                    );
                  })}
                </View>
              )}
            </View>
            <View style={styles.editorActions}>
              <Button title="Save meal" onPress={handleSave} />
              {selectedMealTitle ? (
                <Button
                  title="Remove meal"
                  onPress={() => plan.removeMeal(selectedDay, selectedMealType)}
                />
              ) : null}
              <Button title="Close" onPress={() => setSelectedDay(null)} />
            </View>
          </Pressable>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#f6f5f2',
  },
  container: {
    padding: 16,
    gap: 12,
    backgroundColor: '#f6f5f2',
  },
  headerCard: {
    backgroundColor: '#ffffff',
    borderRadius: 14,
    padding: 14,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 6,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1e1c16',
    fontFamily: 'Georgia',
  },
  listCard: {
    backgroundColor: '#ffffff',
    borderRadius: 14,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 10,
  },
  row: {
    borderWidth: 1,
    borderColor: '#efe7da',
    padding: 12,
    borderRadius: 10,
    gap: 6,
    backgroundColor: '#fffaf0',
  },
  rowActive: {
    borderColor: '#bfa77a',
    backgroundColor: '#fdf4e3',
  },
  rowHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  dayLabel: {
    fontWeight: '700',
    color: '#2b2418',
  },
  mealText: {
    color: '#40372c',
  },
  mealSlots: {
    gap: 8,
  },
  slotRow: {
    borderWidth: 1,
    borderRadius: 10,
    padding: 8,
    gap: 4,
  },
  slotFilled: {
    borderColor: '#d1b78a',
    backgroundColor: '#fff6e3',
  },
  slotEmpty: {
    borderColor: '#e1d9cc',
    backgroundColor: '#f7f2ea',
  },
  slotLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: '#4a3f2f',
  },
  slotValue: {
    color: '#40372c',
  },
  editorCard: {
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: '#e7e1d7',
    padding: 12,
    borderRadius: 14,
    gap: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1e1c16',
    fontFamily: 'Georgia',
  },
  input: {
    borderWidth: 1,
    borderColor: '#c9bfae',
    borderRadius: 8,
    padding: 8,
    backgroundColor: '#fffdf8',
  },
  toggleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  toggleLabel: {
    fontWeight: '600',
    color: '#2b2418',
  },
  lists: {
    gap: 6,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chip: {
    borderWidth: 1,
    borderColor: '#d9cbb3',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: '#fff7e6',
  },
  chipActive: {
    borderColor: '#b28941',
    backgroundColor: '#f0dfbd',
  },
  chipText: {
    color: '#5a4b33',
    fontWeight: '600',
  },
  chipTextActive: {
    color: '#3f2f18',
  },
  editorActions: {
    gap: 8,
  },
  subtle: {
    color: '#6f675b',
  },
  error: {
    color: '#b00020',
  },
  footerCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e7e1d7',
  },
  backdrop: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.35)',
    justifyContent: 'center',
    padding: 16,
  },
  modalCard: {
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 10,
  },
  mealTypeRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  mealTypeChip: {
    borderWidth: 1,
    borderColor: '#d9cbb3',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: '#fff7e6',
  },
  mealTypeChipActive: {
    borderColor: '#b28941',
    backgroundColor: '#f0dfbd',
  },
  mealTypeText: {
    color: '#5a4b33',
    fontWeight: '600',
  },
  mealTypeTextActive: {
    color: '#3f2f18',
  },
});
