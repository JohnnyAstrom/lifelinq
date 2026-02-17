import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useMemo, useState } from 'react';
import { useAuth } from '../shared/auth/AuthContext';
import { useDocuments } from '../features/documents/hooks/useDocuments';
import { AppButton, AppCard, AppInput, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  onDone: () => void;
};

export function DocumentsScreen({ onDone }: Props) {
  const { token } = useAuth();
  const documents = useDocuments(token);
  const [showCreate, setShowCreate] = useState(false);
  const [title, setTitle] = useState('');
  const [notes, setNotes] = useState('');
  const [date, setDate] = useState('');
  const [category, setCategory] = useState('');
  const [tags, setTags] = useState('');
  const [externalLink, setExternalLink] = useState('');
  const strings = {
    title: 'Documents',
    subtitle: 'Receipts, warranties, and important records.',
    searchLabel: 'Search documents',
    searchPlaceholder: 'Search by title or notes',
    clearSearch: 'Clear',
    createTitle: 'Create document',
    createSubtitle: 'Metadata only in V0. Enter what you have.',
    titleLabel: 'Title',
    titlePlaceholder: 'Receipt, warranty, contract...',
    notesLabel: 'Notes',
    notesPlaceholder: 'Optional notes',
    dateLabel: 'Date',
    datePlaceholder: 'YYYY-MM-DD',
    categoryLabel: 'Category',
    categoryPlaceholder: 'Optional category',
    tagsLabel: 'Tags (comma separated)',
    tagsPlaceholder: 'house, car, insurance',
    linkLabel: 'External link',
    linkPlaceholder: 'https://',
    save: 'Save',
    cancel: 'Cancel',
    loading: 'Loading documents...',
    emptyTitle: 'No documents yet.',
    emptyBody: 'Create a document in the backend to see it here.',
    results: 'results',
    untitled: 'Untitled document',
    back: 'Back',
    create: 'New document',
  };

  const hasQuery = documents.query.trim().length > 0;
  const canSave = title.trim().length > 0;
  const parsedTags = useMemo(() => {
    return tags
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean);
  }, [tags]);

  async function handleCreate() {
    if (!canSave) {
      return;
    }
    const created = await documents.create({
      title: title.trim(),
      notes: notes.trim() || null,
      date: date.trim() || null,
      category: category.trim() || null,
      tags: parsedTags.length ? parsedTags : null,
      externalLink: externalLink.trim() || null,
    });
    if (created) {
      setShowCreate(false);
      setTitle('');
      setNotes('');
      setDate('');
      setCategory('');
      setTags('');
      setExternalLink('');
    }
  }

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
      />

      <View style={styles.contentOffset}>
        <AppCard>
          <SectionTitle>{strings.searchLabel}</SectionTitle>
          <AppInput
            value={documents.query}
            placeholder={strings.searchPlaceholder}
            onChangeText={documents.setQuery}
          />
          <View style={styles.searchMeta}>
            <Subtle>
              {documents.items.length} {strings.results}
            </Subtle>
            {hasQuery ? (
              <AppButton title={strings.clearSearch} onPress={() => documents.setQuery('')} variant="ghost" />
            ) : null}
          </View>
        </AppCard>

        <AppCard>
          <View style={styles.createHeader}>
            <View style={styles.createHeaderText}>
              <Text style={textStyles.h3}>{strings.createTitle}</Text>
              <Subtle>{strings.createSubtitle}</Subtle>
            </View>
            <AppButton title={strings.create} onPress={() => setShowCreate(true)} />
          </View>
        </AppCard>

        {documents.loading ? <Subtle>{strings.loading}</Subtle> : null}
        {documents.error ? <Text style={styles.error}>{documents.error}</Text> : null}

        {documents.items.length === 0 && !documents.loading ? (
          <AppCard>
            <Text style={textStyles.h3}>{strings.emptyTitle}</Text>
            <Subtle>{strings.emptyBody}</Subtle>
          </AppCard>
        ) : (
          <View style={styles.list}>
            {documents.items.map((item) => {
              const metaParts: string[] = [];
              if (item.date) {
                metaParts.push(item.date);
              }
              if (item.category) {
                metaParts.push(item.category);
              }
              if (item.tags?.length) {
                metaParts.push(item.tags.join(', '));
              }
              const meta = metaParts.join(' · ');
              return (
                <Pressable key={item.id} style={({ pressed }) => [styles.row, pressed ? styles.rowPressed : null]}>
                  <View style={styles.rowBody}>
                    <Text style={styles.rowTitle}>{item.title?.trim() || strings.untitled}</Text>
                    {item.notes ? <Subtle>{item.notes}</Subtle> : null}
                    {meta ? <Subtle>{meta}</Subtle> : null}
                    {item.externalLink ? <Subtle>{item.externalLink}</Subtle> : null}
                  </View>
                </Pressable>
              );
            })}
          </View>
        )}
      </View>

      {showCreate ? (
        <Pressable style={styles.backdrop} onPress={() => setShowCreate(false)}>
          <Pressable style={styles.sheet} onPress={() => null}>
            <View style={styles.sheetHandle} />
            <Text style={textStyles.h3}>{strings.createTitle}</Text>
            <Subtle>{strings.createSubtitle}</Subtle>
            <View style={styles.formField}>
              <Text style={styles.label}>{strings.titleLabel}</Text>
              <AppInput value={title} placeholder={strings.titlePlaceholder} onChangeText={setTitle} autoFocus />
            </View>
            <View style={styles.formField}>
              <Text style={styles.label}>{strings.notesLabel}</Text>
              <AppInput value={notes} placeholder={strings.notesPlaceholder} onChangeText={setNotes} multiline />
            </View>
            <View style={styles.formField}>
              <Text style={styles.label}>{strings.dateLabel}</Text>
              <AppInput value={date} placeholder={strings.datePlaceholder} onChangeText={setDate} />
            </View>
            <View style={styles.formField}>
              <Text style={styles.label}>{strings.categoryLabel}</Text>
              <AppInput value={category} placeholder={strings.categoryPlaceholder} onChangeText={setCategory} />
            </View>
            <View style={styles.formField}>
              <Text style={styles.label}>{strings.tagsLabel}</Text>
              <AppInput value={tags} placeholder={strings.tagsPlaceholder} onChangeText={setTags} />
            </View>
            <View style={styles.formField}>
              <Text style={styles.label}>{strings.linkLabel}</Text>
              <AppInput value={externalLink} placeholder={strings.linkPlaceholder} onChangeText={setExternalLink} />
            </View>
            <View style={styles.sheetActions}>
              <AppButton title={strings.save} onPress={handleCreate} fullWidth disabled={!canSave} />
              <AppButton title={strings.cancel} onPress={() => setShowCreate(false)} variant="ghost" fullWidth />
            </View>
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
  searchMeta: {
    marginTop: theme.spacing.sm,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  createHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.md,
  },
  createHeaderText: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  list: {
    gap: theme.spacing.sm,
  },
  row: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
  },
  rowPressed: {
    opacity: 0.9,
    transform: [{ scale: 0.99 }],
  },
  rowBody: {
    gap: theme.spacing.xs,
  },
  rowTitle: {
    ...textStyles.h3,
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
  sheetActions: {
    gap: theme.spacing.sm,
  },
  formField: {
    gap: theme.spacing.xs,
  },
  label: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
});
