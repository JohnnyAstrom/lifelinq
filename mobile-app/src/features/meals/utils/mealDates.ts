export type MonthGridCell = {
  date: Date;
  isCurrentMonth: boolean;
};

export function addDays(date: Date, days: number) {
  const next = new Date(date.getTime());
  next.setDate(next.getDate() + days);
  return next;
}

export function getIsoWeekParts(date: Date): { year: number; isoWeek: number } {
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

export function getWeekStartDate(year: number, isoWeek: number): Date {
  const date = new Date(Date.UTC(year, 0, 4));
  const day = date.getUTCDay() || 7;
  date.setUTCDate(date.getUTCDate() + 1 - day);
  date.setUTCDate(date.getUTCDate() + (isoWeek - 1) * 7);
  return date;
}

export function toDateKey(date: Date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function isTodayDate(date: Date) {
  const now = new Date();
  return (
    date.getFullYear() === now.getFullYear()
    && date.getMonth() === now.getMonth()
    && date.getDate() === now.getDate()
  );
}

export function buildMonthGridCells(anchorDate: Date): MonthGridCell[] {
  const startOfMonth = new Date(anchorDate.getFullYear(), anchorDate.getMonth(), 1);
  const startWeekday = (startOfMonth.getDay() + 6) % 7; // Mon=0
  const gridStart = new Date(startOfMonth.getTime());
  gridStart.setDate(startOfMonth.getDate() - startWeekday);
  return Array.from({ length: 42 }).map((_, index) => {
    const date = new Date(gridStart.getTime());
    date.setDate(gridStart.getDate() + index);
    return {
      date,
      isCurrentMonth: date.getMonth() === anchorDate.getMonth() && date.getFullYear() === anchorDate.getFullYear(),
    };
  });
}
