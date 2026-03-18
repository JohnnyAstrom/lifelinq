import { useEffect, useRef, useState } from 'react';
import {
  ScrollView,
  StyleSheet,
  Text,
  View,
  type KeyboardTypeOptions,
  type StyleProp,
  type TextStyle,
  type ViewStyle,
} from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import {
  type MealIngredientRow,
  type MealIngredientUnit,
} from '../utils/ingredientRows';
import {
  getImportedIngredientReviewInfo,
  isMealIngredientRowEffectivelyEmpty,
  MealIngredientEditorRow,
  type MealIngredientEditorRowStrings,
} from './MealIngredientEditorRow';

type Strings = MealIngredientEditorRowStrings & {
  eyebrow: string;
  title: string;
  subtitle?: string;
  newRecipeLabel: string;
  usingRecipeLabel: string;
  mealSpecificRecipeLabel: string;
  editingSavedRecipeLabel: string;
  importDraftLabel: string;
  archivedRecipeLabel?: string;
  newRecipeTitle: string;
  recipeContextHint?: string;
  archivedReadOnlyHint?: string;
  recipeNameEditHint?: string;
  mealAttachmentLabel?: string;
  mealAttachmentValue?: string;
  editSavedRecipeAction?: string;
  editingSavedRecipeHint?: string;
  recipeNameLabel: string;
  recipeNamePlaceholder: string;
  recipeContentLabel: string;
  recipeMetadataHint?: string;
  recipeSourceLabel: string;
  recipeSourcePlaceholder: string;
  recipeSourceUrlLabel?: string;
  recipeSourceUrlPlaceholder?: string;
  recipeShortNoteLabel: string;
  recipeShortNotePlaceholder: string;
  recipeShortNoteHint?: string;
  recipeInstructionsLabel: string;
  recipeInstructionsPlaceholder: string;
  recipeInstructionsHint?: string;
  importInstructionsHint?: string;
  ingredientsLabel: string;
  ingredientsRecipeHint?: string;
  importedIngredientsHint?: string;
  importReviewLabel?: string;
  importReviewHint?: string;
  importReviewSummary?: (count: number) => string;
  importReviewSourceSummaryTitle?: string;
  importReviewSourceSummaryHint?: string;
  importReviewSourceEmpty?: string;
  importReviewSourceUrlEmpty?: string;
  ingredientsEmptyState: string;
  loadingIngredients: string;
  saveAsNewRecipeHint?: string;
  saveRecipe?: string;
  savingRecipe?: string;
  archiveRecipe?: string;
  archivingRecipe?: string;
  restoreRecipe?: string;
  restoringRecipe?: string;
  deleteRecipe?: string;
  deletingRecipe?: string;
  addIngredient: string;
  importedIngredientHint?: string;
  close: string;
};

type ReadOnlyFieldProps = {
  label: string;
  value: string;
  emptyText?: string;
  multiline?: boolean;
};

function ReadOnlyField({ label, value, emptyText, multiline = false }: ReadOnlyFieldProps) {
  const trimmedValue = value.trim();
  const hasValue = trimmedValue.length > 0;

  return (
    <View style={styles.subSection}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <View style={[styles.readOnlyField, multiline ? styles.readOnlyFieldMultiline : null]}>
        {hasValue ? (
          <Text style={styles.readOnlyValue}>{trimmedValue}</Text>
        ) : (
          <Text style={styles.readOnlyEmpty}>
            {emptyText ?? `${label} not added yet.`}
          </Text>
        )}
      </View>
    </View>
  );
}

type EditableFieldProps = {
  label: string;
  hint?: string;
  placeholder: string;
  value: string;
  onChangeText: (value: string) => void;
  multiline?: boolean;
  keyboardType?: KeyboardTypeOptions;
  inputStyle?: StyleProp<TextStyle>;
  cardStyle?: StyleProp<ViewStyle>;
};

