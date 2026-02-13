import { useEffect, useState } from 'react';
import DateTimePicker from '@react-native-community/datetimepicker';
import {
  Platform,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useTodos } from '../features/todo/hooks/useTodos';
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

export function TodoListScreen({ token, onDone }: Props) {
  const [status, setStatus] = useState<'OPEN' | 'COMPLETED' | 'ALL'>('OPEN');
  const [view, setView] = useState<'ALL' | 'TODAY' | 'SCHEDULED' | 'DONE'>('ALL');
  const [text, setText] = useState('');
  const [pendingDate, setPendingDate] = useState<Date | null>(null);
  const [pendingTime, setPendingTime] = useState<string | null>(null);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [detailsTodoId, setDetailsTodoId] = useState<string | null>(null);
  const [detailText, setDetailText] = useState('');
  const [detailDate, setDetailDate] = useState<Date | null>(null);
  const [detailTime, setDetailTime] = useState<string | null>(null);
  const [showDetailDatePicker, setShowDetailDatePicker] = useState(false);
  const [showDetailTimePicker, setShowDetailTimePicker] = useState(false);
  const [savingDetails, setSavingDetails] = useState(false);
  const todos = useTodos(token, status);
  const strings = {
    title: 'Todos',
    subtitle: 'Keep the list moving.',
    filterTitle: 'Filter',
    all: 'All',
    today: 'Today',
    scheduled: 'Scheduled',
    done: 'Done',
    listTitle: 'List',
    noTodos: 'No todos yet.',
    complete: 'Complete',
    reopen: 'Reopen',
    addTodoTitle: 'Add todo',
    addPlaceholder: 'What needs to be done?',
    addAction: 'Add',
    adding: 'Adding...',
    back: 'Back',
    quickDateTitle: 'Quick date',
    quickToday: 'Today',
    quickTomorrow: 'Tomorrow',
    quickPick: 'Pick date',
    quickTimeTitle: 'Add time',
    timeMorning: 'Morning',
    timeAfternoon: 'Afternoon',
    timeEvening: 'Evening',
    timePick: 'Pick time',
    timeNone: 'None',
    pendingDatePrefix: 'Scheduled:',
    dateFiltersInfo: 'Filtered by date.',
    details: 'Details',
    editTitle: 'Edit todo',
    editSubtitle: 'Update details and scheduling.',
    saveChanges: 'Save changes',
    savingChanges: 'Saving...',
    close: 'Close',
    clearDate: 'Clear date',
    pickDateTitle: 'Pick a date',
    pickTimeTitle: 'Pick a time',
  };

  async function handleAdd() {
    if (!text.trim() || todos.loading) {
      return;
    }
    const dueDate = pendingDate ? toApiDate(pendingDate) : undefined;
    const dueTime = pendingDate && pendingTime ? pendingTime : undefined;
    const added = await todos.add(text.trim(), { dueDate, dueTime });
    if (added) {
      setText('');
      setPendingDate(null);
      setPendingTime(null);
    }
  }

  function setDateToToday() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    setPendingDate(today);
  }

  function setDateToTomorrow() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    setPendingDate(tomorrow);
  }

  function setDetailDateToToday() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    setDetailDate(today);
  }

  function setDetailDateToTomorrow() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    setDetailDate(tomorrow);
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
    const day = parsed.toLocaleDateString(undefined, { weekday: 'short' });
    const date = parsed.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    return dueTime ? `${day} · ${date} · ${dueTime}` : `${day} · ${date}`;
  }

  const timeOptions = [
    { label: strings.timeMorning, value: '08:00' },
    { label: strings.timeAfternoon, value: '13:00' },
    { label: strings.timeEvening, value: '18:00' },
  ];

  const selectedTodo = detailsTodoId
    ? todos.items.find((item) => item.id === detailsTodoId)
    : null;

  useEffect(() => {
    if (!selectedTodo) {
      return;
    }
    setDetailText(selectedTodo.text);
    setDetailDate(parseApiDate(selectedTodo.dueDate));
    setDetailTime(selectedTodo.dueTime ?? null);
  }, [selectedTodo]);

  async function handleSaveDetails() {
    if (!selectedTodo || savingDetails) {
      return;
    }
    if (!detailText.trim()) {
      return;
    }
    setSavingDetails(true);
    const dueDate = detailDate ? toApiDate(detailDate) : null;
    const dueTime = detailDate ? detailTime : null;
    const updated = await todos.update(selectedTodo.id, detailText.trim(), { dueDate, dueTime });
    setSavingDetails(false);
    if (updated) {
      setDetailsTodoId(null);
    }
  }

  function clearDetailDate() {
    setDetailDate(null);
    setDetailTime(null);
  }

  function setFilter(nextView: 'ALL' | 'TODAY' | 'SCHEDULED' | 'DONE') {
    setView(nextView);
    if (nextView === 'DONE') {
      setStatus('COMPLETED');
    } else if (nextView === 'ALL') {
      setStatus('ALL');
    } else {
      setStatus('OPEN');
    }
  }

  const visibleItems = todos.items.filter((item) => {
    if (view === 'TODAY') {
      const due = parseApiDate(item.dueDate);
      return due ? isToday(due) : false;
    }
    if (view === 'SCHEDULED') {
      const due = parseApiDate(item.dueDate);
      return !!due && !isToday(due);
    }
    return true;
  });

  return (
    <AppScreen
      refreshControl={
        <RefreshControl refreshing={todos.loading} onRefresh={todos.reload} />
      }
    >
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
      />

        <View style={styles.contentOffset}>
        <AppCard>
          <SectionTitle>{strings.filterTitle}</SectionTitle>
          <View style={styles.filters}>
            <AppChip label={strings.all} active={view === 'ALL'} onPress={() => setFilter('ALL')} />
            <AppChip label={strings.today} active={view === 'TODAY'} onPress={() => setFilter('TODAY')} />
            <AppChip label={strings.scheduled} active={view === 'SCHEDULED'} onPress={() => setFilter('SCHEDULED')} />
            <AppChip label={strings.done} active={view === 'DONE'} onPress={() => setFilter('DONE')} />
          </View>
        </AppCard>

        {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}

        <AppCard>
          <SectionTitle>{strings.listTitle}</SectionTitle>
          {view === 'TODAY' || view === 'SCHEDULED' ? (
            <Subtle>{strings.dateFiltersInfo}</Subtle>
          ) : null}
          {visibleItems.length === 0 && !todos.loading ? (
            <Subtle>{strings.noTodos}</Subtle>
          ) : null}
          <View style={styles.list}>
            {visibleItems.map((item) => {
              const dueLabel = formatDueLabel(item.dueDate, item.dueTime);
              return (
              <View key={item.id} style={styles.itemRow}>
                <Pressable
                  style={styles.checkboxPressable}
                  onPress={() => todos.complete(item.id)}
                >
                  <View style={[styles.checkbox, item.status === 'COMPLETED' ? styles.checkboxChecked : null]}>
                    {item.status === 'COMPLETED' ? (
                      <Text style={styles.checkboxMark}>✓</Text>
                    ) : null}
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
            })}
          </View>
        </AppCard>

        <AppCard>
          <SectionTitle>{strings.addTodoTitle}</SectionTitle>
          <AppInput
            value={text}
            placeholder={strings.addPlaceholder}
            onChangeText={setText}
          />
          <View style={styles.quickDateRow}>
            <Text style={styles.quickDateLabel}>{strings.quickDateTitle}</Text>
            <View style={styles.quickDateChips}>
              <AppChip
                label={strings.quickToday}
                active={!!pendingDate && isSameDay(pendingDate, new Date())}
                onPress={setDateToToday}
              />
              <AppChip
                label={strings.quickTomorrow}
                active={(() => {
                  if (!pendingDate) {
                    return false;
                  }
                  const tomorrow = new Date();
                  tomorrow.setDate(tomorrow.getDate() + 1);
                  return isSameDay(pendingDate, tomorrow);
                })()}
                onPress={setDateToTomorrow}
              />
              <AppChip
                label={strings.quickPick}
                active={showDatePicker}
                onPress={() => setShowDatePicker(true)}
              />
            </View>
            {pendingDate ? (
              <Subtle>
                {strings.pendingDatePrefix} {formatDate(pendingDate)} {pendingTime ? `· ${pendingTime}` : ''}
              </Subtle>
            ) : null}
          </View>
          {pendingDate ? (
            <View style={styles.quickTimeRow}>
              <Text style={styles.quickDateLabel}>{strings.quickTimeTitle}</Text>
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
          <AppButton
            title={todos.loading ? strings.adding : strings.addAction}
            onPress={handleAdd}
            fullWidth
            disabled={todos.loading}
          />
        </AppCard>
        </View>
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
                date.setHours(0, 0, 0, 0);
                setPendingDate(date);
              }}
            />
          ) : (
          <Pressable style={styles.backdrop} onPress={() => setShowDatePicker(false)}>
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
                value.setHours(0, 0, 0, 0);
                setDetailDate(value);
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
          <Pressable style={styles.backdrop} onPress={() => setDetailsTodoId(null)}>
            <Pressable style={styles.sheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <Text style={textStyles.h3}>{strings.editTitle}</Text>
              <Subtle>{strings.editSubtitle}</Subtle>
              <AppInput
                value={detailText}
                placeholder={strings.addPlaceholder}
                onChangeText={setDetailText}
              />
              <View style={styles.quickDateRow}>
                <Text style={styles.quickDateLabel}>{strings.quickDateTitle}</Text>
                <View style={styles.quickDateChips}>
                  <AppChip
                    label={strings.quickToday}
                    active={!!detailDate && isSameDay(detailDate, new Date())}
                    onPress={setDetailDateToToday}
                  />
                  <AppChip
                    label={strings.quickTomorrow}
                    active={(() => {
                      if (!detailDate) {
                        return false;
                      }
                      const tomorrow = new Date();
                      tomorrow.setDate(tomorrow.getDate() + 1);
                      return isSameDay(detailDate, tomorrow);
                    })()}
                    onPress={setDetailDateToTomorrow}
                  />
                  <AppChip
                    label={strings.quickPick}
                    active={showDetailDatePicker}
                    onPress={() => setShowDetailDatePicker(true)}
                  />
                  <AppChip
                    label={strings.clearDate}
                    active={!detailDate}
                    onPress={clearDetailDate}
                  />
                </View>
                {detailDate ? (
                  <Subtle>
                    {strings.pendingDatePrefix} {formatDate(detailDate)} {detailTime ? `· ${detailTime}` : ''}
                  </Subtle>
                ) : null}
              </View>
              {detailDate ? (
                <View style={styles.quickTimeRow}>
                  <Text style={styles.quickDateLabel}>{strings.quickTimeTitle}</Text>
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
              <AppButton
                title={savingDetails ? strings.savingChanges : strings.saveChanges}
                onPress={handleSaveDetails}
                fullWidth
                disabled={savingDetails || !detailText.trim()}
              />
              <AppButton title={strings.close} onPress={() => setDetailsTodoId(null)} variant="ghost" fullWidth />
            </Pressable>
          </Pressable>
        ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  filters: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
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
  quickTimeRow: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.xs,
  },
  list: {
    marginTop: theme.spacing.sm,
    gap: theme.spacing.sm,
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
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    padding: theme.spacing.lg,
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
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
});
