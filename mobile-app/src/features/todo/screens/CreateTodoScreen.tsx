import { useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { useTodos } from '../features/todo/hooks/useTodos';
import {
  AppButton,
  AppCard,
  AppChip,
  AppInput,
  AppScreen,
  SectionTitle,
  Subtle,
} from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

export function CreateTodoScreen({ token, onDone }: Props) {
  const [text, setText] = useState('');
  const [pendingDate, setPendingDate] = useState<Date | null>(null);
  const [pendingTime, setPendingTime] = useState<string | null>(null);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const todos = useTodos(token, 'OPEN');
  const canCreateTodo = text.trim().length > 0;
  const strings = {
    title: 'New todo',
    subtitle: 'Add something you want to remember today.',
    placeholder: 'Buy milk',
    save: 'Save',
    saving: 'Saving...',
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
    timeNone: 'Any',
    pendingDatePrefix: 'Scheduled:',
    pickDateTitle: 'Pick a date',
    pickTimeTitle: 'Pick a time',
  };

  async function handleCreate() {
    if (!text.trim()) {
      return;
    }
    if (todos.loading) {
      return;
    }
    const dueDate = pendingDate ? toApiDate(pendingDate) : undefined;
    const dueTime = pendingDate && pendingTime ? pendingTime : undefined;
    const created = await todos.add(text.trim(), {
      scope: dueDate ? 'DAY' : 'LATER',
      dueDate,
      dueTime,
    });
    if (created) {
      setText('');
      setPendingDate(null);
      setPendingTime(null);
      onDone();
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

  function isSameDay(left: Date, right: Date) {
    return left.getFullYear() === right.getFullYear()
      && left.getMonth() === right.getMonth()
      && left.getDate() === right.getDate();
  }

  function formatPendingDate() {
    if (!pendingDate) {
      return null;
    }
    const day = pendingDate.toLocaleDateString(undefined, { weekday: 'short' });
    const date = pendingDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    return `${day} · ${date}`;
  }

  function toApiDate(date: Date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  const timeOptions = [
    { label: strings.timeMorning, value: '08:00' },
    { label: strings.timeAfternoon, value: '13:00' },
    { label: strings.timeEvening, value: '18:00' },
  ];

  return (
    <AppScreen scroll={false} contentStyle={styles.container}>
      <AppCard style={styles.card}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
        <AppInput value={text} onChangeText={setText} placeholder={strings.placeholder} />
        <View style={styles.quickDateRow}>
          <SectionTitle>{strings.quickDateTitle}</SectionTitle>
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
              {strings.pendingDatePrefix} {formatPendingDate()} {pendingTime ? `· ${pendingTime}` : ''}
            </Subtle>
          ) : null}
        </View>
        {pendingDate ? (
          <View style={styles.quickTimeRow}>
            <SectionTitle>{strings.quickTimeTitle}</SectionTitle>
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
        {todos.error ? <Text style={styles.error}>{todos.error}</Text> : null}
        {canCreateTodo || todos.loading ? (
          <AppButton
            title={todos.loading ? strings.saving : strings.save}
            onPress={handleCreate}
            fullWidth
            disabled={todos.loading}
          />
        ) : null}
        <AppButton title={strings.back} onPress={onDone} variant="ghost" fullWidth />
      </AppCard>

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
                    <Text style={styles.pickerText}>{label}</Text>
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
                  <Text style={styles.pickerText}>{time}</Text>
                </Pressable>
              ))}
            </View>
          </Pressable>
        </Pressable>
        )
      ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  card: {
    gap: theme.spacing.sm,
  },
  quickDateRow: {
    gap: theme.spacing.xs,
  },
  quickDateChips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  quickTimeRow: {
    gap: theme.spacing.xs,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
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
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
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
  pickerText: {
    ...textStyles.body,
  },
});
