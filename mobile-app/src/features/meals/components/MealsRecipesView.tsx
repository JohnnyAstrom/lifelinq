import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppChip, AppInput, Subtle } from '../../../shared/ui/components';
import { iconBackground, textStyles, theme } from '../../../shared/ui/theme';

type RecipeListItem = {
  recipeId: string;
  name: string;
  sourceName: string | null;
  ingredientCount: number;
  duplicateNameCount: number;
  similarNameCount: number;
  identitySummary: string | null;
  archivedAt: string | null;
};

type Strings = {
  title: string;
  addRecipe: string;
  addRecipeHint: string;
  archivedAction: string;
  archivedTitle: string;
  savedRecipesLabel: string;
  makeSoonTitle: string;
  recentlyUsedTitle: string;
  browseAllLabel: string;
  browseMakeSoonLabel: string;
  browseRecentLabel: string;
  searchPlaceholder: string;
  loadingRecipes: string;
  noRecipes: string;
  noRecipesHint?: string;
  noMakeSoonRecipes: string;
  noMakeSoonRecipesHint?: string;
  noRecentRecipes: string;
  noRecentRecipesHint?: string;
  noArchivedRecipes: string;
  noArchivedRecipesHint?: string;
  noSearchResults: string;
  noSearchResultsHint?: string;
  duplicateNameHint: (count: number) => string;
  similarNameHint: string;
  recipeCountLabel: (count: number) => string;
  archivedCountLabel: (count: number) => string;
  matchingRecipesLabel: (count: number) => string;
};

type Props = {
  recipes: RecipeListItem[];
  searchQuery: string;
  listMode: 'active' | 'archived';
  browseMode: 'all' | 'makeSoon' | 'recent';
  activeCount: number;
  archivedCount: number;
  makeSoonCount: number;
  recentCount: number;
  isLoading: boolean;
  error: string | null;
  onShowArchived: () => void;
  onShowAllRecipes: () => void;
  onShowMakeSoonRecipes: () => void;
  onShowRecentRecipes: () => void;
  onChangeSearchQuery: (value: string) => void;
  onOpenRecipe: (recipeId: string) => void;
  onAddRecipe: () => void;
  strings: Strings;
};

