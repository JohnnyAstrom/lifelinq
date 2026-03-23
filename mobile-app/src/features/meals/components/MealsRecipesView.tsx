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
  subtitle?: string;
  libraryLabel?: string;
  browseHint?: string;
  newRecipe: string;
  importRecipe: string;
  archivedAction: string;
  archivedTitle: string;
  archivedSubtitle?: string;
  archivedBackAction: string;
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
  makeSoonRecipes: RecipeListItem[];
  recentRecipes: RecipeListItem[];
  searchQuery: string;
  listMode: 'active' | 'archived';
  browseMode: 'all' | 'makeSoon' | 'recent';
  activeCount: number;
  archivedCount: number;
  makeSoonCount: number;
  recentCount: number;
  isLoading: boolean;
  error: string | null;
  onShowActive: () => void;
  onShowArchived: () => void;
  onShowAllRecipes: () => void;
  onShowMakeSoonRecipes: () => void;
  onShowRecentRecipes: () => void;
  onChangeSearchQuery: (value: string) => void;
  onOpenRecipe: (recipeId: string) => void;
  onCreateRecipe: () => void;
  onImportRecipe: () => void;
  strings: Strings;
};

export function MealsRecipesView({
  recipes,
  makeSoonRecipes,
  recentRecipes,
  searchQuery,
  listMode,
  browseMode,
  activeCount,
  archivedCount,
  makeSoonCount,
  recentCount,
  isLoading,
  error,
  onShowActive,
  onShowArchived,
  onShowAllRecipes,
  onShowMakeSoonRecipes,
  onShowRecentRecipes,
  onChangeSearchQuery,
  onOpenRecipe,
  onCreateRecipe,
  onImportRecipe,
  strings,
}: Props) {
  const supportPreviewLimit = 3;
  const hasSearchQuery = searchQuery.trim().length > 0;
  const isArchivedView = listMode === 'archived';
  const isAllBrowse = browseMode === 'all';
  const isMakeSoonBrowse = browseMode === 'makeSoon';
  const isRecentBrowse = browseMode === 'recent';
  const showMakeSoon = !isArchivedView && isAllBrowse && !hasSearchQuery && makeSoonRecipes.length > 0;
  const showRecentlyUsed = !isArchivedView
    && isAllBrowse
    && !hasSearchQuery
    && !showMakeSoon
    && recentRecipes.length > 0;
  const showTopSections = showMakeSoon || showRecentlyUsed;
  const mainCountLabel = isArchivedView
    ? strings.archivedCountLabel(archivedCount)
    : isMakeSoonBrowse
      ? strings.recipeCountLabel(makeSoonCount)
      : isRecentBrowse
        ? strings.recipeCountLabel(recentCount)
        : strings.recipeCountLabel(activeCount);
  const libraryCountLabel = !isArchivedView
    ? strings.recipeCountLabel(activeCount)
    : strings.archivedCountLabel(archivedCount);
  const libraryTitle = !isArchivedView ? strings.title : strings.archivedTitle;
  const librarySubtitle = !isArchivedView ? strings.subtitle : strings.archivedSubtitle;
  const libraryMeta = librarySubtitle ? `${libraryCountLabel} · ${librarySubtitle}` : libraryCountLabel;
  const supportTitle = showMakeSoon ? strings.makeSoonTitle : showRecentlyUsed ? strings.recentlyUsedTitle : null;
  const supportItems = (showMakeSoon ? makeSoonRecipes : showRecentlyUsed ? recentRecipes : [])
    .slice(0, supportPreviewLimit);
  const shouldShowAlphabetSections = !isArchivedView && isAllBrowse && !hasSearchQuery;
  const mainLibraryLabel = isArchivedView
    ? strings.archivedTitle
    : isMakeSoonBrowse
      ? strings.makeSoonTitle
      : isRecentBrowse
        ? strings.recentlyUsedTitle
        : strings.savedRecipesLabel;
  const mainLibraryMeta = hasSearchQuery
    ? strings.matchingRecipesLabel(recipes.length)
    : mainCountLabel;

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
    variant: 'main' | 'support' = 'main',
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
            variant === 'support' ? styles.supportRow : styles.row,
            index > 0
              ? (variant === 'support' ? styles.supportRowBorder : styles.rowBorder)
              : null,
            pressed ? styles.rowPressed : null,
          ]}
        >
          <View style={styles.rowCopy}>
            {hasCollisionHint && variant === 'main' ? (
              <View style={styles.rowMetaTop}>
                <Text style={styles.duplicateHint}>
                  {recipe.duplicateNameCount > 1
                    ? strings.duplicateNameHint(recipe.duplicateNameCount)
                    : strings.similarNameHint}
                </Text>
              </View>
            ) : null}
            <Text style={variant === 'support' ? styles.supportRowTitle : styles.rowTitle}>
              {recipe.name}
            </Text>
            <Text
              style={variant === 'support' ? styles.supportRowMeta : styles.rowMeta}
              numberOfLines={variant === 'support' ? 1 : 2}
            >
              {metaLine}
            </Text>
          </View>
          <Ionicons
            name="chevron-forward"
            size={variant === 'support' ? 16 : 18}
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
        <View style={styles.libraryHero}>
          <View style={styles.libraryHeroTop}>
            <View style={styles.libraryHeroBadge}>
              <Ionicons name="book-outline" size={18} color={theme.colors.feature.meals} />
            </View>
            <View style={styles.libraryHeroCopy}>
              {strings.libraryLabel ? (
                <Text style={styles.libraryEyebrow}>{strings.libraryLabel}</Text>
              ) : null}
              <Text style={styles.libraryHeroTitle}>{strings.title}</Text>
              <Text style={styles.libraryHeroSubtitle}>
                {strings.subtitle ?? 'Browse, re-find, and save recipes you want to keep using.'}
              </Text>
            </View>
          </View>
          <View style={styles.libraryActionRow}>
            <Pressable
              onPress={onImportRecipe}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.primaryLibraryAction,
                pressed ? styles.actionPressed : null,
              ]}
            >
              <Ionicons name="bookmark-outline" size={16} color={theme.colors.feature.meals} />
              <Text style={styles.primaryLibraryActionText}>{strings.importRecipe}</Text>
            </Pressable>
            <Pressable
              onPress={onCreateRecipe}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.secondaryLibraryAction,
                pressed ? styles.actionPressed : null,
              ]}
            >
              <Ionicons name="add" size={16} color={theme.colors.textSecondary} />
              <Text style={styles.secondaryLibraryActionText}>{strings.newRecipe}</Text>
            </Pressable>
          </View>
          <View style={styles.retrievalBand}>
            <AppInput
              value={searchQuery}
              onChangeText={onChangeSearchQuery}
              placeholder={strings.searchPlaceholder}
              style={styles.searchInput}
            />
            {strings.browseHint && !hasSearchQuery ? (
              <Text style={styles.retrievalHint}>{strings.browseHint}</Text>
            ) : null}
          </View>
        </View>
      ) : null}

      {isArchivedView ? (
        <>
          <View style={styles.archivedHeader}>
            <Pressable
              onPress={onShowActive}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.archivedBackAction,
                pressed ? styles.actionPressed : null,
              ]}
            >
              <Ionicons name="arrow-back" size={16} color={theme.colors.textSecondary} />
              <Text style={styles.archivedBackActionText}>{strings.archivedBackAction}</Text>
            </Pressable>
            <View style={styles.archivedHeaderCopy}>
              <Text style={styles.libraryEyebrow}>{strings.archivedAction}</Text>
              <Text style={styles.libraryTitle}>{libraryTitle}</Text>
              <Text style={styles.libraryMeta}>{libraryMeta}</Text>
            </View>
          </View>
          <View style={styles.retrievalBand}>
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
            {showTopSections && !isArchivedView ? (
              <View style={styles.supportLayer}>
                {supportTitle ? (
                  <Text style={styles.supportLabel}>{supportTitle}</Text>
                ) : null}
                <View style={styles.supportSurface}>
                  <View style={styles.supportList}>
                    {renderRecipeRows(supportItems, showMakeSoon ? 'soon' : 'recent', 'support')}
                  </View>
                </View>
              </View>
            ) : null}
            <View style={styles.mainLibrarySection}>
              <View style={styles.mainLibraryHeader}>
                <View style={styles.mainLibraryCopy}>
                  <Text style={styles.mainLibraryLabel}>{mainLibraryLabel}</Text>
                  <Text style={styles.mainLibraryMeta}>{mainLibraryMeta}</Text>
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
  libraryHero: {
    gap: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    borderRadius: theme.radius.xl,
    backgroundColor: iconBackground(theme.colors.feature.meals, 0.12),
  },
  libraryHeroTop: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: theme.spacing.sm,
  },
  libraryHeroBadge: {
    width: 38,
    height: 38,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.surface,
  },
  libraryHeroCopy: {
    flex: 1,
    minWidth: 0,
    gap: 4,
  },
  libraryEyebrow: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 0.4,
  },
  libraryHeroTitle: {
    ...textStyles.h1,
    color: theme.colors.textPrimary,
  },
  libraryHeroSubtitle: {
    ...textStyles.body,
    color: theme.colors.textSecondary,
  },
  libraryActionRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  primaryLibraryAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 10,
    borderRadius: theme.radius.pill,
    backgroundColor: theme.colors.surface,
  },
  secondaryLibraryAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 10,
    borderRadius: theme.radius.pill,
    backgroundColor: iconBackground(theme.colors.surface, 0.42),
    borderWidth: 1,
    borderColor: iconBackground(theme.colors.textPrimary, 0.08),
  },
  primaryLibraryActionText: {
    ...textStyles.subtle,
    color: theme.colors.textPrimary,
    fontWeight: '700',
  },
  secondaryLibraryActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '700',
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
  retrievalBand: {
    gap: theme.spacing.xs,
  },
  retrievalHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  searchInput: {
    backgroundColor: theme.colors.surface,
  },
  archivedHeader: {
    gap: theme.spacing.xs,
  },
  archivedBackAction: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    gap: 6,
  },
  archivedBackActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  archivedHeaderCopy: {
    gap: 4,
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
  supportLayer: {
    gap: 6,
  },
  supportSurface: {
    backgroundColor: iconBackground(theme.colors.feature.meals, 0.07),
    borderRadius: theme.radius.lg,
    paddingVertical: 2,
    paddingHorizontal: 2,
  },
  supportList: {
    gap: 0,
  },
  supportLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
    paddingHorizontal: 2,
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
  supportRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 12,
  },
  rowBorder: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  supportRowBorder: {
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
  supportRowTitle: {
    ...textStyles.body,
    fontWeight: '600',
    color: theme.colors.textPrimary,
  },
  rowMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  supportRowMeta: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
