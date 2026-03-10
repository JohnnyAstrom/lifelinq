import { Alert, Keyboard, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useMemo, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useDocuments } from '../hooks/useDocuments';
import { ApiError } from '../../../shared/api/client';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppCard, AppInput, AppRow, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

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
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);
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
    emptyBody: 'Create a document to see it here.',
    results: 'results',
    untitled: 'Untitled document',
    back: 'Back',
    create: 'New document',
    remove: 'Remove',
    confirmRemoveTitle: 'Remove document?',
    confirmRemoveBody: 'This action cannot be undone.',
    removeConfirm: 'Remove',
    neutralMissing: 'Document no longer exists. The list was refreshed.',
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
      closeCreate();
      setTitle('');
      setNotes('');
      setDate('');
      setCategory('');
      setTags('');
      setExternalLink('');
    }
  }

  function requestRemove(id: string) {
    Alert.alert(
      strings.confirmRemoveTitle,
      strings.confirmRemoveBody,
      [
        { text: strings.cancel, style: 'cancel' },
        {
          text: strings.removeConfirm,
          style: 'destructive',
          onPress: () => {
            void handleRemove(id);
          },
        },
      ]
    );
  }

  async function handleRemove(id: string) {
    setRemovingId(id);
    setInfoMessage(null);
    try {
      await documents.remove(id);
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        setInfoMessage(strings.neutralMissing);
      }
    } finally {
      setRemovingId(null);
    }
  }

  function closeCreate() {
    setShowCreate(false);
    Keyboard.dismiss();
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: showCreate,
    onCloseOverlay: closeCreate,
  });

  return (
    <AppScreen
      scroll={false}
      contentStyle={styles.screenContent}
      header={(
        <TopBar
          title={strings.title}
          subtitle={strings.subtitle}
          icon={<Ionicons name="document-text-outline" />}
          accentKey="documents"
          right={<BackIconButton onPress={onDone} />}
        />
      )}
    >

      <View style={styles.mainLayout}>
        <ScrollView
          style={styles.mainScroll}
          contentContainerStyle={styles.contentOffset}
          keyboardShouldPersistTaps="handled"
        >
          <AppCard style={styles.searchCard}>
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
                <Text style={textStyles.h2}>{strings.createTitle}</Text>
                <Subtle>{strings.createSubtitle}</Subtle>
              </View>
              <AppButton title={strings.create} onPress={() => setShowCreate(true)} accentKey="documents" />
            </View>
          </AppCard>

          {documents.loading ? <Subtle>{strings.loading}</Subtle> : null}
          {infoMessage ? <Subtle>{infoMessage}</Subtle> : null}
          {documents.error ? <Text style={styles.error}>{documents.error}</Text> : null}

          {documents.items.length === 0 && !documents.loading ? (
            <AppCard>
              <Text style={textStyles.h2}>{strings.emptyTitle}</Text>
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
                    <AppRow
                      title={item.title?.trim() || strings.untitled}
                      titleStyle={styles.rowTitle}
                      contentStyle={styles.rowBody}
                      subtitle={(
                        <>
                          {item.notes ? <Subtle>{item.notes}</Subtle> : null}
                          {meta ? <Subtle>{meta}</Subtle> : null}
                          {item.externalLink ? <Subtle>{item.externalLink}</Subtle> : null}
                        </>
                      )}
                      trailing={(
                        <AppButton
                          title={strings.remove}
                          onPress={() => requestRemove(item.id)}
                          variant="ghost"
                          disabled={removingId === item.id}
                        />
                      )}
                    />
                  </Pressable>
                );
              })}
            </View>
          )}
        </ScrollView>
      </View>

      {showCreate ? (
        <OverlaySheet onClose={closeCreate} sheetStyle={styles.sheet}>
          <View style={styles.sheetLayout}>
            <ScrollView
              style={styles.sheetScroll}
              contentContainerStyle={styles.sheetScrollContent}
              keyboardShouldPersistTaps="handled"
              showsVerticalScrollIndicator={false}
            >
              <Text style={textStyles.h2}>{strings.createTitle}</Text>
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
            </ScrollView>
            <View style={styles.sheetActions}>
                <AppButton
                  title={strings.save}
                  onPress={handleCreate}
                  fullWidth
                  disabled={!canSave || documents.loading}
                  accentKey="documents"
                />
              <AppButton title={strings.cancel} onPress={closeCreate} variant="ghost" fullWidth />
            </View>
          </View>
        </OverlaySheet>
      ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  screenContent: {
    flex: 1,
  },
  mainLayout: {
    flex: 1,
  },
  mainScroll: {
    flex: 1,
  },
  contentOffset: {
    paddingTop: theme.layout.topBarOffset + theme.spacing.md,
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.md,
  },
  searchCard: {
    gap: theme.spacing.sm,
  },
  searchMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  createHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  createHeaderText: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  list: {
    gap: theme.spacing.xs,
  },
  row: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.sm,
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
    ...textStyles.body,
    flex: 1,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    maxHeight: theme.layout.sheetMaxHeight.tall,
    alignSelf: 'center',
    width: '100%',
    padding: theme.layout.sheetPadding,
    borderWidth: 1,
    borderColor: theme.colors.border,
    overflow: 'hidden',
  },
  sheetLayout: {
    maxHeight: '100%',
    flexShrink: 1,
  },
  sheetScroll: {
    flex: 1,
    minHeight: 0,
  },
  sheetScrollContent: {
    flexGrow: 1,
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
    gap: theme.spacing.xs,
  },
  sheetActions: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
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

