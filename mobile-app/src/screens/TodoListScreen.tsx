import { useEffect, useMemo, useState } from 'react';
import DateTimePicker from '@react-native-community/datetimepicker';
import {
  Keyboard,
  Modal,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';
import { useTodos } from '../features/todo/hooks/useTodos';
import { useAppBackHandler } from '../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../shared/ui/OverlaySheet';
import {
  AppButton,
  AppCard,
  AppChip,
  AppInput,
  AppScreen,
  SectionTitle,
  Subtle,
  TopBar,
} from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

type TodoTimeView = 'DAILY' | 'WEEKLY' | 'MONTHLY';
type FrontendTodoScope = 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
type ScopePickerTarget = 'ADD' | 'EDIT' | null;
type FrontendScopeState =
  | { scope: 'DAY'; scopeValue: string }
  | { scope: 'WEEK'; scopeValue: { year: number; week: number } }
  | { scope: 'MONTH'; scopeValue: { year: number; month: number } }
  | { scope: 'LATER'; scopeValue: null };
type TodoDatePickerContext =
  | 'ADD_DAY'
  | 'ADD_WEEK'
  | 'ADD_MONTH'
  | 'EDIT_DAY'
  | 'EDIT_WEEK'
  | 'EDIT_MONTH';

function getStartOfWeekMonday(date: Date) {
  const next = new Date(date.getTime());
  next.setHours(0, 0, 0, 0);
  const day = next.getDay(); // 0 Sun ... 6 Sat
  const diff = day === 0 ? -6 : 1 - day;
  next.setDate(next.getDate() + diff);
  return next;
}

function toDateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function getIsoWeekInfoFromDate(date: Date) {
  const target = new Date(date.getTime());
  target.setHours(0, 0, 0, 0);
  const day = target.getDay() || 7;
  target.setDate(target.getDate() + 4 - day);
  const yearStart = new Date(target.getFullYear(), 0, 1);
  const diffMs = target.getTime() - yearStart.getTime();
  const week = Math.ceil(((diffMs / 86400000) + 1) / 7);
  return { week, year: target.getFullYear() };
}

export function TodoListScreen({ token, onDone }: Props) {
  const { width: viewportWidth } = useWindowDimensions();
  const [timeView, setTimeView] = useState<TodoTimeView>('DAILY');
  const [calendarMonth, setCalendarMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [selectedDailyDate, setSelectedDailyDate] = useState(() => {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    return now;
  });
  const [isDailyDoneExpanded, setIsDailyDoneExpanded] = useState(false);
  const [selectedWeeklyStart, setSelectedWeeklyStart] = useState(() => getStartOfWeekMonday(new Date()));
  const [text, setText] = useState('');
  const [pendingDate, setPendingDate] = useState<Date | null>(null);
  const [pendingTime, setPendingTime] = useState<string | null>(null);
  const [pendingScope, setPendingScope] = useState<FrontendTodoScope>('LATER');
  const [pendingWeekStart, setPendingWeekStart] = useState<Date>(() => getStartOfWeekMonday(new Date()));
  const [pendingMonthDate, setPendingMonthDate] = useState<Date>(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1));
  const [showAddTodoSheet, setShowAddTodoSheet] = useState(false);
  const [showUnplannedSheet, setShowUnplannedSheet] = useState(false);
  const [showWeekGoalsSheet, setShowWeekGoalsSheet] = useState(false);
  const [showMonthGoalsSheet, setShowMonthGoalsSheet] = useState(false);
  const [isKeyboardVisible, setIsKeyboardVisible] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showWeekPicker, setShowWeekPicker] = useState(false);
  const [showMonthPicker, setShowMonthPicker] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [detailsTodoId, setDetailsTodoId] = useState<string | null>(null);
  const [detailText, setDetailText] = useState('');
  const [detailDate, setDetailDate] = useState<Date | null>(null);
  const [detailTime, setDetailTime] = useState<string | null>(null);
  const [detailScope, setDetailScope] = useState<FrontendTodoScope>('LATER');
  const [detailWeekStart, setDetailWeekStart] = useState<Date>(() => getStartOfWeekMonday(new Date()));
  const [detailMonthDate, setDetailMonthDate] = useState<Date>(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1));
  const [showDetailDatePicker, setShowDetailDatePicker] = useState(false);
  const [showDetailWeekPicker, setShowDetailWeekPicker] = useState(false);
  const [showDetailMonthPicker, setShowDetailMonthPicker] = useState(false);
  const [showDetailTimePicker, setShowDetailTimePicker] = useState(false);
  const [scopePickerTarget, setScopePickerTarget] = useState<ScopePickerTarget>(null);
  const [savingDetails, setSavingDetails] = useState(false);
  const todos = useTodos(token, 'ALL');
  const strings = {
    title: 'Todos',
    subtitle: 'Keep the list moving.',
    viewTitle: 'View',
    daily: 'Daily',
    weekly: 'Weekly',
    monthly: 'Monthly',
    listTitle: 'List',
    noTodos: 'No todos yet.',
    complete: 'Complete',
    reopen: 'Reopen',
    addTodoTitle: 'Add todo',
    addPlaceholder: 'What needs to be done?',
    addAction: 'Add',
    adding: 'Adding...',
    back: 'Back',
    quickDateTitle: 'Date',
    quickToday: 'Today',
    quickTomorrow: 'Tomorrow',
    quickPick: 'Pick date',
    quickTimeTitle: 'Time',
    timeMorning: 'Morning (08–12)',
    timeAfternoon: 'Afternoon (12–17)',
    timeEvening: 'Evening (17–21)',
    timePick: 'Exact time…',
    timeNone: 'Any time',
    pendingDatePrefix: 'Scheduled:',
    monthBack: 'Prev month',
    monthNext: 'Next month',
    dayBack: 'Prev day',
    dayNext: 'Next day',
    weekBack: 'Prev week',
    weekNext: 'Next week',
    thisWeekSection: 'This Week',
    noWeekScopeTodosYet: 'No week-scoped todos yet (Phase 1).',
    noItemsForDay: 'No todos for this day.',
    thisMonthSection: 'This Month',
    noMonthScopeTodosYet: 'No month-scoped todos yet (Phase 1).',
    weekGoals: 'Week goals',
    monthGoals: 'Month goals',
    details: 'Edit',
    editTitle: 'Edit todo',
    editSubtitle: 'Update details and scheduling.',
    saveChanges: 'Save changes',
    savingChanges: 'Saving...',
    close: 'Close',
    clearDate: 'Clear date',
    change: 'Change',
    forToday: 'Today',
    forTomorrow: 'Tomorrow',
    forWeek: 'This week',
    forMonth: 'This month',
    forLater: 'Unplanned',
    unplannedTitle: 'Unplanned',
    noUnplannedItems: 'No unplanned todos.',
    chooseScopeTitle: 'Schedule',
    chooseScopeSubtitle: 'Choose when this todo belongs.',
    pickWeek: 'Pick week',
    pickMonth: 'Pick month',
    timeValuePrefix: 'Time',
    pickDateTitle: 'Pick a date',
    pickWeekTitle: 'Pick a week',
    pickMonthTitle: 'Pick a month',
    pickTimeTitle: 'Pick a time',
  };

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen:
      showDetailTimePicker ||
      showDetailMonthPicker ||
      showDetailWeekPicker ||
      showMonthGoalsSheet ||
      showWeekGoalsSheet ||
      showDetailDatePicker ||
      showTimePicker ||
      showMonthPicker ||
      showWeekPicker ||
      showDatePicker ||
      showUnplannedSheet ||
      showAddTodoSheet ||
      !!detailsTodoId,
    onCloseOverlay: () => {
      if (showDetailTimePicker) {
        setShowDetailTimePicker(false);
        return;
      }
      if (showWeekGoalsSheet) {
        setShowWeekGoalsSheet(false);
        return;
      }
      if (showMonthGoalsSheet) {
        setShowMonthGoalsSheet(false);
        return;
      }
      if (showDetailDatePicker) {
        setShowDetailDatePicker(false);
        return;
      }
      if (showDetailWeekPicker) {
        setShowDetailWeekPicker(false);
        return;
      }
      if (showDetailMonthPicker) {
        setShowDetailMonthPicker(false);
        return;
      }
      if (showTimePicker) {
        setShowTimePicker(false);
        return;
      }
      if (showDatePicker) {
        setShowDatePicker(false);
        return;
      }
      if (showWeekPicker) {
        setShowWeekPicker(false);
        return;
      }
      if (showMonthPicker) {
        setShowMonthPicker(false);
        return;
      }
      if (showUnplannedSheet) {
        setShowUnplannedSheet(false);
        return;
      }
      if (showAddTodoSheet) {
        closeAddTodoSheet();
        return;
      }
      if (detailsTodoId) {
        setDetailsTodoId(null);
      }
    },
  });

  useEffect(() => {
    const showSub = Keyboard.addListener('keyboardDidShow', () => setIsKeyboardVisible(true));
    const hideSub = Keyboard.addListener('keyboardDidHide', () => setIsKeyboardVisible(false));
    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, []);

  function getMondayOfIsoWeek(year: number, week: number) {
    const jan4 = new Date(year, 0, 4);
    jan4.setHours(0, 0, 0, 0);
    const mondayOfWeek1 = getStartOfWeekMonday(jan4);
    const monday = new Date(mondayOfWeek1.getTime());
    monday.setDate(monday.getDate() + (week - 1) * 7);
    monday.setHours(0, 0, 0, 0);
    return monday;
  }

  function deriveScopeStateFromItem(item: (typeof todos.items)[number]): FrontendScopeState {
    if (item.scope === 'DAY' && item.dueDate) {
      return { scope: 'DAY', scopeValue: item.dueDate };
    }
    if (item.scope === 'WEEK' && item.scopeYear && item.scopeWeek) {
      return { scope: 'WEEK', scopeValue: { year: item.scopeYear, week: item.scopeWeek } };
    }
    if (item.scope === 'MONTH' && item.scopeYear && item.scopeMonth) {
      return { scope: 'MONTH', scopeValue: { year: item.scopeYear, month: item.scopeMonth } };
    }
    if (item.scope === 'LATER') {
      return { scope: 'LATER', scopeValue: null };
    }
    // Legacy fallback for rows created before backend scope migration/backfill.
    if (item.dueDate) {
      return { scope: 'DAY', scopeValue: item.dueDate };
    }
    return { scope: 'LATER', scopeValue: null };
  }

  function buildSchedulingPayload(
    scope: FrontendTodoScope,
    dayDate: Date | null,
    time: string | null,
    weekStartDate: Date,
    monthDate: Date
  ) {
    if (scope === 'DAY') {
      return {
        scope: 'DAY' as const,
        dueDate: dayDate ? toApiDate(dayDate) : null,
        dueTime: dayDate ? time : null,
      };
    }
    if (scope === 'WEEK') {
      const { week, year } = getIsoWeekInfoFromDate(weekStartDate);
      return {
        scope: 'WEEK' as const,
        scopeYear: year,
        scopeWeek: week,
      };
    }
    if (scope === 'MONTH') {
      return {
        scope: 'MONTH' as const,
        scopeYear: monthDate.getFullYear(),
        scopeMonth: monthDate.getMonth() + 1,
      };
    }
    return {
      scope: 'LATER' as const,
    };
  }

  async function handleAdd() {
    if (!text.trim() || todos.loading) {
      return;
    }
    const scheduling = buildSchedulingPayload(pendingScope, pendingDate, pendingTime, pendingWeekStart, pendingMonthDate);
    const added = await todos.add(text.trim(), scheduling);
    if (added) {
      setText('');
      setPendingScope('LATER');
      setPendingDate(null);
      setPendingTime(null);
      setShowAddTodoSheet(false);
      Keyboard.dismiss();
    }
  }

  function closeAddTodoSheet() {
    setShowAddTodoSheet(false);
    setScopePickerTarget(null);
    setText('');
    setPendingScope('LATER');
    setPendingDate(null);
    setPendingTime(null);
    setShowDatePicker(false);
    setShowWeekPicker(false);
    setShowMonthPicker(false);
    setShowTimePicker(false);
    Keyboard.dismiss();
  }

  function setDateToToday() {
    Keyboard.dismiss();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    setPendingScope('DAY');
    setPendingDate(today);
  }

  function setDateToTomorrow() {
    Keyboard.dismiss();
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    setPendingScope('DAY');
    setPendingDate(tomorrow);
  }

  function setDetailDateToToday() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    setDetailScope('DAY');
    setDetailDate(today);
  }

  function setDetailDateToTomorrow() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    setDetailScope('DAY');
    setDetailDate(tomorrow);
  }

  function setPendingToCurrentWeek() {
    const currentWeekStart = getStartOfWeekMonday(new Date());
    setPendingScope('WEEK');
    setPendingDate(null);
    setPendingTime(null);
    setPendingWeekStart(currentWeekStart);
  }

  function setDetailToCurrentWeek() {
    const currentWeekStart = getStartOfWeekMonday(new Date());
    setDetailScope('WEEK');
    setDetailDate(null);
    setDetailTime(null);
    setDetailWeekStart(currentWeekStart);
  }

  function setPendingToCurrentMonth() {
    const now = new Date();
    setPendingScope('MONTH');
    setPendingDate(null);
    setPendingTime(null);
    setPendingMonthDate(new Date(now.getFullYear(), now.getMonth(), 1));
  }

  function setDetailToCurrentMonth() {
    const now = new Date();
    setDetailScope('MONTH');
    setDetailDate(null);
    setDetailTime(null);
    setDetailMonthDate(new Date(now.getFullYear(), now.getMonth(), 1));
  }

  function isSameDay(left: Date, right: Date) {
    return left.getFullYear() === right.getFullYear()
      && left.getMonth() === right.getMonth()
      && left.getDate() === right.getDate();
  }

  function formatDate(date: Date) {
    const day = date.toLocaleDateString(undefined, { weekday: 'short' });
    const dayDate = date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    return `${day} · ${dayDate}`;
  }

  function toApiDate(date: Date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  function parseApiDate(value?: string | null) {
    if (!value) {
      return null;
    }
    const [year, month, day] = value.split('-').map(Number);
    if (!year || !month || !day) {
      return null;
    }
    return new Date(year, month - 1, day);
  }

  function isToday(date: Date) {
    return isSameDay(date, new Date());
  }

  function formatDueLabel(dueDate?: string | null, dueTime?: string | null) {
    const parsed = parseApiDate(dueDate);
    if (!parsed) {
      return null;
    }
    const date = parsed.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    let dayLabel = parsed.toLocaleDateString(undefined, { weekday: 'short' });
    if (isToday(parsed)) {
      dayLabel = 'Today';
    } else if (isSameDay(parsed, getTomorrowDate())) {
      dayLabel = 'Tomorrow';
    }
    return dueTime ? `${dayLabel} · ${date} · ${dueTime}` : `${dayLabel} · ${date}`;
  }

  const timeOptions = [
    { label: strings.timeMorning, value: '08:00' },
    { label: strings.timeAfternoon, value: '13:00' },
    { label: strings.timeEvening, value: '18:00' },
  ];

  const selectedTodo = detailsTodoId
    ? todos.items.find((item) => item.id === detailsTodoId)
    : null;
  const canAddTodo = text.trim().length > 0;
  const horizontalGutter = theme.spacing.sm * 2;
  const modalWidth = Math.max(280, Math.min(760, viewportWidth - horizontalGutter));

  useEffect(() => {
    if (!selectedTodo) {
      return;
    }
    setDetailText(selectedTodo.text);
    const parsedDetailDate = parseApiDate(selectedTodo.dueDate);
    const derivedScope = deriveScopeStateFromItem(selectedTodo);
    setDetailTime(selectedTodo.dueTime ?? null);
    if (derivedScope.scope === 'DAY') {
      const day = parseApiDate(derivedScope.scopeValue);
      setDetailScope('DAY');
      setDetailDate(day);
      setDetailWeekStart(day ? getStartOfWeekMonday(day) : new Date(selectedWeeklyStart.getTime()));
      setDetailMonthDate(day ? new Date(day.getFullYear(), day.getMonth(), 1) : new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1));
      return;
    }
    if (derivedScope.scope === 'WEEK') {
      setDetailScope('WEEK');
      setDetailDate(null);
      setDetailTime(null);
      setDetailWeekStart(getMondayOfIsoWeek(derivedScope.scopeValue.year, derivedScope.scopeValue.week));
      setDetailMonthDate(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1));
      return;
    }
    if (derivedScope.scope === 'MONTH') {
      setDetailScope('MONTH');
      setDetailDate(null);
      setDetailTime(null);
      setDetailMonthDate(new Date(derivedScope.scopeValue.year, derivedScope.scopeValue.month - 1, 1));
      setDetailWeekStart(new Date(selectedWeeklyStart.getTime()));
      return;
    }
    setDetailDate(parsedDetailDate);
    setDetailScope('LATER');
    setDetailWeekStart(new Date(selectedWeeklyStart.getTime()));
    setDetailMonthDate(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1));
  }, [selectedTodo, selectedWeeklyStart, calendarMonth]);

  async function handleSaveDetails() {
    if (!selectedTodo || savingDetails) {
      return;
    }
    if (!detailText.trim()) {
      return;
    }
    setSavingDetails(true);
    const scheduling = buildSchedulingPayload(detailScope, detailDate, detailTime, detailWeekStart, detailMonthDate);
    const updated = await todos.update(selectedTodo.id, detailText.trim(), scheduling);
    setSavingDetails(false);
    if (updated) {
      setDetailsTodoId(null);
    }
  }

  function clearDetailDate() {
    setDetailScope('LATER');
    setDetailDate(null);
    setDetailTime(null);
  }

  function applyScopeSelection(target: Exclude<ScopePickerTarget, null>, scope: FrontendTodoScope) {
    if (target === 'ADD') {
      setPendingScope(scope);
      if (scope === 'DAY') {
        const nextDate = pendingDate ?? new Date(selectedDailyDate.getTime());
        nextDate.setHours(0, 0, 0, 0);
        setPendingDate(nextDate);
      } else {
        setPendingDate(null);
        setPendingTime(null);
      }
      if (scope === 'WEEK') {
        setPendingWeekStart(new Date(selectedWeeklyStart.getTime()));
      }
      if (scope === 'MONTH') {
        setPendingMonthDate(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1));
      }
      if (scope === 'LATER') {
        setPendingDate(null);
        setPendingTime(null);
      }
      return;
    }
    setDetailScope(scope);
    if (scope === 'DAY') {
      const nextDate = detailDate ?? new Date(selectedDailyDate.getTime());
      nextDate.setHours(0, 0, 0, 0);
      setDetailDate(nextDate);
    } else {
      setDetailDate(null);
      setDetailTime(null);
    }
    if (scope === 'WEEK') {
      setDetailWeekStart(new Date(selectedWeeklyStart.getTime()));
    }
    if (scope === 'MONTH') {
      setDetailMonthDate(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1));
    }
  }

  function shiftCalendarMonth(delta: number) {
    setCalendarMonth((current) => new Date(current.getFullYear(), current.getMonth() + delta, 1));
  }

  function formatCalendarMonth(value: Date) {
    return value.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  }

  function shiftSelectedDailyDate(delta: number) {
    setSelectedDailyDate((current) => {
      const next = new Date(current.getTime());
      next.setDate(next.getDate() + delta);
      next.setHours(0, 0, 0, 0);
      return next;
    });
  }

  function formatSelectedDailyDate(value: Date) {
    const dateLabel = value.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
    });
    if (isToday(value)) {
      return `Today · ${dateLabel}`;
    }
    if (isSameDay(value, getTomorrowDate())) {
      return `Tomorrow · ${dateLabel}`;
    }
    return value.toLocaleDateString(undefined, {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
    });
  }

  function getTomorrowDate() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    return tomorrow;
  }

  function getIsoWeekInfo(date: Date) {
    const target = new Date(date.getTime());
    target.setHours(0, 0, 0, 0);
    const day = target.getDay() || 7;
    target.setDate(target.getDate() + 4 - day);
    const yearStart = new Date(target.getFullYear(), 0, 1);
    const diffMs = target.getTime() - yearStart.getTime();
    const week = Math.ceil(((diffMs / 86400000) + 1) / 7);
    return { week, year: target.getFullYear() };
  }

  function getIsoWeekLabel(date: Date) {
    const { week, year } = getIsoWeekInfo(date);
    return `Week ${week} · ${year}`;
  }

  function formatMonthScopeLabel(date: Date) {
    return date.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  }

  function formatDayScopeLabel(dayDate: Date) {
    const pretty = dayDate.toLocaleDateString(undefined, {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
    });
    if (isToday(dayDate)) {
      return `${strings.forToday} · ${pretty}`;
    }
    if (isSameDay(dayDate, getTomorrowDate())) {
      return `${strings.forTomorrow} · ${pretty}`;
    }
    return pretty;
  }

  function formatWeekScopeLabel(weekStartDate: Date) {
    const weekEnd = new Date(weekStartDate.getTime());
    weekEnd.setDate(weekEnd.getDate() + 6);
    const { week, year } = getIsoWeekInfo(weekStartDate);
    const range = `${weekStartDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}–${weekEnd.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}`;
    const currentWeekStart = getStartOfWeekMonday(new Date());
    if (isSameDay(weekStartDate, currentWeekStart)) {
      return `${strings.forWeek} · Week ${week} (${range})`;
    }
    return `Week ${week} · ${year} (${range})`;
  }

  function formatForValue(scope: FrontendTodoScope, dayDate: Date | null, weekStartDate: Date, monthDate: Date) {
    if (scope === 'DAY' && dayDate) {
      return formatDayScopeLabel(dayDate);
    }
    if (scope === 'WEEK') {
      return formatWeekScopeLabel(weekStartDate);
    }
    if (scope === 'MONTH') {
      const label = formatMonthScopeLabel(monthDate);
      const now = new Date();
      const isCurrentMonth = monthDate.getFullYear() === now.getFullYear() && monthDate.getMonth() === now.getMonth();
      return isCurrentMonth ? `${strings.forMonth} · ${label}` : label;
    }
    return strings.forLater;
  }

  function formatTimeValue(value: string | null) {
    if (!value) {
      return `${strings.timeValuePrefix} · ${strings.timeNone}`;
    }
    const preset = timeOptions.find((option) => option.value === value);
    return `${strings.timeValuePrefix} · ${preset ? preset.label : value}`;
  }

  function applyAddDefaultScopeFromView() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (timeView === 'DAILY') {
      setPendingScope('DAY');
      setPendingDate(new Date(selectedDailyDate.getTime()));
      setPendingWeekStart(getStartOfWeekMonday(selectedDailyDate));
      setPendingMonthDate(new Date(selectedDailyDate.getFullYear(), selectedDailyDate.getMonth(), 1));
      return;
    }
    if (timeView === 'WEEKLY') {
      setPendingScope('WEEK');
      setPendingDate(null);
      setPendingTime(null);
      setPendingWeekStart(new Date(selectedWeeklyStart.getTime()));
      setPendingMonthDate(new Date(selectedWeeklyStart.getFullYear(), selectedWeeklyStart.getMonth(), 1));
      return;
    }
    if (timeView === 'MONTHLY') {
      setPendingScope('MONTH');
      setPendingDate(null);
      setPendingTime(null);
      setPendingMonthDate(new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1));
      setPendingWeekStart(getStartOfWeekMonday(calendarMonth));
      return;
    }
    setPendingScope('LATER');
    setPendingDate(null);
    setPendingTime(null);
    setPendingWeekStart(getStartOfWeekMonday(today));
    setPendingMonthDate(new Date(today.getFullYear(), today.getMonth(), 1));
  }

  function shiftSelectedWeek(delta: number) {
    setSelectedWeeklyStart((current) => {
      const next = new Date(current.getTime());
      next.setDate(next.getDate() + delta * 7);
      next.setHours(0, 0, 0, 0);
      return getStartOfWeekMonday(next);
    });
  }

  function formatWeekRangeLabel(weekStartDate: Date) {
    const weekEnd = new Date(weekStartDate.getTime());
    weekEnd.setDate(weekEnd.getDate() + 6);
    const start = weekStartDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    const end = weekEnd.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    return `${start} - ${end}`;
  }

  function getFrontendScope(item: (typeof todos.items)[number]): FrontendScopeState {
    return deriveScopeStateFromItem(item);
  }

  function openScopeDatePicker(context: TodoDatePickerContext) {
    Keyboard.dismiss();
    switch (context) {
      case 'ADD_DAY':
        setPendingScope('DAY');
        setShowDatePicker(true);
        return;
      case 'ADD_WEEK':
        setPendingScope('WEEK');
        setShowWeekPicker(true);
        return;
      case 'ADD_MONTH':
        setPendingScope('MONTH');
        setShowMonthPicker(true);
        return;
      case 'EDIT_DAY':
        setDetailScope('DAY');
        setShowDetailDatePicker(true);
        return;
      case 'EDIT_WEEK':
        setDetailScope('WEEK');
        setShowDetailWeekPicker(true);
        return;
      case 'EDIT_MONTH':
        setDetailScope('MONTH');
        setShowDetailMonthPicker(true);
        return;
      default:
        return;
    }
  }

  function applyPickedDateForContext(context: TodoDatePickerContext, value: Date) {
    const picked = new Date(value.getTime());
    picked.setHours(0, 0, 0, 0);
    if (context === 'ADD_DAY') {
      setPendingScope('DAY');
      setPendingDate(picked);
      return;
    }
    if (context === 'ADD_WEEK') {
      setPendingScope('WEEK');
      setPendingDate(null);
      setPendingTime(null);
      setPendingWeekStart(getStartOfWeekMonday(picked));
      return;
    }
    if (context === 'ADD_MONTH') {
      setPendingScope('MONTH');
      setPendingDate(null);
      setPendingTime(null);
      setPendingMonthDate(new Date(picked.getFullYear(), picked.getMonth(), 1));
      return;
    }
    if (context === 'EDIT_DAY') {
      setDetailScope('DAY');
      setDetailDate(picked);
      return;
    }
    if (context === 'EDIT_WEEK') {
      setDetailScope('WEEK');
      setDetailDate(null);
      setDetailTime(null);
      setDetailWeekStart(getStartOfWeekMonday(picked));
      return;
    }
    setDetailScope('MONTH');
    setDetailDate(null);
    setDetailTime(null);
    setDetailMonthDate(new Date(picked.getFullYear(), picked.getMonth(), 1));
  }

  const frontendScopedItems = useMemo(() => {
    return todos.items.map((item) => {
      const scopeState = getFrontendScope(item);
      return {
        ...item,
        parsedDueDate: parseApiDate(item.dueDate),
        frontendScope: scopeState.scope,
        frontendScopeState: scopeState,
      };
    });
  }, [todos.items]);

  const weeklyDays = useMemo(() => {
    return Array.from({ length: 7 }).map((_, index) => {
      const date = new Date(selectedWeeklyStart.getTime());
      date.setDate(date.getDate() + index);
      date.setHours(0, 0, 0, 0);
      return date;
    });
  }, [selectedWeeklyStart]);

  const monthGridCells = useMemo(() => {
    const monthStartDate = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
    monthStartDate.setHours(0, 0, 0, 0);
    const monthGridStartDate = getStartOfWeekMonday(monthStartDate);
    return Array.from({ length: 42 }).map((_, index) => {
      const date = new Date(monthGridStartDate.getTime());
      date.setDate(date.getDate() + index);
      date.setHours(0, 0, 0, 0);
      const isCurrentMonth = date.getMonth() === calendarMonth.getMonth() && date.getFullYear() === calendarMonth.getFullYear();
      return { date, isCurrentMonth };
    });
  }, [calendarMonth]);

  const {
    visibleItems,
    isViewingToday,
    dailyOpenDayItems,
    dailyOpenLaterItems,
    dailyDoneItems,
    dailyTotalCount,
    dailyDoneCount,
    dailyProgressRatio,
    weeklyWeekOpenItems,
    weeklyWeekDoneItems,
    weeklyDayOpenItems,
    weeklyDayDoneItems,
    weeklyTotalCount,
    weeklyDoneCount,
    weeklyProgressRatio,
    weeklyDaySummaryByKey,
    monthlyMonthOpenItems,
    monthlyMonthDoneItems,
    dayTodoCountByDateKey,
  } = useMemo(() => {
    const visible = frontendScopedItems;
    const viewingToday = isToday(selectedDailyDate);
    const weeklyDayKeys = new Set(weeklyDays.map((day) => toDateKey(day)));

    const dailyOpenDay = visible.filter((item) =>
      item.status === 'OPEN'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && isSameDay(item.parsedDueDate, selectedDailyDate));
    const dailyOpenLater = visible.filter((item) => item.status === 'OPEN' && item.frontendScope === 'LATER');
    const dailyDone = visible.filter((item) =>
      item.status === 'COMPLETED'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && isSameDay(item.parsedDueDate, selectedDailyDate));

    const weekOpen = visible.filter((item) => item.status === 'OPEN' && item.frontendScope === 'WEEK');
    const weekDone = visible.filter((item) => item.status === 'COMPLETED' && item.frontendScope === 'WEEK');
    const weeklyDayOpen = visible.filter((item) =>
      item.status === 'OPEN'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && weeklyDayKeys.has(toDateKey(item.parsedDueDate)));
    const weeklyDayDone = visible.filter((item) =>
      item.status === 'COMPLETED'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && weeklyDayKeys.has(toDateKey(item.parsedDueDate)));

    const monthlyOpen = visible.filter((item) => item.status === 'OPEN' && item.frontendScope === 'MONTH');
    const monthlyDone = visible.filter((item) => item.status === 'COMPLETED' && item.frontendScope === 'MONTH');

    const countsByDate = visible.reduce<Record<string, number>>((acc, item) => {
      if (item.frontendScope !== 'DAY' || !item.parsedDueDate) {
        return acc;
      }
      const key = toDateKey(item.parsedDueDate);
      acc[key] = (acc[key] ?? 0) + 1;
      return acc;
    }, {});

    const dTotal = dailyOpenDay.length + dailyDone.length;
    const dDone = dailyDone.length;
    const weeklySummaryByKey = weeklyDays.reduce<Record<string, { open: number; done: number }>>((acc, day) => {
      acc[toDateKey(day)] = { open: 0, done: 0 };
      return acc;
    }, {});
    for (const item of weeklyDayOpen) {
      if (item.parsedDueDate) {
        const key = toDateKey(item.parsedDueDate);
        if (weeklySummaryByKey[key]) {
          weeklySummaryByKey[key].open += 1;
        }
      }
    }
    for (const item of weeklyDayDone) {
      if (item.parsedDueDate) {
        const key = toDateKey(item.parsedDueDate);
        if (weeklySummaryByKey[key]) {
          weeklySummaryByKey[key].done += 1;
        }
      }
    }

    const wTotal = weekOpen.length + weekDone.length + weeklyDayOpen.length + weeklyDayDone.length;
    const wDone = weekDone.length + weeklyDayDone.length;

    return {
      visibleItems: visible,
      isViewingToday: viewingToday,
      dailyOpenDayItems: dailyOpenDay,
      dailyOpenLaterItems: dailyOpenLater,
      dailyDoneItems: dailyDone,
      dailyTotalCount: dTotal,
      dailyDoneCount: dDone,
      dailyProgressRatio: dTotal > 0 ? dDone / dTotal : 0,
      weeklyWeekOpenItems: weekOpen,
      weeklyWeekDoneItems: weekDone,
      weeklyDayOpenItems: weeklyDayOpen,
      weeklyDayDoneItems: weeklyDayDone,
      weeklyTotalCount: wTotal,
      weeklyDoneCount: wDone,
      weeklyProgressRatio: wTotal > 0 ? wDone / wTotal : 0,
      weeklyDaySummaryByKey: weeklySummaryByKey,
      monthlyMonthOpenItems: monthlyOpen,
      monthlyMonthDoneItems: monthlyDone,
      dayTodoCountByDateKey: countsByDate,
    };
  }, [frontendScopedItems, selectedDailyDate, weeklyDays]);

  function renderTodoRow(item: (typeof visibleItems)[number], variant: 'default' | 'daily' = 'default') {
    const dueLabel = formatDueLabel(item.dueDate, item.dueTime);
    return (
      <View key={item.id} style={[styles.itemRow, variant === 'daily' ? styles.itemRowDaily : null]}>
        <Pressable
          style={styles.checkboxPressable}
          onPress={() => todos.complete(item.id)}
        >
          <View style={[styles.checkbox, item.status === 'COMPLETED' ? styles.checkboxChecked : null]}>
            {item.status === 'COMPLETED' ? <Text style={styles.checkboxMark}>✓</Text> : null}
          </View>
        </Pressable>
        <View style={styles.itemInfo}>
          <Text style={[styles.itemText, item.status === 'COMPLETED' ? styles.itemTextDone : null]}>
            {item.text}
          </Text>
          {dueLabel ? <Text style={styles.itemMeta}>{dueLabel}</Text> : null}
        </View>
        <Pressable style={styles.detailZone} onPress={() => setDetailsTodoId(item.id)}>
          <Text style={styles.itemHintText}>{strings.details}</Text>
          <Text style={styles.itemHintChevron}>›</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <AppScreen scroll={false} contentStyle={styles.screenContent}>
      <View style={styles.mainLayout}>
        <TopBar
          title={strings.title}
          subtitle={strings.subtitle}
          left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
        />

        <ScrollView
          style={styles.mainScroll}
          contentContainerStyle={styles.mainScrollContent}
          refreshControl={<RefreshControl refreshing={todos.loading} onRefresh={todos.reload} />}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
        >
          <View style={styles.contentOffset}>
        <AppCard>
          <SectionTitle>{strings.viewTitle}</SectionTitle>
          <View style={styles.filters}>
            <AppChip label={strings.daily} active={timeView === 'DAILY'} onPress={() => setTimeView('DAILY')} />
            <AppChip label={strings.weekly} active={timeView === 'WEEKLY'} onPress={() => setTimeView('WEEKLY')} />
            <AppChip label={strings.monthly} active={timeView === 'MONTHLY'} onPress={() => setTimeView('MONTHLY')} />
          </View>
        </AppCard>

        {timeView === 'DAILY' ? (
          <AppCard>
            <View style={styles.calendarMonthRow}>
              <AppButton title={strings.dayBack} onPress={() => shiftSelectedDailyDate(-1)} variant="ghost" />
              <Text style={styles.calendarMonthText}>{formatSelectedDailyDate(selectedDailyDate)}</Text>
              <AppButton title={strings.dayNext} onPress={() => shiftSelectedDailyDate(1)} variant="ghost" />
            </View>
            {dailyTotalCount > 0 ? (
              <View style={styles.dailyProgressBlock}>
                <Subtle>{`${dailyDoneCount} / ${dailyTotalCount} completed`}</Subtle>
                <View style={styles.weekProgressTrack}>
                  <View style={[styles.weekProgressFill, { width: `${Math.round(dailyProgressRatio * 100)}%` }]} />
                </View>
              </View>
            ) : null}
          </AppCard>
        ) : null}

        {timeView === 'MONTHLY' ? (
          <AppCard>
            <View style={styles.calendarMonthRow}>
              <AppButton title={strings.monthBack} onPress={() => shiftCalendarMonth(-1)} variant="ghost" />
              <Text style={styles.calendarMonthText}>{formatCalendarMonth(calendarMonth)}</Text>
              <AppButton title={strings.monthNext} onPress={() => shiftCalendarMonth(1)} variant="ghost" />
            </View>
          </AppCard>
        ) : null}
        {timeView === 'WEEKLY' ? (
          <AppCard>
            <View style={styles.calendarMonthRow}>
              <AppButton title={strings.weekBack} onPress={() => shiftSelectedWeek(-1)} variant="ghost" />
              <Text style={styles.calendarMonthText}>{formatWeekRangeLabel(selectedWeeklyStart)}</Text>
              <AppButton title={strings.weekNext} onPress={() => shiftSelectedWeek(1)} variant="ghost" />
            </View>
            {weeklyTotalCount > 0 ? (
              <View style={styles.dailyProgressBlock}>
                <Subtle>{`${weeklyDoneCount} / ${weeklyTotalCount} completed`}</Subtle>
                <View style={styles.weekProgressTrack}>
                  <View style={[styles.weekProgressFill, { width: `${Math.round(weeklyProgressRatio * 100)}%` }]} />
                </View>
              </View>
            ) : null}
          </AppCard>
        ) : null}

        {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}

        {timeView === 'DAILY' ? (
          <View style={styles.dailyListSurface}>
            <View style={styles.list}>
              {dailyOpenDayItems.length === 0
                && dailyDoneItems.length === 0
                && (!isViewingToday || dailyOpenLaterItems.length === 0)
                && !todos.loading ? (
                <Subtle>{strings.noTodos}</Subtle>
              ) : null}

              {dailyOpenDayItems.length > 0 ? (
                <View style={styles.todoSection}>
                  <View style={styles.list}>
                    {dailyOpenDayItems.map((item) => renderTodoRow(item, 'daily'))}
                  </View>
                </View>
              ) : null}

              {dailyDoneItems.length > 0 ? (
                <View style={styles.todoSection}>
                  <Pressable
                    style={styles.collapsibleHeader}
                    onPress={() => setIsDailyDoneExpanded((value) => !value)}
                  >
                    <Text style={styles.todoSectionTitle}>Done ({dailyDoneItems.length})</Text>
                    <Text style={styles.itemHintChevron}>{isDailyDoneExpanded ? '⌄' : '›'}</Text>
                  </Pressable>
                  {isDailyDoneExpanded ? (
                    <View style={styles.list}>
                      {dailyDoneItems.map((item) => renderTodoRow(item, 'daily'))}
                    </View>
                  ) : null}
                </View>
              ) : null}
            </View>
          </View>
        ) : (
        <AppCard>
          {timeView === 'WEEKLY' ? (
            <View style={styles.list}>
              {weeklyWeekOpenItems.length + weeklyWeekDoneItems.length > 0 ? (
                <Pressable
                  style={styles.unplannedShortcutRow}
                  onPress={() => {
                    Keyboard.dismiss();
                    setShowWeekGoalsSheet(true);
                  }}
                >
                  <Text style={styles.itemText}>{`${strings.weekGoals} (${weeklyWeekOpenItems.length + weeklyWeekDoneItems.length})`}</Text>
                  <Text style={styles.itemHintChevron}>›</Text>
                </Pressable>
              ) : null}

              {weeklyDays.map((dayDate) => {
                const key = toDateKey(dayDate);
                const summary = weeklyDaySummaryByKey[key] ?? { open: 0, done: 0 };
                const dayTotal = summary.open + summary.done;
                const label = dayDate.toLocaleDateString(undefined, { weekday: 'short' });
                const isTodayRow = isToday(dayDate);
                return (
                  <Pressable
                    key={key}
                    style={[styles.weekDayOverviewRow, dayTotal === 0 ? styles.weekDayOverviewRowEmpty : null]}
                    onPress={() => {
                      setSelectedDailyDate(new Date(dayDate.getTime()));
                      setTimeView('DAILY');
                    }}
                  >
                    <Text style={[styles.weekDayOverviewText, isTodayRow ? styles.weekDayOverviewTextToday : null]}>
                      {isTodayRow ? 'Today' : label}
                    </Text>
                    <Text style={styles.itemMeta}>
                      {dayTotal === 0
                        ? '0'
                        : `${dayTotal} task${dayTotal === 1 ? '' : 's'} · ${summary.done} done`}
                    </Text>
                    <Text style={styles.itemHintChevron}>›</Text>
                  </Pressable>
                );
              })}
            </View>
          ) : timeView === 'MONTHLY' ? (
            <View style={styles.list}>
              <View style={styles.todoSection}>
                <View style={styles.monthGridHeaderRow}>
                  {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((label) => (
                    <Text key={label} style={styles.monthGridWeekday}>{label}</Text>
                  ))}
                </View>
                <View style={styles.monthGrid}>
                  {Array.from({ length: 6 }).map((_, rowIndex) => (
                    <View key={`month-row-${rowIndex}`} style={styles.monthGridRow}>
                      {monthGridCells.slice(rowIndex * 7, rowIndex * 7 + 7).map(({ date, isCurrentMonth }) => {
                        const key = toDateKey(date);
                        const count = dayTodoCountByDateKey[key] ?? 0;
                        const isCurrentDay = isCurrentMonth && isToday(date);
                        if (!isCurrentMonth) {
                          return (
                            <View key={key} style={[styles.monthGridCellBase, styles.monthCell, styles.monthCellPlaceholder]}>
                              <Text style={styles.monthCellGhostText}>{date.getDate()}</Text>
                              <View style={styles.monthCellGhostBadge} />
                            </View>
                          );
                        }
                        return (
                          <Pressable
                            key={key}
                            style={[styles.monthGridCellBase, styles.monthCell, isCurrentDay ? styles.monthCellToday : null]}
                            onPress={() => {
                              const next = new Date(date.getTime());
                              next.setHours(0, 0, 0, 0);
                              setSelectedDailyDate(next);
                              setTimeView('DAILY');
                            }}
                          >
                            <Text style={[styles.monthCellText, isCurrentDay ? styles.monthCellTextToday : null]}>{date.getDate()}</Text>
                            {count > 0 ? (
                              <View style={styles.monthCountBadge}>
                                <Text style={styles.monthCountBadgeText}>{count}</Text>
                              </View>
                            ) : null}
                          </Pressable>
                        );
                      })}
                    </View>
                  ))}
                </View>
              </View>

              {monthlyMonthOpenItems.length + monthlyMonthDoneItems.length > 0 ? (
                <Pressable
                  style={styles.unplannedShortcutRow}
                  onPress={() => {
                    Keyboard.dismiss();
                    setShowMonthGoalsSheet(true);
                  }}
                >
                  <Text style={styles.itemText}>{`${strings.monthGoals} (${monthlyMonthOpenItems.length + monthlyMonthDoneItems.length})`}</Text>
                  <Text style={styles.itemHintChevron}>›</Text>
                </Pressable>
              ) : null}
            </View>
          ) : (
            <>
              <Subtle>Unsupported view</Subtle>
            </>
          )}
        </AppCard>
        )}
        {timeView === 'DAILY' && isViewingToday && dailyOpenLaterItems.length > 0 ? (
          <AppCard>
            <Pressable
              style={styles.unplannedShortcutRow}
              onPress={() => {
                Keyboard.dismiss();
                setShowUnplannedSheet(true);
              }}
            >
              <Text style={styles.itemText}>{`${strings.unplannedTitle} (${dailyOpenLaterItems.length})`}</Text>
              <Text style={styles.itemHintChevron}>›</Text>
            </Pressable>
          </AppCard>
        ) : null}
          </View>
        </ScrollView>

        <View style={styles.bottomComposerContainer}>
          <View style={styles.bottomComposerBar}>
            <AppInput
              value={text}
              placeholder={strings.addPlaceholder}
              onChangeText={setText}
              onFocus={() => {
                applyAddDefaultScopeFromView();
                setShowAddTodoSheet(true);
              }}
              showSoftInputOnFocus={false}
            />
          </View>
        </View>
      </View>

        {showAddTodoSheet ? (
          <OverlaySheet onClose={closeAddTodoSheet} sheetStyle={styles.sheet}>
            <View style={styles.sheetHandle} />
            <View style={styles.sheetLayout}>
              <ScrollView
                style={styles.detailScroll}
                contentContainerStyle={styles.detailContent}
                keyboardShouldPersistTaps="handled"
                showsVerticalScrollIndicator={false}
                showsHorizontalScrollIndicator={false}
                horizontal={false}
                bounces={false}
                alwaysBounceHorizontal={false}
              >
                <View style={styles.detailHeader}>
                  <Text style={textStyles.h3}>{strings.addTodoTitle}</Text>
                  {!isKeyboardVisible ? <Subtle>{strings.editSubtitle}</Subtle> : null}
                </View>

                <View style={styles.detailSection}>
                  <AppInput
                    value={text}
                    placeholder={strings.addPlaceholder}
                    onChangeText={setText}
                    autoFocus
                  />
                </View>

                <View style={styles.detailSection}>
                  <Pressable
                    style={styles.selectionRow}
                    onPress={() => {
                      Keyboard.dismiss();
                      setScopePickerTarget('ADD');
                    }}
                  >
                    <Text style={styles.selectionRowValue}>
                      {formatForValue(pendingScope, pendingDate, pendingWeekStart, pendingMonthDate)}
                    </Text>
                    <Text style={styles.itemHintChevron}>›</Text>
                  </Pressable>
                {pendingScope === 'DAY' && pendingDate ? (
                  <View style={styles.detailSubsection}>
                    <Subtle>{formatTimeValue(pendingTime)}</Subtle>
                    <View style={styles.quickDateChips}>
                      {timeOptions.map((option) => (
                        <AppChip
                          key={option.value}
                          label={option.label}
                          active={pendingTime === option.value}
                          onPress={() => setPendingTime(option.value)}
                        />
                      ))}
                      <AppChip
                        label={strings.timePick}
                        active={showTimePicker}
                        onPress={() => setShowTimePicker(true)}
                      />
                      <AppChip
                        label={strings.timeNone}
                        active={!pendingTime}
                        onPress={() => setPendingTime(null)}
                      />
                    </View>
                  </View>
                ) : null}
              </View>
              </ScrollView>

              <View style={styles.sheetFooterActions}>
                <AppButton
                  title={todos.loading ? strings.adding : strings.addAction}
                  onPress={handleAdd}
                  fullWidth
                  disabled={todos.loading || !canAddTodo}
                />
                {!isKeyboardVisible ? (
                  <AppButton title={strings.close} onPress={closeAddTodoSheet} variant="ghost" fullWidth />
                ) : null}
              </View>
            </View>
          </OverlaySheet>
        ) : null}

        {showUnplannedSheet ? (
          <OverlaySheet onClose={() => setShowUnplannedSheet(false)} sheetStyle={[styles.sheet, styles.detailSheet]}>
            <View style={styles.sheetHandle} />
            <View style={styles.sheetLayout}>
              <ScrollView
                style={styles.detailScroll}
                contentContainerStyle={styles.detailContent}
                keyboardShouldPersistTaps="handled"
                showsVerticalScrollIndicator={false}
              >
                <View style={styles.detailHeader}>
                  <Text style={textStyles.h3}>{strings.unplannedTitle}</Text>
                  <Subtle>Open todos without a schedule.</Subtle>
                </View>
                {dailyOpenLaterItems.length > 0 ? (
                  <View style={styles.list}>
                    {dailyOpenLaterItems.map((item) => renderTodoRow(item))}
                  </View>
                ) : (
                  <Subtle>{strings.noUnplannedItems}</Subtle>
                )}
              </ScrollView>
              <View style={styles.sheetFooterActions}>
                <AppButton title={strings.close} onPress={() => setShowUnplannedSheet(false)} variant="ghost" fullWidth />
              </View>
            </View>
          </OverlaySheet>
        ) : null}

        {showWeekGoalsSheet ? (
          <OverlaySheet onClose={() => setShowWeekGoalsSheet(false)} sheetStyle={[styles.sheet, styles.detailSheet]}>
            <View style={styles.sheetHandle} />
            <View style={styles.sheetLayout}>
              <ScrollView
                style={styles.detailScroll}
                contentContainerStyle={styles.detailContent}
                keyboardShouldPersistTaps="handled"
                showsVerticalScrollIndicator={false}
              >
                <View style={styles.detailHeader}>
                  <Text style={textStyles.h3}>{strings.weekGoals}</Text>
                  <Subtle>{formatWeekRangeLabel(selectedWeeklyStart)}</Subtle>
                </View>
                {weeklyWeekOpenItems.length > 0 ? (
                  <View style={styles.list}>{weeklyWeekOpenItems.map((item) => renderTodoRow(item))}</View>
                ) : null}
                {weeklyWeekDoneItems.length > 0 ? (
                  <View style={styles.todoSection}>
                    <Text style={styles.todoSectionTitle}>{`Done (${weeklyWeekDoneItems.length})`}</Text>
                    <View style={styles.list}>{weeklyWeekDoneItems.map((item) => renderTodoRow(item))}</View>
                  </View>
                ) : null}
              </ScrollView>
              <View style={styles.sheetFooterActions}>
                <AppButton title={strings.close} onPress={() => setShowWeekGoalsSheet(false)} variant="ghost" fullWidth />
              </View>
            </View>
          </OverlaySheet>
        ) : null}

        {showMonthGoalsSheet ? (
          <OverlaySheet onClose={() => setShowMonthGoalsSheet(false)} sheetStyle={[styles.sheet, styles.detailSheet]}>
            <View style={styles.sheetHandle} />
            <View style={styles.sheetLayout}>
              <ScrollView
                style={styles.detailScroll}
                contentContainerStyle={styles.detailContent}
                keyboardShouldPersistTaps="handled"
                showsVerticalScrollIndicator={false}
              >
                <View style={styles.detailHeader}>
                  <Text style={textStyles.h3}>{strings.monthGoals}</Text>
                  <Subtle>{formatCalendarMonth(calendarMonth)}</Subtle>
                </View>
                {monthlyMonthOpenItems.length > 0 ? (
                  <View style={styles.list}>{monthlyMonthOpenItems.map((item) => renderTodoRow(item))}</View>
                ) : null}
                {monthlyMonthDoneItems.length > 0 ? (
                  <View style={styles.todoSection}>
                    <Text style={styles.todoSectionTitle}>{`Done (${monthlyMonthDoneItems.length})`}</Text>
                    <View style={styles.list}>{monthlyMonthDoneItems.map((item) => renderTodoRow(item))}</View>
                  </View>
                ) : null}
              </ScrollView>
              <View style={styles.sheetFooterActions}>
                <AppButton title={strings.close} onPress={() => setShowMonthGoalsSheet(false)} variant="ghost" fullWidth />
              </View>
            </View>
          </OverlaySheet>
        ) : null}

        {showDatePicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={pendingDate ?? new Date()}
              mode="date"
              onChange={(event, date) => {
                setShowDatePicker(false);
                if (event.type !== 'set' || !date) {
                  return;
                }
                applyPickedDateForContext('ADD_DAY', date);
              }}
            />
          ) : (
          <Pressable style={styles.backdrop} onPress={() => setShowDatePicker(false)}>
            <Pressable style={[styles.sheet, { width: modalWidth }]} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.pickDateTitle}</Text>
              <View style={styles.pickerList}>
                {Array.from({ length: 7 }).map((_, idx) => {
                  const date = new Date();
                  date.setDate(date.getDate() + idx);
                  const label = date.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
                  return (
                    <Pressable
                      key={label}
                      style={styles.pickerRow}
                      onPress={() => {
                        date.setHours(0, 0, 0, 0);
                        setPendingScope('DAY');
                        setPendingDate(date);
                        setShowDatePicker(false);
                      }}
                    >
                      <Text style={styles.itemText}>{label}</Text>
                    </Pressable>
                  );
                })}
              </View>
            </Pressable>
          </Pressable>
          )
        ) : null}

        {showWeekPicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={pendingWeekStart}
              mode="date"
              onChange={(event, value) => {
                setShowWeekPicker(false);
                if (event.type !== 'set' || !value) {
                  return;
                }
                applyPickedDateForContext('ADD_WEEK', value);
              }}
            />
          ) : (
            <Pressable style={styles.backdrop} onPress={() => setShowWeekPicker(false)}>
              <Pressable style={[styles.sheet, { width: modalWidth }]} onPress={() => null}>
                <View style={styles.sheetHandle} />
                <Text style={textStyles.h3}>{strings.pickWeekTitle}</Text>
                <View style={styles.pickerList}>
                  {Array.from({ length: 12 }).map((_, idx) => {
                    const value = new Date(selectedWeeklyStart.getTime());
                    value.setDate(value.getDate() + idx * 7);
                    const weekStart = getStartOfWeekMonday(value);
                    const label = formatWeekScopeLabel(weekStart);
                    return (
                      <Pressable
                        key={`${weekStart.toISOString()}-week`}
                        style={styles.pickerRow}
                        onPress={() => {
                          applyPickedDateForContext('ADD_WEEK', weekStart);
                          setShowWeekPicker(false);
                        }}
                      >
                        <Text style={styles.itemText}>{label}</Text>
                      </Pressable>
                    );
                  })}
                </View>
              </Pressable>
            </Pressable>
          )
        ) : null}

        {showMonthPicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={pendingMonthDate}
              mode="date"
              onChange={(event, value) => {
                setShowMonthPicker(false);
                if (event.type !== 'set' || !value) {
                  return;
                }
                applyPickedDateForContext('ADD_MONTH', value);
              }}
            />
          ) : (
            <Pressable style={styles.backdrop} onPress={() => setShowMonthPicker(false)}>
              <Pressable style={[styles.sheet, { width: modalWidth }]} onPress={() => null}>
                <View style={styles.sheetHandle} />
                <Text style={textStyles.h3}>{strings.pickMonthTitle}</Text>
                <View style={styles.pickerList}>
                  {Array.from({ length: 12 }).map((_, idx) => {
                    const value = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + idx, 1);
                    const label = formatForValue('MONTH', null, getStartOfWeekMonday(value), value);
                    return (
                      <Pressable
                        key={`${value.getFullYear()}-${value.getMonth()}-month`}
                        style={styles.pickerRow}
                        onPress={() => {
                          applyPickedDateForContext('ADD_MONTH', value);
                          setShowMonthPicker(false);
                        }}
                      >
                        <Text style={styles.itemText}>{label}</Text>
                      </Pressable>
                    );
                  })}
                </View>
              </Pressable>
            </Pressable>
          )
        ) : null}

        {showTimePicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={(() => {
                const value = new Date();
                if (pendingTime) {
                  const [hours, minutes] = pendingTime.split(':').map(Number);
                  value.setHours(hours ?? 0, minutes ?? 0, 0, 0);
                }
                return value;
              })()}
              mode="time"
              is24Hour
              onChange={(event, value) => {
                setShowTimePicker(false);
                if (event.type !== 'set' || !value) {
                  return;
                }
                const hours = String(value.getHours()).padStart(2, '0');
                const minutes = String(value.getMinutes()).padStart(2, '0');
                setPendingTime(`${hours}:${minutes}`);
              }}
            />
          ) : (
          <Pressable style={styles.backdrop} onPress={() => setShowTimePicker(false)}>
            <Pressable style={styles.sheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.pickTimeTitle}</Text>
              <View style={styles.pickerList}>
                {['08:00', '12:00', '16:00', '20:00'].map((time) => (
                  <Pressable
                    key={time}
                    style={styles.pickerRow}
                    onPress={() => {
                      setPendingTime(time);
                      setShowTimePicker(false);
                    }}
                  >
                    <Text style={styles.itemText}>{time}</Text>
                  </Pressable>
                ))}
              </View>
            </Pressable>
          </Pressable>
          )
        ) : null}

        {showDetailDatePicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={detailDate ?? new Date()}
              mode="date"
              onChange={(event, value) => {
                setShowDetailDatePicker(false);
                        if (event.type !== 'set' || !value) {
                  return;
                }
                applyPickedDateForContext('EDIT_DAY', value);
              }}
            />
          ) : (
          <Pressable style={styles.backdrop} onPress={() => setShowDetailDatePicker(false)}>
            <Pressable style={styles.sheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.pickDateTitle}</Text>
              <View style={styles.pickerList}>
                {Array.from({ length: 7 }).map((_, idx) => {
                  const date = new Date();
                  date.setDate(date.getDate() + idx);
                  const label = date.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
                  return (
                    <Pressable
                      key={label}
                      style={styles.pickerRow}
                      onPress={() => {
                        date.setHours(0, 0, 0, 0);
                        setDetailScope('DAY');
                        setDetailDate(date);
                        setShowDetailDatePicker(false);
                      }}
                    >
                      <Text style={styles.itemText}>{label}</Text>
                    </Pressable>
                  );
                })}
              </View>
            </Pressable>
          </Pressable>
          )
        ) : null}

        {showDetailWeekPicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={detailWeekStart}
              mode="date"
              onChange={(event, value) => {
                setShowDetailWeekPicker(false);
                if (event.type !== 'set' || !value) {
                  return;
                }
                applyPickedDateForContext('EDIT_WEEK', value);
              }}
            />
          ) : (
            <Pressable style={styles.backdrop} onPress={() => setShowDetailWeekPicker(false)}>
              <Pressable style={[styles.sheet, { width: modalWidth }]} onPress={() => null}>
                <View style={styles.sheetHandle} />
                <Text style={textStyles.h3}>{strings.pickWeekTitle}</Text>
                <View style={styles.pickerList}>
                  {Array.from({ length: 12 }).map((_, idx) => {
                    const value = new Date(selectedWeeklyStart.getTime());
                    value.setDate(value.getDate() + idx * 7);
                    const weekStart = getStartOfWeekMonday(value);
                    const label = formatWeekScopeLabel(weekStart);
                    return (
                      <Pressable
                        key={`${weekStart.toISOString()}-detail-week`}
                        style={styles.pickerRow}
                        onPress={() => {
                          applyPickedDateForContext('EDIT_WEEK', weekStart);
                          setShowDetailWeekPicker(false);
                        }}
                      >
                        <Text style={styles.itemText}>{label}</Text>
                      </Pressable>
                    );
                  })}
                </View>
              </Pressable>
            </Pressable>
          )
        ) : null}

        {showDetailMonthPicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={detailMonthDate}
              mode="date"
              onChange={(event, value) => {
                setShowDetailMonthPicker(false);
                if (event.type !== 'set' || !value) {
                  return;
                }
                applyPickedDateForContext('EDIT_MONTH', value);
              }}
            />
          ) : (
            <Pressable style={styles.backdrop} onPress={() => setShowDetailMonthPicker(false)}>
              <Pressable style={[styles.sheet, { width: modalWidth }]} onPress={() => null}>
                <View style={styles.sheetHandle} />
                <Text style={textStyles.h3}>{strings.pickMonthTitle}</Text>
                <View style={styles.pickerList}>
                  {Array.from({ length: 12 }).map((_, idx) => {
                    const value = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + idx, 1);
                    const label = formatForValue('MONTH', null, getStartOfWeekMonday(value), value);
                    return (
                      <Pressable
                        key={`${value.getFullYear()}-${value.getMonth()}-detail-month`}
                        style={styles.pickerRow}
                        onPress={() => {
                          applyPickedDateForContext('EDIT_MONTH', value);
                          setShowDetailMonthPicker(false);
                        }}
                      >
                        <Text style={styles.itemText}>{label}</Text>
                      </Pressable>
                    );
                  })}
                </View>
              </Pressable>
            </Pressable>
          )
        ) : null}

        {showDetailTimePicker ? (
          Platform.OS !== 'web' ? (
            <DateTimePicker
              value={(() => {
                const value = new Date();
                if (detailTime) {
                  const [hours, minutes] = detailTime.split(':').map(Number);
                  value.setHours(hours ?? 0, minutes ?? 0, 0, 0);
                }
                return value;
              })()}
              mode="time"
              is24Hour
              onChange={(event, value) => {
                setShowDetailTimePicker(false);
                if (event.type !== 'set' || !value) {
                  return;
                }
                const hours = String(value.getHours()).padStart(2, '0');
                const minutes = String(value.getMinutes()).padStart(2, '0');
                setDetailTime(`${hours}:${minutes}`);
              }}
            />
          ) : (
          <Pressable style={styles.backdrop} onPress={() => setShowDetailTimePicker(false)}>
            <Pressable style={styles.sheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.pickTimeTitle}</Text>
              <View style={styles.pickerList}>
                {['08:00', '12:00', '16:00', '20:00'].map((time) => (
                  <Pressable
                    key={time}
                    style={styles.pickerRow}
                    onPress={() => {
                      setDetailTime(time);
                      setShowDetailTimePicker(false);
                    }}
                  >
                    <Text style={styles.itemText}>{time}</Text>
                  </Pressable>
                ))}
              </View>
            </Pressable>
          </Pressable>
          )
        ) : null}

        {detailsTodoId ? (
          <OverlaySheet onClose={() => setDetailsTodoId(null)} sheetStyle={[styles.sheet, styles.detailSheet]}>
            <View style={styles.sheetHandle} />
            <View style={styles.sheetLayout}>
              <ScrollView
                style={styles.detailScroll}
                contentContainerStyle={styles.detailContent}
                showsVerticalScrollIndicator={false}
                showsHorizontalScrollIndicator={false}
                horizontal={false}
                bounces={false}
                alwaysBounceHorizontal={false}
                keyboardShouldPersistTaps="handled"
              >
                <View style={styles.detailHeader}>
                  <Text style={textStyles.h3}>{strings.editTitle}</Text>
                  {!isKeyboardVisible ? <Subtle>{strings.editSubtitle}</Subtle> : null}
                </View>

                <View style={styles.detailSection}>
                  <AppInput
                    value={detailText}
                    placeholder={strings.addPlaceholder}
                    onChangeText={setDetailText}
                  />
                </View>

                <View style={styles.detailSection}>
                  <Pressable
                    style={styles.selectionRow}
                    onPress={() => {
                      Keyboard.dismiss();
                      setScopePickerTarget('EDIT');
                    }}
                  >
                    <Text style={styles.selectionRowValue}>
                      {formatForValue(detailScope, detailDate, detailWeekStart, detailMonthDate)}
                    </Text>
                    <Text style={styles.itemHintChevron}>›</Text>
                  </Pressable>
                {detailScope === 'DAY' && detailDate ? (
                  <View style={styles.detailSubsection}>
                    <Subtle>{formatTimeValue(detailTime)}</Subtle>
                    <View style={styles.quickDateChips}>
                      {timeOptions.map((option) => (
                        <AppChip
                          key={option.value}
                          label={option.label}
                          active={detailTime === option.value}
                          onPress={() => setDetailTime(option.value)}
                        />
                      ))}
                      <AppChip
                        label={strings.timePick}
                        active={showDetailTimePicker}
                        onPress={() => setShowDetailTimePicker(true)}
                      />
                      <AppChip
                        label={strings.timeNone}
                        active={!detailTime}
                        onPress={() => setDetailTime(null)}
                      />
                    </View>
                  </View>
                ) : null}
              </View>
              </ScrollView>

              <View style={styles.sheetFooterActions}>
                <AppButton
                  title={savingDetails ? strings.savingChanges : strings.saveChanges}
                  onPress={handleSaveDetails}
                  fullWidth
                  disabled={savingDetails || !detailText.trim()}
                />
                {!isKeyboardVisible ? (
                  <AppButton title={strings.close} onPress={() => setDetailsTodoId(null)} variant="ghost" fullWidth />
                ) : null}
              </View>
            </View>
          </OverlaySheet>
        ) : null}

        {scopePickerTarget ? (
          <OverlaySheet onClose={() => setScopePickerTarget(null)} sheetStyle={[styles.sheet, styles.scopePickerSheet]}>
            <View style={styles.sheetHandle} />
            <ScrollView
              style={styles.detailScroll}
              contentContainerStyle={styles.detailContent}
              keyboardShouldPersistTaps="handled"
              showsVerticalScrollIndicator={false}
            >
              <View style={styles.detailHeader}>
                <Text style={textStyles.h3}>{strings.chooseScopeTitle}</Text>
                <Subtle>{strings.chooseScopeSubtitle}</Subtle>
              </View>

              <View style={styles.detailSection}>
                <Pressable style={styles.selectionRow}>
                  <Text style={styles.selectionRowValue}>
                    {scopePickerTarget === 'ADD'
                      ? formatForValue(pendingScope, pendingDate, pendingWeekStart, pendingMonthDate)
                      : formatForValue(detailScope, detailDate, detailWeekStart, detailMonthDate)}
                  </Text>
                </Pressable>
                <Subtle>{strings.chooseScopeSubtitle}</Subtle>
              </View>

              <View style={styles.detailSection}>
                <Text style={styles.quickDateLabel}>{strings.quickDateTitle}</Text>
                <View style={styles.quickDateChips}>
                  <AppChip
                    label={strings.forToday}
                    active={
                      (scopePickerTarget === 'ADD' ? pendingScope : detailScope) === 'DAY'
                      && (scopePickerTarget === 'ADD'
                        ? !!pendingDate && isSameDay(pendingDate, new Date())
                        : !!detailDate && isSameDay(detailDate, new Date()))
                    }
                    onPress={() => (scopePickerTarget === 'ADD' ? setDateToToday() : setDetailDateToToday())}
                  />
                  <AppChip
                    label={strings.forTomorrow}
                    active={
                      (scopePickerTarget === 'ADD' ? pendingScope : detailScope) === 'DAY'
                      && (scopePickerTarget === 'ADD'
                        ? !!pendingDate && isSameDay(pendingDate, getTomorrowDate())
                        : !!detailDate && isSameDay(detailDate, getTomorrowDate()))
                    }
                    onPress={() => (scopePickerTarget === 'ADD' ? setDateToTomorrow() : setDetailDateToTomorrow())}
                  />
                  <AppChip
                    label={strings.quickPick}
                    active={scopePickerTarget === 'ADD' ? showDatePicker : showDetailDatePicker}
                    onPress={() => openScopeDatePicker(scopePickerTarget === 'ADD' ? 'ADD_DAY' : 'EDIT_DAY')}
                  />
                </View>
              </View>

              <View style={styles.detailSection}>
                <Text style={styles.quickDateLabel}>Week</Text>
                <View style={styles.quickDateChips}>
                  <AppChip
                    label={strings.forWeek}
                    active={(scopePickerTarget === 'ADD' ? pendingScope : detailScope) === 'WEEK'
                      && isSameDay(
                        scopePickerTarget === 'ADD' ? pendingWeekStart : detailWeekStart,
                        getStartOfWeekMonday(new Date()),
                      )}
                    onPress={() => (scopePickerTarget === 'ADD' ? setPendingToCurrentWeek() : setDetailToCurrentWeek())}
                  />
                  <AppChip
                    label={strings.pickWeek}
                    active={scopePickerTarget === 'ADD' ? showWeekPicker : showDetailWeekPicker}
                    onPress={() => openScopeDatePicker(scopePickerTarget === 'ADD' ? 'ADD_WEEK' : 'EDIT_WEEK')}
                  />
                </View>
              </View>

              <View style={styles.detailSection}>
                <Text style={styles.quickDateLabel}>Month</Text>
                <View style={styles.quickDateChips}>
                  <AppChip
                    label={strings.forMonth}
                    active={(scopePickerTarget === 'ADD' ? pendingScope : detailScope) === 'MONTH'
                      && (() => {
                        const current = new Date();
                        const selected = scopePickerTarget === 'ADD' ? pendingMonthDate : detailMonthDate;
                        return selected.getFullYear() === current.getFullYear() && selected.getMonth() === current.getMonth();
                      })()}
                    onPress={() => (scopePickerTarget === 'ADD' ? setPendingToCurrentMonth() : setDetailToCurrentMonth())}
                  />
                  <AppChip
                    label={strings.pickMonth}
                    active={scopePickerTarget === 'ADD' ? showMonthPicker : showDetailMonthPicker}
                    onPress={() => openScopeDatePicker(scopePickerTarget === 'ADD' ? 'ADD_MONTH' : 'EDIT_MONTH')}
                  />
                </View>
              </View>

              <View style={styles.detailSection}>
                <Text style={styles.quickDateLabel}>{strings.forLater}</Text>
                <View style={styles.quickDateChips}>
                  <AppChip
                    label={strings.forLater}
                    active={(scopePickerTarget === 'ADD' ? pendingScope : detailScope) === 'LATER'}
                    onPress={() => {
                      applyScopeSelection(scopePickerTarget, 'LATER');
                      setScopePickerTarget(null);
                    }}
                  />
                </View>
              </View>

              <View style={styles.sheetFooterActions}>
                <AppButton title={strings.close} onPress={() => setScopePickerTarget(null)} variant="ghost" fullWidth />
              </View>
            </ScrollView>
          </OverlaySheet>
        ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  screenContent: {
    padding: 0,
    gap: 0,
    flex: 1,
  },
  mainLayout: {
    flex: 1,
  },
  mainScroll: {
    flex: 1,
  },
  mainScrollContent: {
    padding: theme.spacing.lg,
    paddingTop: 90,
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.md,
  },
  contentOffset: {
    gap: theme.spacing.md,
  },
  filters: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  calendarMonthRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  calendarMonthText: {
    ...textStyles.h3,
    textTransform: 'capitalize',
  },
  dailyProgressBlock: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  quickDateRow: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  quickDateLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  quickDateChips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  selectionRow: {
    minHeight: 44,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surface,
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.sm,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  selectionRowValue: {
    ...textStyles.body,
    flex: 1,
  },
  forRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  quickTimeRow: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  list: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
  },
  todoSection: {
    gap: theme.spacing.xs,
  },
  todoSectionTitle: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  collapsibleHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  weekProgressRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  weekProgressTrack: {
    flex: 1,
    height: 8,
    borderRadius: 999,
    backgroundColor: theme.colors.border,
    overflow: 'hidden',
  },
  weekProgressFill: {
    height: '100%',
    borderRadius: 999,
    backgroundColor: theme.colors.primary,
    minWidth: 0,
  },
  weekDayOverviewRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  weekDayOverviewRowEmpty: {
    opacity: 0.75,
  },
  weekDayOverviewText: {
    ...textStyles.body,
    minWidth: 52,
  },
  weekDayOverviewTextToday: {
    fontWeight: '700',
    color: theme.colors.primary,
  },
  unplannedShortcutRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  monthGridHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: theme.spacing.xs,
  },
  monthGridWeekday: {
    ...textStyles.subtle,
    flex: 1,
    textAlign: 'center',
  },
  monthGrid: {
    marginTop: theme.spacing.xs,
    gap: theme.spacing.xs,
  },
  monthGridRow: {
    flexDirection: 'row',
    gap: theme.spacing.xs,
  },
  monthGridCellBase: {
    flex: 1,
    aspectRatio: 1,
  },
  monthCell: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.sm,
    backgroundColor: theme.colors.surfaceAlt,
    padding: 6,
    justifyContent: 'space-between',
  },
  monthCellPlaceholder: {
    backgroundColor: theme.colors.surface,
    opacity: 0.45,
  },
  monthCellGhostText: {
    ...textStyles.subtle,
    color: 'transparent',
  },
  monthCellGhostBadge: {
    alignSelf: 'flex-end',
    minWidth: 20,
    height: 20,
    paddingHorizontal: 5,
  },
  monthCellToday: {
    backgroundColor: theme.colors.primarySoft,
    borderColor: theme.colors.primary,
  },
  monthCellText: {
    ...textStyles.subtle,
    color: theme.colors.text,
  },
  monthCellTextToday: {
    color: theme.colors.primary,
    fontWeight: '700',
  },
  monthCountBadge: {
    alignSelf: 'flex-end',
    minWidth: 20,
    height: 20,
    borderRadius: 999,
    paddingHorizontal: 5,
    backgroundColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  monthCountBadgeText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '700',
  },
  itemRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  itemRowDaily: {
    borderRadius: theme.radius.sm,
    backgroundColor: theme.colors.surface,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
  },
  checkboxPressable: {
    padding: 2,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: theme.colors.borderStrong,
    backgroundColor: theme.colors.surfaceAlt,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    backgroundColor: theme.colors.success,
    borderColor: theme.colors.success,
  },
  checkboxMark: {
    color: '#ffffff',
    fontWeight: '700',
  },
  itemInfo: {
    flex: 1,
  },
  itemText: {
    ...textStyles.body,
  },
  itemTextDone: {
    color: theme.colors.subtle,
    textDecorationLine: 'line-through',
  },
  itemMeta: {
    ...textStyles.subtle,
  },
  detailZone: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingLeft: theme.spacing.sm,
    borderLeftWidth: 1,
    borderLeftColor: theme.colors.border,
  },
  itemHintText: {
    ...textStyles.subtle,
  },
  itemHintChevron: {
    ...textStyles.subtle,
    fontSize: 18,
    lineHeight: 18,
  },
  dailyListSurface: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.lg,
    backgroundColor: theme.colors.surface,
    padding: theme.spacing.md,
  },
  backdrop: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderRadius: 0,
    padding: theme.spacing.lg,
    borderWidth: 1,
    borderColor: theme.colors.border,
    maxWidth: 760,
    maxHeight: Platform.OS === 'web' ? '94%' : '86%',
    alignSelf: 'center',
    overflow: 'hidden',
  },
  detailSheet: {
    maxHeight: Platform.OS === 'web' ? '94%' : '76%',
  },
  scopePickerSheet: {
    maxHeight: Platform.OS === 'web' ? '94%' : '95%',
  },
  sheetHandle: {
    alignSelf: 'center',
    width: 48,
    height: 5,
    borderRadius: 999,
    backgroundColor: theme.colors.borderStrong,
    marginBottom: theme.spacing.sm,
  },
  pickerList: {
    gap: theme.spacing.sm,
  },
  pickerRow: {
    paddingVertical: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  detailScroll: {
    marginTop: theme.spacing.xs,
  },
  detailContent: {
    gap: theme.spacing.md,
    paddingBottom: theme.spacing.sm,
    minWidth: 0,
  },
  detailHeader: {
    gap: theme.spacing.xs,
  },
  detailSection: {
    gap: theme.spacing.sm,
    padding: theme.spacing.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    minWidth: 0,
  },
  detailSubsection: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.xs,
  },
  sheetLayout: {
    maxHeight: '100%',
    flexShrink: 1,
  },
  sheetFooterActions: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
    paddingTop: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
  },
  bottomComposerContainer: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
  },
  bottomComposerBar: {
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.lg,
  },
});
