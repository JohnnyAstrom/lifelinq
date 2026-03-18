import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppCard, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type RecipeListItem = {
  recipeId: string;
  name: string;
  ingredientCount: number;
  duplicateNameCount: number;
  createdLabel: string;
  archivedAt: string | null;
};

type Strings = {
  title: string;
  subtitle?: string;
  activeTab: string;
  archivedTab: string;
  newRecipe: string;
  importRecipe: string;
  loadingRecipes: string;
  noRecipes: string;
  noRecipesHint?: string;
  noArchivedRecipes: string;
  noArchivedRecipesHint?: string;
  savedRecipeLabel: string;
  archivedRecipeLabel: string;
  createdLabel: string;
  duplicateNameHint: (count: number) => string;
  recipeCountLabel: (count: number) => string;
  archivedCountLabel: (count: number) => string;
};

type Props = {
  recipes: RecipeListItem[];
  listMode: 'active' | 'archived';
  activeCount: number;
  archivedCount: number;
  isLoading: boolean;
  error: string | null;
  onShowActive: () => void;
  onShowArchived: () => void;
  onOpenRecipe: (recipeId: string) => void;
  onCreateRecipe: () => void;
  onImportRecipe: () => void;
  strings: Strings;
};

export function MealsRecipesView({
  recipes,
  listMode,
  activeCount,
  archivedCount,
  isLoading,
  error,
  onShowActive,
  onShowArchived,
  onOpenRecipe,
  onCreateRecipe,
  onImportRecipe,
  strings,
}: Props) {
  return (
    <View style={styles.layout}>
      <AppCard style={styles.workspaceCard}>
        <View style={styles.toolbarRow}>
          <View style={styles.modeSwitchRow}>
            <Pressable
              onPress={onShowActive}
              style={({ pressed }) => [
                styles.modeTab,
                listMode === 'active' ? styles.modeTabActive : null,
                pressed ? styles.modeTabPressed : null,
              ]}
            >
              <Text style={[styles.modeTabText, listMode === 'active' ? styles.modeTabTextActive : null]}>
                {strings.activeTab} ({activeCount})
              </Text>
            </Pressable>
            <Pressable
              onPress={onShowArchived}
              style={({ pressed }) => [
                styles.modeTab,
                listMode === 'archived' ? styles.modeTabActive : null,
                pressed ? styles.modeTabPressed : null,
              ]}
            >
              <Text style={[styles.modeTabText, listMode === 'archived' ? styles.modeTabTextActive : null]}>
                {strings.archivedTab} ({archivedCount})
              </Text>
            </Pressable>
          </View>
          <View style={styles.headerActions}>
            <Pressable
              onPress={onImportRecipe}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.toolbarAction,
                pressed ? styles.quickActionPressed : null,
              ]}
            >
              <Ionicons name="download-outline" size={16} color={theme.colors.textSecondary} />
              <Text style={styles.toolbarActionText}>{strings.importRecipe}</Text>
            </Pressable>
            <Pressable
              onPress={onCreateRecipe}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.toolbarAction,
                styles.toolbarActionPrimary,
                pressed ? styles.quickActionPressed : null,
              ]}
            >
              <Ionicons name="add" size={16} color={theme.colors.feature.meals} />
              <Text style={styles.toolbarActionPrimaryText}>{strings.newRecipe}</Text>
            </Pressable>
          </View>
        </View>

        {isLoading || error || recipes.length === 0 ? (
          <View style={styles.workspaceBody}>
            {isLoading ? <Subtle>{strings.loadingRecipes}</Subtle> : null}
            {error ? <Text style={styles.error}>{error}</Text> : null}
            {!isLoading && !error ? (
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
            ) : null}
          </View>
        ) : (
          <View style={styles.list}>
            {recipes.map((recipe, index) => {
              const ingredientLabel = recipe.ingredientCount === 1
                ? '1 ingredient'
                : `${recipe.ingredientCount} ingredients`;
              const showIdentityBadge = !!recipe.archivedAt;
              const showMetaTop = showIdentityBadge || recipe.duplicateNameCount > 1;
              return (
                <Pressable
                  key={recipe.recipeId}
                  onPress={() => onOpenRecipe(recipe.recipeId)}
                  style={({ pressed }) => [
                    styles.row,
                    index > 0 ? styles.rowBorder : null,
                    pressed ? styles.rowPressed : null,
                  ]}
                >
                  <View style={styles.rowCopy}>
                    {showMetaTop ? (
                      <View style={styles.rowMetaTop}>
                        {showIdentityBadge ? (
                          <View style={styles.identityBadge}>
                            <Text style={styles.identityBadgeText}>{strings.archivedRecipeLabel}</Text>
                          </View>
                        ) : null}
                        {recipe.duplicateNameCount > 1 ? (
                          <Text style={styles.duplicateHint}>
                            {strings.duplicateNameHint(recipe.duplicateNameCount)}
                          </Text>
                        ) : null}
                      </View>
                    ) : null}
                    <Text style={styles.rowTitle}>{recipe.name}</Text>
                    <Text style={styles.rowMeta}>
                      {ingredientLabel} · {strings.createdLabel} {recipe.createdLabel}
                    </Text>
                  </View>
                  <Ionicons name="chevron-forward" size={18} color={theme.colors.textSecondary} />
                </Pressable>
              );
            })}
          </View>
        )}
      </AppCard>
    </View>
  );
}

const styles = StyleSheet.create({
  layout: {
    gap: 0,
  },
  workspaceCard: {
    paddingTop: theme.spacing.xs,
    paddingBottom: 0,
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  toolbarAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: 6,
  },
  toolbarActionPrimary: {
    backgroundColor: theme.colors.accentSoft,
  },
  quickActionPressed: {
    opacity: 0.82,
  },
  toolbarActionText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  toolbarActionPrimaryText: {
    ...textStyles.subtle,
    color: theme.colors.feature.meals,
    fontWeight: '700',
  },
  toolbarRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
  },
  modeSwitchRow: {
    flexDirection: 'row',
    gap: 4,
    padding: 3,
    borderRadius: theme.radius.pill,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceSubtle,
  },
  modeTab: {
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 5,
  },
  modeTabActive: {
    backgroundColor: theme.colors.surface,
  },
  modeTabPressed: {
    opacity: 0.78,
  },
  modeTabText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  modeTabTextActive: {
    color: theme.colors.textPrimary,
  },
  workspaceBody: {
    paddingHorizontal: theme.spacing.sm,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    gap: theme.spacing.xs,
  },
  list: {
    gap: 0,
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
    gap: 2,
  },
  rowMetaTop: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    marginBottom: 2,
  },
  identityBadge: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: 2,
    backgroundColor: theme.colors.surfaceAlt,
  },
  identityBadgeText: {
    ...textStyles.subtle,
    color: theme.colors.text,
    fontWeight: '600',
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
  },
  emptyState: {
    gap: theme.spacing.xs,
  },
  emptyStateTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