function EditableField({
  label,
  hint,
  placeholder,
  value,
  onChangeText,
  multiline = false,
  keyboardType,
  inputStyle,
  cardStyle,
}: EditableFieldProps) {
  return (
    <View style={styles.subSection}>
      <View style={styles.sectionCopy}>
        <Text style={styles.sectionTitle}>{label}</Text>
        {hint ? <Text style={styles.sectionHint}>{hint}</Text> : null}
      </View>
      <View style={[styles.editorCard, multiline ? styles.editorCardMultiline : null, cardStyle]}>
        <AppInput
          placeholder={placeholder}
          value={value}
          onChangeText={onChangeText}
          multiline={multiline}
          keyboardType={keyboardType}
          editable
          style={[styles.embeddedInput, inputStyle]}
        />
      </View>
    </View>
  );
}

type Props = {
  recipeTitle: string;
  onChangeRecipeTitle: (value: string) => void;
  recipeSource: string;
  onChangeRecipeSource: (value: string) => void;
  recipeSourceUrl?: string;
  onChangeRecipeSourceUrl?: (value: string) => void;
  recipeShortNote: string;
  onChangeRecipeShortNote: (value: string) => void;
  recipeInstructions: string;
  onChangeRecipeInstructions: (value: string) => void;
  ingredientRows: MealIngredientRow[];
  isRecipeLoading: boolean;
  hasExistingRecipe: boolean;
  isImportDraft?: boolean;
  isArchivedRecipe?: boolean;
  hasIngredients: boolean;
  showSaveAsNewRecipeHint: boolean;
  canEnterSavedRecipeEditMode: boolean;
  isEditingSavedRecipeDirectly: boolean;
  isActionPending: boolean;
  onAddIngredientRow: () => void;
  onRemoveIngredientRow: (rowId: string) => void;
  onChangeIngredientName: (rowId: string, value: string) => void;
  onChangeIngredientQuantity: (rowId: string, value: string) => void;
  onToggleIngredientUnit: (rowId: string, value: MealIngredientUnit) => void;
  onStartEditingSavedRecipeDirectly: () => void;
  onSave?: () => void;
  onArchive?: () => void;
  onRestore?: () => void;
  onDelete?: () => void;
  onClose: () => void;
  isSaving?: boolean;
  isArchiving?: boolean;
  isDeleting?: boolean;
  canDelete?: boolean;
  deleteBlockedHint?: string | null;
  error?: string | null;
  strings: Strings;
};

