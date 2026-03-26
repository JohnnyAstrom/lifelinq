import { useEffect, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type ShoppingListOption = {
  id: string;
  name: string;
};

type WeekShoppingReviewLine = {
  lineId: string;
  name: string;
  amount: string | null;
  metadataLabels: string[];
  contributorOccurrenceCount: number;
  contributorOccurrenceLabels: string[];
  contributorOccurrenceGroups: {
    title: string;
    occurrenceLabels: string[];
  }[];
  otherListNames: string[];
  hasExpandableDetails: boolean;
};

type Strings = {
  title: string;
  subtitle?: string;
  selectedListLabel: string;
  loadingListsHint: string;
  noShoppingLists: string;
  newListLabel: string;
  newListPlaceholder: string;
  createListAction: string;
  creatingListAction: string;
  alreadyOnListLabel: string;
  addToListLabel: string;
  emptyWeekHint: string;
  everythingAlreadyOnListHint: string;
  loadingReviewHint: string;
  loadingReviewFailedHint: string;
  selectionHint: string;
  nothingToAddHint: string;
  confirm: string;
  confirming: string;
  close: string;
};

type Props = {
  representedLines: WeekShoppingReviewLine[];
  addableLines: WeekShoppingReviewLine[];
  selectedLineIds: string[];
  lists: ShoppingListOption[];
  effectiveListId: string | null;
  isLoadingLists: boolean;
  hasReview: boolean;
  isLoadingReview: boolean;
  reviewError: string | null;
  showCreateListForm: boolean;
  newListName: string;
  isCreatingList: boolean;
  isSubmitting: boolean;
  onSelectListId: (id: string) => void;
  onToggleLine: (lineId: string) => void;
  onOpenCreateList: () => void;
  onCloseCreateList: () => void;
  onChangeNewListName: (value: string) => void;
  onCreateList: () => void;
  onConfirm: () => void;
  onClose: () => void;
  strings: Strings;
};

export function WeekShoppingReviewSheet({
  representedLines,
  addableLines,
  selectedLineIds,
  lists,
  effectiveListId,
  isLoadingLists,
  hasReview,
  isLoadingReview,
  reviewError,
  showCreateListForm,
  newListName,
  isCreatingList,
  isSubmitting,
  onSelectListId,
  onToggleLine,
  onOpenCreateList,
  onCloseCreateList,
  onChangeNewListName,
  onCreateList,
  onConfirm,
  onClose,
  strings,
}: Props) {
  const selectedIds = new Set(selectedLineIds);
  const selectedLineCount = addableLines.filter((line) => selectedIds.has(line.lineId)).length;
  const [expandedLineId, setExpandedLineId] = useState<string | null>(null);
  const showEmptyWeek = !isLoadingReview
    && !reviewError
    && hasReview
    && addableLines.length === 0
    && representedLines.length === 0;
  const showPendingReview = !isLoadingReview
    && !reviewError
    && !hasReview;
  const showEverythingAlreadyHint = !isLoadingReview
    && !reviewError
    && hasReview
    && addableLines.length === 0
    && representedLines.length > 0;
  const showNoSelectionHint = !isLoadingReview
    && !reviewError
    && hasReview
    && addableLines.length > 0
    && selectedLineCount === 0;
  const canCreateList = newListName.trim().length > 0;
  const visibleLineIds = new Set([...representedLines, ...addableLines].map((line) => line.lineId));

  useEffect(() => {
    if (expandedLineId && !visibleLineIds.has(expandedLineId)) {
      setExpandedLineId(null);
    }
  }, [expandedLineId, representedLines, addableLines]);

  function toggleExpandedLine(lineId: string, expandable: boolean) {
    if (!expandable) {
      return;
    }
    setExpandedLineId((current) => (current === lineId ? null : lineId));
  }

  function renderExpandedDetails(line: WeekShoppingReviewLine) {
    if (expandedLineId !== line.lineId || !line.hasExpandableDetails) {
      return null;
    }

    const maxVisibleContributorOccurrences = 4;
    let remainingContributorSlots = maxVisibleContributorOccurrences;
    const visibleContributorGroups = line.contributorOccurrenceGroups
      .map((group) => {
        if (remainingContributorSlots <= 0) {
          return null;
        }
        const visibleOccurrences = group.occurrenceLabels.slice(0, remainingContributorSlots);
        remainingContributorSlots -= visibleOccurrences.length;
        if (visibleOccurrences.length === 0) {
          return null;
        }
        return {
          title: group.title,
          occurrenceLabels: visibleOccurrences,
        };
      })
      .filter((group): group is { title: string; occurrenceLabels: string[] } => group != null);
    const hiddenContributorOccurrenceCount = Math.max(
      0,
      line.contributorOccurrenceCount - visibleContributorGroups.reduce(
        (count, group) => count + group.occurrenceLabels.length,
        0
      )
    );

    return (
      <View style={styles.expandedDetails}>
        {line.otherListNames.length > 1 ? (
          <Text style={styles.expandedDetailText}>
            {`Also on: ${line.otherListNames.join(', ')}`}
          </Text>
        ) : null}
        {(line.contributorOccurrenceCount > 1
          || (line.contributorOccurrenceCount === 1 && line.metadataLabels.includes('1 meal'))) ? (
            <View style={styles.expandedContributorList}>
              {visibleContributorGroups.length > 0
                ? visibleContributorGroups.map((group) => (
                    <View key={`${line.lineId}-${group.title}`} style={styles.expandedContributorGroup}>
                      <Text style={styles.expandedContributorTitle}>{group.title}</Text>
                      <View style={styles.expandedContributorOccurrences}>
                        {group.occurrenceLabels.map((label) => (
                          <Text key={`${line.lineId}-${group.title}-${label}`} style={styles.expandedOccurrenceText}>
                            {label}
                          </Text>
                        ))}
                      </View>
                    </View>
                  ))
                : line.contributorOccurrenceLabels.map((label) => (
                    <Text key={`${line.lineId}-${label}`} style={styles.expandedOccurrenceText}>
                      {label}
                    </Text>
                  ))}
              {hiddenContributorOccurrenceCount > 0 ? (
                <Text style={styles.expandedOverflowText}>{`+ ${hiddenContributorOccurrenceCount} more`}</Text>
              ) : null}
            </View>
          ) : null}
      </View>
    );
  }

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          {strings.subtitle ? <Subtle>{strings.subtitle}</Subtle> : null}
        </View>
        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.section}>
              <Text style={styles.sectionLabel}>{strings.selectedListLabel}</Text>
              {lists.length > 0 ? (
                <View style={styles.chipRow}>
                  {lists.map((list) => (
                    <AppChip
                      key={list.id}
                      label={list.name}
                      active={list.id === effectiveListId}
                      accentKey="meals"
                      onPress={() => onSelectListId(list.id)}
                    />
                  ))}
                  {!showCreateListForm ? (
                    <AppChip
                      label={strings.newListLabel}
                      active={false}
                      accentKey="meals"
                      onPress={onOpenCreateList}
                    />
                  ) : null}
                </View>
              ) : (
                <Subtle>{isLoadingLists ? strings.loadingListsHint : strings.noShoppingLists}</Subtle>
              )}

              {showCreateListForm ? (
                <View style={styles.createListCard}>
                  <AppInput
                    placeholder={strings.newListPlaceholder}
                    value={newListName}
                    onChangeText={onChangeNewListName}
                    returnKeyType="done"
                    onSubmitEditing={onCreateList}
                    autoFocus={lists.length === 0}
                  />
                  <View style={styles.createListActions}>
                    <AppButton
                      title={isCreatingList ? strings.creatingListAction : strings.createListAction}
                      onPress={onCreateList}
                      accentKey="meals"
                      disabled={!canCreateList || isCreatingList}
                    />
                    {lists.length > 0 ? (
                      <AppButton
                        title={strings.close}
                        onPress={onCloseCreateList}
                        variant="ghost"
                        disabled={isCreatingList}
                      />
                    ) : null}
                  </View>
                </View>
              ) : null}
            </View>

            <View style={styles.section}>
              {representedLines.length > 0 ? (
                <View style={styles.section}>
                  <Text style={styles.sectionLabel}>{strings.alreadyOnListLabel}</Text>
                  <View style={styles.lineList}>
                    {representedLines.map((line) => (
                      <Pressable
                        key={line.lineId}
                        onPress={() => toggleExpandedLine(line.lineId, line.hasExpandableDetails)}
                        style={({ pressed }) => [
                          styles.lineRow,
                          styles.representedLineRow,
                          line.hasExpandableDetails ? styles.lineRowExpandable : null,
                          pressed && line.hasExpandableDetails ? styles.lineRowPressed : null,
                        ]}
                      >
                        <View style={[styles.checkbox, styles.checkboxSelected]}>
                          <Ionicons name="checkmark" size={14} color="#fff" />
                        </View>
                        <View style={styles.lineCopy}>
                          <View style={styles.linePrimary}>
                            <Text style={styles.lineName}>{line.name}</Text>
                            <View style={styles.linePrimaryMeta}>
                              {line.amount ? <Text style={styles.lineAmount}>{line.amount}</Text> : null}
                              {line.hasExpandableDetails ? (
                                <Ionicons
                                  name={expandedLineId === line.lineId ? 'chevron-up' : 'chevron-down'}
                                  size={16}
                                  color={theme.colors.textSecondary}
                                />
                              ) : null}
                            </View>
                          </View>
                          {line.metadataLabels.length > 0 ? (
                            <View style={styles.metadataRow}>
                              {line.metadataLabels.map((label) => (
                                <View key={`${line.lineId}-${label}`} style={styles.metadataChip}>
                                  <Text style={styles.metadataText} numberOfLines={1} ellipsizeMode="tail">
                                    {label}
                                  </Text>
                                </View>
                              ))}
                            </View>
                          ) : null}
                          {renderExpandedDetails(line)}
                        </View>
                      </Pressable>
                    ))}
                  </View>
                </View>
              ) : null}

              {!showEverythingAlreadyHint ? (
                <Text style={styles.sectionLabel}>{strings.addToListLabel}</Text>
              ) : null}
              {isLoadingReview ? (
                <Subtle>{strings.loadingReviewHint}</Subtle>
              ) : reviewError ? (
                <Text style={styles.error}>{strings.loadingReviewFailedHint}</Text>
              ) : showPendingReview ? (
                <Subtle>{strings.loadingReviewHint}</Subtle>
              ) : showEmptyWeek ? (
                <Subtle>{strings.emptyWeekHint}</Subtle>
              ) : showEverythingAlreadyHint ? (
                <Subtle>{strings.everythingAlreadyOnListHint}</Subtle>
              ) : (
                <View style={styles.lineList}>
                  {addableLines.map((line) => (
                    <Pressable
                      key={line.lineId}
                      onPress={() => {
                        if (line.hasExpandableDetails) {
                          toggleExpandedLine(line.lineId, true);
                          return;
                        }
                        onToggleLine(line.lineId);
                      }}
                      style={({ pressed }) => [
                        styles.lineRow,
                        !selectedIds.has(line.lineId) ? styles.lineRowUnselected : null,
                        pressed ? styles.lineRowPressed : null,
                      ]}
                    >
                      <Pressable
                        onPress={(event) => {
                          event.stopPropagation();
                          onToggleLine(line.lineId);
                        }}
                        style={[
                          styles.checkbox,
                          selectedIds.has(line.lineId) ? styles.checkboxSelected : null,
                        ]}
                      >
                        {selectedIds.has(line.lineId) ? (
                          <Ionicons name="checkmark" size={14} color="#fff" />
                        ) : null}
                      </Pressable>
                      <View style={styles.lineCopy}>
                        <View style={styles.linePrimary}>
                          <Text style={styles.lineName}>{line.name}</Text>
                          <View style={styles.linePrimaryMeta}>
                            {line.amount ? <Text style={styles.lineAmount}>{line.amount}</Text> : null}
                            {line.hasExpandableDetails ? (
                              <Ionicons
                                name={expandedLineId === line.lineId ? 'chevron-up' : 'chevron-down'}
                                size={16}
                                color={theme.colors.textSecondary}
                              />
                            ) : null}
                          </View>
                        </View>
                        {line.metadataLabels.length > 0 ? (
                          <View style={styles.metadataRow}>
                            {line.metadataLabels.map((label) => (
                              <View key={`${line.lineId}-${label}`} style={styles.metadataChip}>
                                <Text style={styles.metadataText} numberOfLines={1} ellipsizeMode="tail">
                                  {label}
                                </Text>
                              </View>
                            ))}
                          </View>
                        ) : null}
                        {renderExpandedDetails(line)}
                      </View>
                    </Pressable>
                  ))}
                </View>
              )}
            </View>

            <View style={styles.footer}>
              {showNoSelectionHint ? (
                <Subtle>{strings.selectionHint}</Subtle>
              ) : null}
              <AppButton
                title={isSubmitting ? strings.confirming : strings.confirm}
                onPress={onConfirm}
                fullWidth
                accentKey="meals"
                disabled={!effectiveListId || selectedLineCount === 0 || isSubmitting || isLoadingReview || !!reviewError || !hasReview}
              />
              <AppButton
                title={strings.close}
                onPress={onClose}
                variant="secondary"
                fullWidth
                disabled={isSubmitting}
              />
            </View>
          </ScrollView>
        </View>
      </View>
    </OverlaySheet>
  );
}