export function MealsRecipesView({
  recipes,
  searchQuery,
  listMode,
  browseMode,
  activeCount,
  archivedCount,
  makeSoonCount,
  recentCount,
  isLoading,
  error,
  onShowArchived,
  onShowAllRecipes,
  onShowMakeSoonRecipes,
  onShowRecentRecipes,
  onChangeSearchQuery,
  onOpenRecipe,
  onAddRecipe,
  strings,
}: Props) {
  const hasSearchQuery = searchQuery.trim().length > 0;
  const isArchivedView = listMode === 'archived';
  const isAllBrowse = browseMode === 'all';
  const isMakeSoonBrowse = browseMode === 'makeSoon';
  const isRecentBrowse = browseMode === 'recent';
  const libraryTitle = !isArchivedView ? strings.title : strings.archivedTitle;
  const shouldShowAlphabetSections = !isArchivedView && isAllBrowse && !hasSearchQuery;
  const mainLibraryLabel = isArchivedView
    ? null
    : isMakeSoonBrowse
      ? strings.makeSoonTitle
      : isRecentBrowse
        ? strings.recentlyUsedTitle
        : strings.savedRecipesLabel;
  const mainLibraryMeta = hasSearchQuery
    ? strings.matchingRecipesLabel(recipes.length)
    : isArchivedView
      ? strings.archivedCountLabel(archivedCount)
      : null;

  function getBrowseEmptyState() {
    if (isArchivedView) {
      return {
        title: strings.noArchivedRecipes,
        hint: strings.noArchivedRecipesHint,
      };
    }
    if (isMakeSoonBrowse) {
      return {
        title: strings.noMakeSoonRecipes,
        hint: strings.noMakeSoonRecipesHint,
      };
    }
    if (isRecentBrowse) {
      return {
        title: strings.noRecentRecipes,
        hint: strings.noRecentRecipesHint,
      };
    }
    return {
      title: strings.noRecipes,
      hint: strings.noRecipesHint,
    };
  }

  function renderRecipeRows(
    items: RecipeListItem[],
    sectionKey: string,
  ) {
    return items.map((recipe, index) => {
      const ingredientLabel = recipe.ingredientCount === 1
        ? '1 ingredient'
        : `${recipe.ingredientCount} ingredients`;
      const hasCollisionHint = recipe.duplicateNameCount > 1 || recipe.similarNameCount > 1;
      const supportingLine = hasCollisionHint
        ? recipe.identitySummary
        : (recipe.sourceName ?? recipe.identitySummary);
      const metaLine = supportingLine
        ? `${supportingLine} · ${ingredientLabel}`
        : ingredientLabel;
      return (
        <Pressable
          key={`${sectionKey}-${recipe.recipeId}`}
          onPress={() => onOpenRecipe(recipe.recipeId)}
          style={({ pressed }) => [
            styles.row,
            index > 0 ? styles.rowBorder : null,
            pressed ? styles.rowPressed : null,
          ]}
        >
          <View style={styles.rowCopy}>
            {hasCollisionHint ? (
              <View style={styles.rowMetaTop}>
                <Text style={styles.duplicateHint}>
                  {recipe.duplicateNameCount > 1
                    ? strings.duplicateNameHint(recipe.duplicateNameCount)
                    : strings.similarNameHint}
                </Text>
              </View>
            ) : null}
            <Text style={styles.rowTitle}>{recipe.name}</Text>
            <Text
              style={styles.rowMeta}
              numberOfLines={2}
            >
              {metaLine}
            </Text>
          </View>
          <Ionicons
            name="chevron-forward"
            size={18}
            color={theme.colors.textSecondary}
          />
        </Pressable>
      );
    });
  }

  function renderAlphabetSections(items: RecipeListItem[]) {
    const sections = new Map<string, RecipeListItem[]>();

    for (const recipe of items) {
      const letter = recipe.name.trim().charAt(0).toLocaleUpperCase();
      const sectionKey = /[\p{L}\p{N}]/u.test(letter) ? letter : '#';
      const existing = sections.get(sectionKey);
      if (existing) {
        existing.push(recipe);
      } else {
        sections.set(sectionKey, [recipe]);
      }
    }

    return Array.from(sections.entries()).map(([sectionKey, sectionItems], index) => (
      <View
        key={`section-${sectionKey}`}
        style={index > 0 ? styles.alphabetSectionBorder : null}
      >
        <View style={styles.alphabetSectionHeader}>
          <Text style={styles.alphabetSectionLabel}>{sectionKey}</Text>
        </View>
        <View style={styles.list}>
          {renderRecipeRows(sectionItems, `section-${sectionKey}`)}
        </View>
      </View>
    ));
  }

  return (
    <View style={styles.layout}>
      {!isArchivedView ? (
        <View style={styles.controlsZone}>
          <View style={styles.controlsHeader}>
            <Text style={styles.controlsTitle}>{strings.title}</Text>
          </View>
          <View style={styles.controlsSearchRow}>
            <AppInput
              value={searchQuery}
              onChangeText={onChangeSearchQuery}
              placeholder={strings.searchPlaceholder}
              style={styles.searchInput}
            />
          </View>
          <View style={styles.controlsDivider} />
          <Pressable
            onPress={onAddRecipe}
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.captureActionRow,
              pressed ? styles.actionPressed : null,
            ]}
          >
            <View style={styles.captureActionIcon}>
              <Ionicons name="add" size={18} color={theme.colors.feature.meals} />
            </View>
            <View style={styles.captureActionCopy}>
              <Text style={styles.captureActionTitle}>{strings.addRecipe}</Text>
            </View>
            <Ionicons
              name="chevron-forward"
              size={18}
              color={theme.colors.textSecondary}
            />
          </Pressable>
        </View>
      ) : null}

      {isArchivedView ? (
        <>
          <View style={styles.archivedHeader}>
            <Text style={styles.libraryTitle}>{libraryTitle}</Text>
          </View>
          <View style={styles.controlsSearchRow}>
            <AppInput
              value={searchQuery}
              onChangeText={onChangeSearchQuery}
              placeholder={strings.searchPlaceholder}
              style={styles.searchInput}
            />
          </View>
        </>
      ) : null}

      <View style={styles.workspaceBody}>
        {isLoading || error ? (
          <View style={styles.feedbackBlock}>
            {isLoading ? <Subtle>{strings.loadingRecipes}</Subtle> : null}
            {error ? <Text style={styles.error}>{error}</Text> : null}
          </View>
        ) : recipes.length === 0 && hasSearchQuery ? (
          <View style={styles.mainListSurface}>
            <View style={styles.emptyState}>
              <Text style={styles.emptyStateTitle}>{strings.noSearchResults}</Text>
              {strings.noSearchResultsHint ? (
                <Subtle>{strings.noSearchResultsHint}</Subtle>
              ) : null}
            </View>
          </View>
        ) : recipes.length === 0 ? (
          <View style={styles.mainListSurface}>
            <View style={styles.emptyState}>
              <Text style={styles.emptyStateTitle}>{getBrowseEmptyState().title}</Text>
              {getBrowseEmptyState().hint ? (
                <Subtle>{getBrowseEmptyState().hint}</Subtle>
              ) : null}
            </View>
          </View>
        ) : (
          <View style={styles.sections}>
            <View style={styles.mainLibrarySection}>
              <View style={styles.mainLibraryHeader}>
                <View style={styles.mainLibraryCopy}>
                  {mainLibraryLabel ? (
                    <Text style={styles.mainLibraryLabel}>{mainLibraryLabel}</Text>
                  ) : null}
                  {mainLibraryMeta ? (
                    <Text style={styles.mainLibraryMeta}>{mainLibraryMeta}</Text>
                  ) : null}
                </View>
                {!isArchivedView ? (
                  <Pressable
                    onPress={onShowArchived}
                    accessibilityRole="button"
                    style={({ pressed }) => [
                      styles.archivedLink,
                      pressed ? styles.actionPressed : null,
                    ]}
                  >
                    <Ionicons name="archive-outline" size={16} color={theme.colors.textSecondary} />
                    <Text style={styles.archivedLinkText}>{strings.archivedAction}</Text>
                  </Pressable>
                ) : null}
              </View>
              {!isArchivedView ? (
                <View style={styles.browseAidRow}>
                  <AppChip
                    label={strings.browseAllLabel}
                    active={isAllBrowse}
                    onPress={onShowAllRecipes}
                    accentKey="meals"
                  />
                  <AppChip
                    label={strings.browseMakeSoonLabel}
                    active={isMakeSoonBrowse}
                    onPress={onShowMakeSoonRecipes}
                    accentKey="meals"
                  />
                  <AppChip
                    label={strings.browseRecentLabel}
                    active={isRecentBrowse}
                    onPress={onShowRecentRecipes}
                    accentKey="meals"
                  />
                </View>
              ) : null}
              <View style={styles.mainListSurface}>
                <View style={styles.mainListSection}>
                  {shouldShowAlphabetSections
                    ? renderAlphabetSections(recipes)
                    : (
                      <View style={styles.list}>
                        {renderRecipeRows(recipes, 'main')}
                      </View>
                    )}
                </View>
              </View>
            </View>
          </View>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  layout: {
    gap: theme.spacing.md,
  },
  controlsZone: {
    gap: theme.spacing.xs,
    padding: theme.spacing.sm,
    borderRadius: theme.radius.cardRadius,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.card,
  },
  controlsHeader: {
    gap: 2,
  },
  controlsTitle: {
    ...textStyles.h2,
    color: theme.colors.textPrimary,
  },
  controlsSearchRow: {
    gap: theme.spacing.xs,
  },
  captureActionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    minHeight: 44,
    paddingHorizontal: 2,
    paddingVertical: 2,
  },
  captureActionIcon: {
    width: 36,
    height: 36,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: iconBackground(theme.colors.feature.meals, 0.12),
  },
  captureActionCopy: {
    flex: 1,
    minWidth: 0,
  },
  captureActionTitle: {
    ...textStyles.body,
    color: theme.colors.textPrimary,
    fontWeight: '700',
    lineHeight: 20,
  },
  libraryTitle: {
    ...textStyles.h2,
    color: theme.colors.textPrimary,
  },
  libraryMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    lineHeight: 18,
  },
  actionPressed: {
    opacity: 0.72,
  },
  controlsDivider: {
    height: 1,
    backgroundColor: theme.colors.border,
  },
  searchInput: {
    backgroundColor: theme.colors.surface,
  },
  archivedHeader: {
    paddingTop: theme.spacing.xs,
  },
  workspaceBody: {
    gap: theme.spacing.sm,
  },
  feedbackBlock: {
    paddingHorizontal: theme.spacing.xs,
    gap: theme.spacing.xs,
  },
  sections: {
    gap: theme.spacing.md,
  },
  mainLibrarySection: {
    gap: theme.spacing.xs,
  },
  mainLibraryHeader: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  mainLibraryCopy: {
    gap: 2,
    flex: 1,
    minWidth: 0,
  },
  mainLibraryLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.4,
  },
  mainLibraryMeta: {
    ...textStyles.body,
    color: theme.colors.textPrimary,
    fontWeight: '600',
  },
  browseAidRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  archivedLink: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingVertical: 4,
  },
  archivedLinkText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  emptyState: {
    gap: 4,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
  },
  emptyStateTitle: {
    ...textStyles.body,
    fontWeight: '600',
    color: theme.colors.textPrimary,
  },
  list: {
    gap: 0,
  },
  mainListSurface: {
    backgroundColor: theme.colors.card,
    borderRadius: theme.radius.cardRadius,
    borderWidth: 1,
    borderColor: theme.colors.border,
    overflow: 'hidden',
  },
  mainListSection: {
    gap: 0,
  },
  alphabetSectionHeader: {
    paddingHorizontal: theme.spacing.sm,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.xs,
  },
  alphabetSectionLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.4,
  },
  alphabetSectionBorder: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
  },
  rowBorder: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  rowPressed: {
    opacity: 0.8,
  },
  rowCopy: {
    flex: 1,
    minWidth: 0,
    gap: 4,
  },
  rowMetaTop: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    marginBottom: 2,
  },
  duplicateHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  rowTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  rowMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
