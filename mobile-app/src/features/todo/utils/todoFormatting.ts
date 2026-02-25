import { getIsoWeekInfoFromDate, getStartOfWeekMonday, getTomorrowDate, isSameDay, isToday, parseApiDate } from './todoDates';

export type TodoScopeLabel = 'DAY' | 'WEEK' | 'MONTH' | 'LATER';

type TodoStrings = {
  forToday: string;
  forTomorrow: string;
  forWeek: string;
  forMonth: string;
  forLater: string;
  timeValuePrefix: string;
  timeNone: string;
};

export function formatDate(date: Date) {
  const day = date.toLocaleDateString(undefined, { weekday: 'short' });
  const dayDate = date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  return `${day} · ${dayDate}`;
}

export function formatCalendarMonth(value: Date) {
  return value.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
}

export function formatSelectedDailyDate(value: Date) {
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
  const weekdayLabel = value.toLocaleDateString(undefined, { weekday: 'long' });
  return `${weekdayLabel} · ${dateLabel}`;
}

export function formatDueLabel(dueDate?: string | null, dueTime?: string | null) {
  const parsed = parseApiDate(dueDate);
  if (!parsed) {
    return null;
  }
  const date = parsed.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  let dayLabel = parsed.toLocaleDateString(undefined, { weekday: 'long' });
  if (isToday(parsed)) {
    dayLabel = 'Today';
  } else if (isSameDay(parsed, getTomorrowDate())) {
    dayLabel = 'Tomorrow';
  }
  const normalizedTime = dueTime ? normalizeTimeValue(dueTime) : null;
  const displayTime = normalizedTime ? formatTimeSlotSummary(normalizedTime) : null;
  return displayTime ? `${dayLabel} · ${date} · ${displayTime}` : `${dayLabel} · ${date}`;
}

export function formatMonthScopeLabel(date: Date) {
  return date.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
}

export function formatDayScopeLabel(dayDate: Date, strings: Pick<TodoStrings, 'forToday' | 'forTomorrow'>) {
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

export function formatWeekScopeLabel(
  weekStartDate: Date,
  strings: Pick<TodoStrings, 'forWeek'>
) {
  const weekEnd = new Date(weekStartDate.getTime());
  weekEnd.setDate(weekEnd.getDate() + 6);
  const { week, year } = getIsoWeekInfoFromDate(weekStartDate);
  const range = `${weekStartDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}–${weekEnd.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}`;
  const currentWeekStart = getStartOfWeekMonday(new Date());
  if (isSameDay(weekStartDate, currentWeekStart)) {
    return `${strings.forWeek} · Week ${week} (${range})`;
  }
  return `Week ${week} · ${year} (${range})`;
}

export function formatWeekRangeLabel(weekStartDate: Date) {
  const weekEnd = new Date(weekStartDate.getTime());
  weekEnd.setDate(weekEnd.getDate() + 6);
  const start = weekStartDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  const end = weekEnd.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  return `${start} - ${end}`;
}

export function formatForValue(
  scope: TodoScopeLabel,
  dayDate: Date | null,
  weekStartDate: Date,
  monthDate: Date,
  strings: TodoStrings
) {
  if (scope === 'DAY' && dayDate) {
    return formatDayScopeLabel(dayDate, strings);
  }
  if (scope === 'WEEK') {
    return formatWeekScopeLabel(weekStartDate, strings);
  }
  if (scope === 'MONTH') {
    const label = formatMonthScopeLabel(monthDate);
    const now = new Date();
    const isCurrentMonth = monthDate.getFullYear() === now.getFullYear() && monthDate.getMonth() === now.getMonth();
    return isCurrentMonth ? `${strings.forMonth} · ${label}` : label;
  }
  return strings.forLater;
}

function normalizeTimeValue(value: string) {
  // Backend may return HH:mm:ss; UI chips use HH:mm.
  return value.length >= 5 ? value.slice(0, 5) : value;
}

function formatTimeSlotSummary(normalizedTime: string) {
  if (normalizedTime === '08:00') {
    return 'Morning (08–12)';
  }
  if (normalizedTime === '12:00' || normalizedTime === '13:00') {
    return 'Afternoon (12–17)';
  }
  if (normalizedTime === '17:00' || normalizedTime === '18:00') {
    return 'Evening (17–21)';
  }
  return normalizedTime;
}

export function formatTimeValue(
  value: string | null,
  timeOptions: Array<{ label: string; value: string; aliases?: string[] }>,
  strings: Pick<TodoStrings, 'timeValuePrefix' | 'timeNone'>
) {
  if (!value) {
    return `${strings.timeValuePrefix} · ${strings.timeNone}`;
  }
  const normalized = normalizeTimeValue(value);
  const preset = timeOptions.find((option) =>
    option.value === normalized || option.aliases?.includes(normalized));
  return `${strings.timeValuePrefix} · ${preset ? preset.label : normalized}`;
}