export function MealRecipeDetailSheet({
  recipeTitle,
  onChangeRecipeTitle,
  recipeSource,
  onChangeRecipeSource,
  recipeSourceUrl,
  onChangeRecipeSourceUrl,
  recipeShortNote,
  onChangeRecipeShortNote,
  recipeInstructions,
  onChangeRecipeInstructions,
  ingredientRows,
  isRecipeLoading,
  hasExistingRecipe,
  isImportDraft = false,
  isArchivedRecipe = false,
  hasIngredients,
  showSaveAsNewRecipeHint,
  canEnterSavedRecipeEditMode,
  isEditingSavedRecipeDirectly,
  isActionPending,
  onAddIngredientRow,
  onRemoveIngredientRow,
  onChangeIngredientName,
  onChangeIngredientQuantity,
  onToggleIngredientUnit,
  onStartEditingSavedRecipeDirectly,
  onSave,
  onArchive,
  onRestore,
  onDelete,
  onClose,
  isSaving = false,
  isArchiving = false,
  isDeleting = false,
  canDelete = false,
  deleteBlockedHint = null,
  error = null,
  strings,
}: Props) {
  const [activeRowId, setActiveRowId] = useState<string | null>(null);
  const previousRowCountRef = useRef(ingredientRows.length);
  const resolvedTitle = recipeTitle.trim().length > 0
    ? recipeTitle.trim()
    : strings.newRecipeTitle;
  const isMealSpecificDraft = hasExistingRecipe
    && showSaveAsNewRecipeHint
    && !isEditingSavedRecipeDirectly;
  const importReviewRows = isImportDraft
    ? ingredientRows.filter((row) => getImportedIngredientReviewInfo(row).needsReview)
    : [];
  const importReviewCount = importReviewRows.length;
  const identityLabel = isMealSpecificDraft
    ? strings.mealSpecificRecipeLabel
    : isImportDraft
      ? strings.importDraftLabel
    : isArchivedRecipe && strings.archivedRecipeLabel
      ? strings.archivedRecipeLabel
    : isEditingSavedRecipeDirectly
      ? strings.editingSavedRecipeLabel
    : hasExistingRecipe
      ? strings.usingRecipeLabel
      : strings.newRecipeLabel;

  useEffect(() => {
    const previousCount = previousRowCountRef.current;
    previousRowCountRef.current = ingredientRows.length;

    if (ingredientRows.length === 0) {
      if (activeRowId !== null) {
        setActiveRowId(null);
      }
      return;
    }

    if (ingredientRows.length > previousCount) {
      setActiveRowId(ingredientRows[ingredientRows.length - 1]?.id ?? null);
      return;
    }

    if (activeRowId === null) {
      return;
    }

    const hasActiveRow = activeRowId != null && ingredientRows.some((row) => row.id === activeRowId);
    if (hasActiveRow) {
      return;
    }

    const firstEmptyRow = ingredientRows.find((row) => isMealIngredientRowEffectivelyEmpty(row));
    setActiveRowId(firstEmptyRow?.id ?? null);
  }, [ingredientRows, activeRowId]);

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={styles.eyebrow}>{strings.eyebrow}</Text>
          <Text style={textStyles.h2}>{resolvedTitle}</Text>
          {strings.subtitle ? <Subtle>{strings.subtitle}</Subtle> : null}
          <View style={styles.headerMeta}>
            <View style={styles.identityBadge}>
              <Text style={styles.identityBadgeText}>{identityLabel}</Text>
            </View>
            {strings.mealAttachmentLabel && strings.mealAttachmentValue ? (
              <View style={styles.attachmentRow}>
                <Text style={styles.attachmentLabel}>{strings.mealAttachmentLabel}</Text>
                <Text style={styles.attachmentValue}>{strings.mealAttachmentValue}</Text>
              </View>
            ) : null}
            {strings.recipeContextHint ? (
              <Text style={styles.contextHint}>{strings.recipeContextHint}</Text>
            ) : null}
            {isArchivedRecipe && strings.archivedReadOnlyHint ? (
              <Subtle>{strings.archivedReadOnlyHint}</Subtle>
            ) : null}
            {showSaveAsNewRecipeHint && !isEditingSavedRecipeDirectly && strings.saveAsNewRecipeHint ? (
              <Subtle>{strings.saveAsNewRecipeHint}</Subtle>
            ) : null}
            {isEditingSavedRecipeDirectly && strings.editingSavedRecipeHint ? (
              <Subtle>{strings.editingSavedRecipeHint}</Subtle>
            ) : null}
            {canEnterSavedRecipeEditMode && strings.editSavedRecipeAction ? (
              <AppButton
                title={strings.editSavedRecipeAction}
                onPress={onStartEditingSavedRecipeDirectly}
                variant="ghost"
                disabled={isActionPending || isRecipeLoading}
              />
            ) : null}
          </View>
        </View>

        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            {isImportDraft && (strings.importReviewLabel || strings.importReviewHint) ? (
              <View style={styles.reviewIntro}>
                {strings.importReviewLabel ? (
                  <Text style={styles.reviewIntroLabel}>{strings.importReviewLabel}</Text>
                ) : null}
                {strings.importReviewHint ? (
                  <Text style={styles.reviewIntroText}>{strings.importReviewHint}</Text>
                ) : null}
              </View>
            ) : null}

            <View style={styles.section}>
              {isArchivedRecipe ? (
                <>
                  <Text style={styles.fieldLabel}>{strings.recipeNameLabel}</Text>
                  <View style={styles.readOnlyField}>
                    <Text style={styles.readOnlyValue}>{resolvedTitle}</Text>
                  </View>
                </>
              ) : (
                <EditableField
                  label={strings.recipeNameLabel}
                  hint={isImportDraft ? undefined : strings.recipeNameEditHint}
                  placeholder={strings.recipeNamePlaceholder}
                  value={recipeTitle}
                  onChangeText={onChangeRecipeTitle}
                  multiline={isImportDraft}
                  cardStyle={isImportDraft ? styles.reviewTitleCard : null}
                  inputStyle={[
                    styles.titleInput,
                    isImportDraft ? styles.reviewTitleInput : null,
                  ]}
                />
              )}
            </View>

            <View style={[styles.section, !isArchivedRecipe ? styles.coreSection : null]}>
              <View style={styles.sectionHeader}>
                <View style={styles.sectionCopy}>
                  <Text style={styles.sectionTitle}>{strings.ingredientsLabel}</Text>
                  {strings.ingredientsRecipeHint ? (
                    <Text style={styles.sectionHint}>{strings.ingredientsRecipeHint}</Text>
                  ) : null}
                  {isImportDraft && strings.importedIngredientsHint ? (
                    <Text style={styles.sectionHint}>{strings.importedIngredientsHint}</Text>
                  ) : null}
                  {!isRecipeLoading && isImportDraft && strings.importReviewSummary ? (
                    <Text style={styles.reviewSectionSummary}>
                      {strings.importReviewSummary(importReviewCount)}
                    </Text>
                  ) : null}
                </View>
                {!isArchivedRecipe ? (
                  <AppButton
                    title={strings.addIngredient}
                    onPress={onAddIngredientRow}
                    variant="ghost"
                    disabled={isActionPending}
                  />
                ) : null}
              </View>

              {isRecipeLoading ? <Subtle>{strings.loadingIngredients}</Subtle> : null}

              {!isRecipeLoading && !hasIngredients ? (
                isArchivedRecipe ? (
                  <View style={styles.readOnlyField}>
                    <Text style={styles.readOnlyEmpty}>No ingredients saved on this recipe.</Text>
                  </View>
                ) : (
                  <View style={styles.emptyStateCard}>
                    <Text style={styles.emptyStateText}>{strings.ingredientsEmptyState}</Text>
                  </View>
                )
              ) : null}

              {!isRecipeLoading && hasIngredients ? (
                <View style={styles.ingredientList}>
                  {ingredientRows.map((row) => (
                    <MealIngredientEditorRow
                      key={row.id}
                      row={row}
                      isActive={row.id === activeRowId}
                      isReadOnly={isArchivedRecipe}
                      isImportDraft={isImportDraft}
                      onActivate={() => setActiveRowId(row.id)}
                      onRemove={() => onRemoveIngredientRow(row.id)}
                      onChangeName={(value) => onChangeIngredientName(row.id, value)}
                      onChangeQuantity={(value) => onChangeIngredientQuantity(row.id, value)}
                      onToggleUnit={(value) => onToggleIngredientUnit(row.id, value)}
                      strings={strings}
                    />
                  ))}
                </View>
              ) : null}
            </View>

            <View style={styles.section}>
              {isArchivedRecipe ? (
                <ReadOnlyField
                  label={strings.recipeInstructionsLabel}
                  value={recipeInstructions}
                  emptyText="No instructions saved."
                  multiline
                />
              ) : (
                <EditableField
                  label={strings.recipeInstructionsLabel}
                  hint={isImportDraft ? strings.importInstructionsHint ?? strings.recipeInstructionsHint : strings.recipeInstructionsHint}
                  placeholder={strings.recipeInstructionsPlaceholder}
                  value={recipeInstructions}
                  onChangeText={onChangeRecipeInstructions}
                  multiline
                  cardStyle={isImportDraft ? styles.reviewPrimaryCard : null}
                  inputStyle={[
                    styles.instructionsInput,
                    isImportDraft ? styles.reviewInstructionsInput : null,
                  ]}
                />
              )}
            </View>

            <View style={styles.section}>
              {isArchivedRecipe ? (
                <ReadOnlyField
                  label={strings.recipeShortNoteLabel}
                  value={recipeShortNote}
                  emptyText="No short note saved."
                  multiline
                />
              ) : (
                <EditableField
                  label={strings.recipeShortNoteLabel}
                  hint={strings.recipeShortNoteHint}
                  placeholder={strings.recipeShortNotePlaceholder}
                  value={recipeShortNote}
                  onChangeText={onChangeRecipeShortNote}
                  multiline
                  cardStyle={isImportDraft ? styles.reviewSupportingCard : null}
                  inputStyle={styles.noteInput}
                />
              )}
            </View>

            <View style={[styles.section, styles.metadataSection]}>
              {isArchivedRecipe ? (
                <>
                  <Text style={styles.fieldLabel}>{strings.recipeContentLabel}</Text>
                  <ReadOnlyField
                    label={strings.recipeSourceLabel}
                    value={recipeSource}
                    emptyText="No source saved."
                  />
                  {onChangeRecipeSourceUrl && strings.recipeSourceUrlLabel ? (
                    <ReadOnlyField
                      label={strings.recipeSourceUrlLabel}
                      value={recipeSourceUrl ?? ''}
                      emptyText="No source link saved."
                    />
                  ) : null}
                </>
              ) : (
                <>
                  <View style={styles.sectionCopy}>
                    <Text style={styles.fieldLabel}>
                      {isImportDraft && strings.importReviewSourceSummaryTitle
                        ? strings.importReviewSourceSummaryTitle
                        : strings.recipeContentLabel}
                    </Text>
                    {strings.recipeMetadataHint ? (
                      <Text style={styles.sectionHint}>{strings.recipeMetadataHint}</Text>
                    ) : null}
                  </View>
                  <View style={styles.metadataFields}>
                    <View style={styles.metadataField}>
                      <Text style={styles.fieldLabel}>{strings.recipeSourceLabel}</Text>
                      <View style={[styles.metadataInputWrap, isImportDraft ? styles.reviewMetadataInputWrap : null]}>
                        <AppInput
                          placeholder={strings.recipeSourcePlaceholder}
                          value={recipeSource}
                          onChangeText={onChangeRecipeSource}
                          editable
                          style={styles.metadataInput}
                        />
                      </View>
                    </View>
                    {onChangeRecipeSourceUrl && strings.recipeSourceUrlLabel ? (
                      <View style={styles.metadataField}>
                        <Text style={styles.fieldLabel}>{strings.recipeSourceUrlLabel}</Text>
                        <View style={[styles.metadataInputWrap, isImportDraft ? styles.reviewMetadataInputWrap : null]}>
                          <AppInput
                            placeholder={strings.recipeSourceUrlPlaceholder}
                            value={recipeSourceUrl ?? ''}
                            onChangeText={onChangeRecipeSourceUrl}
                            keyboardType="url"
                            editable
                            style={styles.metadataInput}
                          />
                        </View>
                      </View>
                    ) : null}
                  </View>
                </>
              )}
            </View>

            {error ? <Text style={styles.error}>{error}</Text> : null}

            {onSave && strings.saveRecipe && !isArchivedRecipe ? (
              <AppButton
                title={isSaving && strings.savingRecipe ? strings.savingRecipe : strings.saveRecipe}
                onPress={onSave}
                fullWidth
                disabled={isActionPending || isRecipeLoading}
                accentKey="meals"
              />
            ) : null}

            {onArchive && strings.archiveRecipe ? (
              <AppButton
                title={isArchiving && strings.archivingRecipe ? strings.archivingRecipe : strings.archiveRecipe}
                onPress={onArchive}
                variant="ghost"
                fullWidth
                disabled={isActionPending || isRecipeLoading}
              />
            ) : null}

            {onRestore && strings.restoreRecipe ? (
              <View style={styles.restoreSection}>
                <AppButton
                  title={isArchiving && strings.restoringRecipe ? strings.restoringRecipe : strings.restoreRecipe}
                  onPress={onRestore}
                  variant="secondary"
                  fullWidth
                  disabled={isActionPending || isRecipeLoading}
                />
                <Subtle>Restore this recipe to bring it back to your active recipe workspace.</Subtle>
              </View>
            ) : null}

            {strings.deleteRecipe && (onDelete || deleteBlockedHint) ? (
              <View style={styles.deleteSection}>
                <AppButton
                  title={isDeleting && strings.deletingRecipe ? strings.deletingRecipe : strings.deleteRecipe}
                  onPress={onDelete ?? (() => {})}
                  variant="ghost"
                  fullWidth
                  disabled={isActionPending || isRecipeLoading || !canDelete}
                />
                <Subtle>
                  {deleteBlockedHint ?? 'Delete permanently removes this archived recipe from Meals.'}
                </Subtle>
              </View>
            ) : null}

            <AppButton
              title={strings.close}
              onPress={onClose}
              variant="secondary"
              fullWidth
              disabled={isActionPending}
            />
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
    gap: theme.spacing.xs,
  },
  eyebrow: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    fontWeight: '700',
  },
  headerMeta: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
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
    gap: theme.spacing.sm,
  },
  reviewIntro: {
    gap: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
  },
  reviewIntroLabel: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    fontWeight: '700',
  },
  reviewIntroText: {
    ...textStyles.subtle,
    color: theme.colors.text,
  },
  coreSection: {
    paddingTop: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  metadataSection: {
    paddingTop: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  subSection: {
    gap: theme.spacing.xs,
  },
  sectionTitle: {
    ...textStyles.body,
    color: theme.colors.text,
    fontWeight: '700',
  },
  sectionHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  fieldLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  editorCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
  },
  editorCardMultiline: {
    minHeight: 96,
  },
  embeddedInput: {
    borderWidth: 0,
    borderRadius: 0,
    backgroundColor: 'transparent',
    paddingHorizontal: 0,
    paddingVertical: 0,
    color: theme.colors.text,
  },
  titleInput: {
    ...textStyles.body,
    fontSize: 17,
    fontWeight: '600',
  },
  reviewTitleCard: {
    backgroundColor: theme.colors.surface,
    paddingVertical: theme.spacing.xs,
  },
  reviewTitleInput: {
    fontSize: 20,
    lineHeight: 28,
    minHeight: 56,
    textAlignVertical: 'top',
  },
  noteInput: {
    minHeight: 88,
    textAlignVertical: 'top',
  },
  readOnlyField: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
  },
  readOnlyFieldMultiline: {
    minHeight: 88,
  },
  readOnlyValue: {
    ...textStyles.body,
    color: theme.colors.text,
  },
  readOnlyEmpty: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  instructionsInput: {
    minHeight: 168,
    textAlignVertical: 'top',
    lineHeight: 22,
  },
  reviewInstructionsInput: {
    lineHeight: 24,
  },
  reviewPrimaryCard: {
    backgroundColor: theme.colors.surface,
  },
  reviewSupportingCard: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  sectionCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  identityBadge: {
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 4,
    backgroundColor: theme.colors.surfaceAlt,
  },
  identityBadgeText: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '600',
  },
  contextHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  attachmentRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  attachmentLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  attachmentValue: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '600',
  },
  ingredientsHint: {
    ...textStyles.subtle,
  },
  ingredientList: {
    gap: theme.spacing.xs,
  },
  reviewSectionSummary: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  emptyStateCard: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
  },
  emptyStateText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  metadataFields: {
    gap: theme.spacing.sm,
  },
  provenancePreviewCard: {
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
  },
  provenancePreviewRow: {
    gap: 2,
  },
  provenancePreviewLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  provenancePreviewValue: {
    ...textStyles.body,
    color: theme.colors.text,
  },
  provenancePreviewHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    paddingTop: 2,
  },
  metadataField: {
    gap: theme.spacing.xs,
  },
  metadataInputWrap: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceSubtle,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
  },
  metadataInput: {
    borderWidth: 0,
    borderRadius: 0,
    backgroundColor: 'transparent',
    paddingHorizontal: 0,
    paddingVertical: 0,
  },
  reviewMetadataInputWrap: {
    backgroundColor: theme.colors.surface,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
  restoreSection: {
    gap: theme.spacing.xs,
  },
  deleteSection: {
    gap: theme.spacing.xs,
  },
});
