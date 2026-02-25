export function getStartOfWeekMonday(date: Date) {
  const next = new Date(date.getTime());
  next.setHours(0, 0, 0, 0);
  const day = next.getDay(); // 0 Sun ... 6 Sat
  const diff = day === 0 ? -6 : 1 - day;
  next.setDate(next.getDate() + diff);
  return next;
}

export function toDateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function getIsoWeekInfoFromDate(date: Date) {
  const target = new Date(date.getTime());
  target.setHours(0, 0, 0, 0);
  const day = target.getDay() || 7;
  target.setDate(target.getDate() + 4 - day);
  const yearStart = new Date(target.getFullYear(), 0, 1);
  const diffMs = target.getTime() - yearStart.getTime();
  const week = Math.ceil(((diffMs / 86400000) + 1) / 7);
  return { week, year: target.getFullYear() };
}

export function getMondayOfIsoWeek(year: number, week: number) {
  const jan4 = new Date(year, 0, 4);
  jan4.setHours(0, 0, 0, 0);
  const mondayOfWeek1 = getStartOfWeekMonday(jan4);
  const monday = new Date(mondayOfWeek1.getTime());
  monday.setDate(monday.getDate() + (week - 1) * 7);
  monday.setHours(0, 0, 0, 0);
  return monday;
}

export function toApiDate(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function parseApiDate(value?: string | null) {
  if (!value) {
    return null;
  }
  const [year, month, day] = value.split('-').map(Number);
  if (!year || !month || !day) {
    return null;
  }
  return new Date(year, month - 1, day);
}

export function isSameDay(left: Date, right: Date) {
  return left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate();
}

export function getTomorrowDate() {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  tomorrow.setHours(0, 0, 0, 0);
  return tomorrow;
}

export function isToday(date: Date) {
  return isSameDay(date, new Date());
}