const styles = StyleSheet.create({
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    paddingTop: theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.layout.sheetPadding,
    borderWidth: 1,
    borderColor: theme.colors.border,
    overflow: 'hidden',
  },
  layout: {
    maxHeight: '100%',
    flexShrink: 1,
    minHeight: 0,
  },
  header: {
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  body: {
    flexShrink: 1,
    minHeight: 0,
  },
  scroll: {
    minHeight: 0,
    maxHeight: '100%',
    marginTop: theme.spacing.sm,
  },
  scrollContent: {
    gap: theme.spacing.md,
    minWidth: 0,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
  },
  section: {
    gap: theme.spacing.xs,
  },
  sectionLabel: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  createListCard: {
    gap: theme.spacing.sm,
    paddingTop: theme.spacing.xs,
  },
  createListActions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  lineList: {
    gap: theme.spacing.xs,
  },
  lineRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    backgroundColor: theme.colors.surfaceAlt,
  },
  lineRowPressed: {
    opacity: 0.82,
  },
  lineRowUnselected: {
    backgroundColor: theme.colors.surface,
  },
  representedLineRow: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: theme.colors.border,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.surface,
    marginTop: 1,
  },
  checkboxSelected: {
    backgroundColor: theme.colors.feature.meals,
    borderColor: theme.colors.feature.meals,
  },
  lineCopy: {
    flex: 1,
    minWidth: 0,
    gap: 6,
  },
  linePrimary: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  linePrimaryMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
    flexShrink: 0,
  },
  lineName: {
    ...textStyles.body,
    flex: 1,
  },
  lineAmount: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
    flexShrink: 0,
  },
  metadataRow: {
    flexDirection: 'row',
    flexWrap: 'nowrap',
    gap: theme.spacing.xs,
    minWidth: 0,
    overflow: 'hidden',
  },
  metadataChip: {
    minWidth: 0,
    maxWidth: '100%',
    flexShrink: 1,
    borderRadius: theme.radius.pill,
    paddingHorizontal: 8,
    paddingVertical: 3,
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  metadataText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  expandedDetails: {
    gap: 2,
  },
  expandedContributorList: {
    gap: 6,
  },
  expandedContributorGroup: {
    gap: 2,
  },
  expandedContributorOccurrences: {
    paddingLeft: theme.spacing.sm,
    gap: 2,
  },
  expandedContributorTitle: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '600',
  },
  expandedOccurrenceText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  expandedOverflowText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  expandedDetailText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  lineRowExpandable: {
    paddingRight: theme.spacing.xs,
  },
  footer: {
    gap: theme.spacing.sm,
    paddingTop: theme.spacing.xs,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
