import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppInput, AppSegmentedControl, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

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
  activeTab: string;
  archivedTab: string;
  newRecipe: string;
  importRecipe: string;
  makeSoonTitle: string;
  recentlyUsedTitle: string;
  searchPlaceholder: string;
  loadingRecipes: string;
  noRecipes: string;
  noRecipesHint?: string;
  noArchivedRecipes: string;
  noArchivedRecipesHint?: string;
  noSearchResults: string;
  noSearchResultsHint?: string;
  savedRecipeLabel: string;
  archivedRecipeLabel: string;
  duplicateNameHint: (count: number) => string;
  similarNameHint: string;
  recipeCountLabel: (count: number) => string;
  archivedCountLabel: (count: number) => string;
};

type Props = {
  recipes: RecipeListItem[];
  makeSoonRecipes: RecipeListItem[];
  recentRecipes: RecipeListItem[];
  searchQuery: string;
  listMode: 'active' | 'archived';
  activeCount: number;
  archivedCount: number;
  isLoading: boolean;
  error: string | null;
  onShowActive: () => void;
  onShowArchived: () => void;
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
  activeCount,
  archivedCount,
  isLoading,
  error,
  onShowActive,
  onShowArchived,
  onChangeSearchQuery,
  onOpenRecipe,
  onCreateRecipe,
  onImportRecipe,
  strings,
}: Props) {
  const hasSearchQuery = searchQuery.trim().length > 0;
  const showMakeSoon = listMode === 'active' && !hasSearchQuery && makeSoonRecipes.length > 0;
  const showRecentlyUsed = listMode === 'active'
    && !hasSearchQuery
    && !showMakeSoon
    && recentRecipes.length > 0;
  const showTopSections = showMakeSoon || showRecentlyUsed;
  const countLabel = listMode === 'active'
    ? strings.recipeCountLabel(activeCount)
    : strings.archivedCountLabel(archivedCount);
  const libraryTitle = strings.title;
  const librarySubtitle = hasSearchQuery ? undefined : strings.subtitle;
  const libraryMeta = librarySubtitle ? `${librarySubtitle} · ${countLabel}` : countLabel;
  const supportTitle = showMakeSoon ? strings.makeSoonTitle : showRecentlyUsed ? strings.recentlyUsedTitle : null;
  const supportItems = showMakeSoon ? makeSoonRecipes : showRecentlyUsed ? recentRecipes : [];

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
              numberOfLines={1}
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

  return (
    <View style={styles.layout}>
      <View style={styles.controlsPrimaryRow}>
        <AppSegmentedControl
          options={[
            { value: 'active', label: strings.activeTab },
            { value: 'archived', label: strings.archivedTab },
          ]}
          value={listMode}
          onChange={(nextValue) => {
            if (nextValue === 'active') {
              onShowActive();
              return;
            }
            onShowArchived();
          }}
          accentKey="meals"
        />
      </View>

      <View style={styles.libraryHeader}>
        <View style={styles.libraryHeaderTop}>
          <View style={styles.libraryHeaderCopy}>
            <Text style={styles.libraryTitle}>{libraryTitle}</Text>
            <Text style={styles.libraryMeta}>{libraryMeta}</Text>
          </View>
          <View style={styles.inlineActionsRow}>
            <Pressable
              onPress={onCreateRecipe}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.toolbarAction,
                pressed ? styles.quickActionPressed : null,
              ]}
            >
              <Ionicons name="add" size={16} color={theme.colors.textSecondary} />
              <Text style={styles.toolbarActionText}>{strings.newRecipe}</Text>
            </Pressable>
            <Pressable
              onPress={onImportRecipe}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.toolbarAction,
                pressed ? styles.quickActionPressed : null,
              ]}
            >
              <Ionicons name="bookmark-outline" size={16} color={theme.colors.textSecondary} />
              <Text style={styles.toolbarActionText}>{strings.importRecipe}</Text>
            </Pressable>
          </View>
        </View>
        <View style={styles.searchRow}>
          <AppInput
            value={searchQuery}
            onChangeText={onChangeSearchQuery}
            placeholder={strings.searchPlaceholder}
            style={styles.searchInput}
          />
        </View>
      </View>

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
              <Text style={styles.emptyStateTitle}>
                {listMode === 'active' ? strings.noRecipes : strings.noArchivedRecipes}
              </Text>
              {(listMode === 'active' ? strings.noRecipesHint : strings.noArchivedRecipesHint) ? (
                <Subtle>
                  {listMode === 'active' ? strings.noRecipesHint : strings.noArchivedRecipesHint}
                </Subtle>
              ) : null}
            </View>
          </View>
        ) : (
          <View style={styles.sections}>
            {showTopSections ? (
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
            <View style={styles.mainListSurface}>
              <View style={styles.mainListSection}>
                <View style={styles.list}>
                  {renderRecipeRows(recipes, 'main')}
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
  controlsPrimaryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  searchRow: {
    paddingTop: 4,
  },
  searchInput: {
    backgroundColor: theme.colors.surfaceSubtle,
  },
  libraryHeader: {
    gap: theme.spacing.sm,
  },
  libraryHeaderTop: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  libraryHeaderCopy: {
    gap: 4,
    flex: 1,
    minWidth: 180,
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
  inlineActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    justifyContent: 'flex-start',
  },
  toolbarAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    borderRadius: theme.radius.pill,
    paddingHorizontal: 2,
    paddingVertical: 4,
    backgroundColor: 'transparent',
  },
  quickActionPressed: {
    opacity: 0.72,
  },
  toolbarActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
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
    paddingTop: 2,
  },
  supportSurface: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderRadius: theme.radius.md,
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
