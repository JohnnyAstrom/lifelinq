import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppCard, AppSegmentedControl, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type RecipeListItem = {
  recipeId: string;
  name: string;
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
  loadingRecipes: string;
  noRecipes: string;
  noRecipesHint?: string;
  noArchivedRecipes: string;
  noArchivedRecipesHint?: string;
  savedRecipeLabel: string;
  archivedRecipeLabel: string;
  duplicateNameHint: (count: number) => string;
  similarNameHint: string;
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
  const shouldShowContentCard = isLoading || error != null || recipes.length > 0;

  return (
    <View style={styles.layout}>
      <AppCard style={styles.controlsCard}>
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
        <View style={styles.controlsActionsRow}>
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
            <Ionicons name="download-outline" size={16} color={theme.colors.textSecondary} />
            <Text style={styles.toolbarActionText}>{strings.importRecipe}</Text>
          </Pressable>
        </View>
      </AppCard>

      {shouldShowContentCard ? (
        <AppCard style={styles.contentCard}>
          <View style={styles.workspaceBody}>
            {isLoading || error ? (
              <>
                {isLoading ? <Subtle>{strings.loadingRecipes}</Subtle> : null}
                {error ? <Text style={styles.error}>{error}</Text> : null}
              </>
            ) : (
              <View style={styles.list}>
                {recipes.map((recipe, index) => {
                  const ingredientLabel = recipe.ingredientCount === 1
                    ? '1 ingredient'
                    : `${recipe.ingredientCount} ingredients`;
                  const hasCollisionHint = recipe.duplicateNameCount > 1 || recipe.similarNameCount > 1;
                  const metaLine = recipe.identitySummary && !hasCollisionHint
                    ? `${ingredientLabel} · ${recipe.identitySummary}`
                    : ingredientLabel;
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
                        {hasCollisionHint && recipe.identitySummary ? (
                          <Text style={styles.rowIdentity} numberOfLines={1}>
                            {recipe.identitySummary}
                          </Text>
                        ) : null}
                        <Text style={styles.rowMeta} numberOfLines={1}>
                          {metaLine}
                        </Text>
                      </View>
                      <Ionicons name="chevron-forward" size={18} color={theme.colors.textSecondary} />
                    </Pressable>
                  );
                })}
              </View>
            )}
          </View>
        </AppCard>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  layout: {
    gap: theme.spacing.sm,
  },
  controlsCard: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
  },
  controlsPrimaryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  controlsActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    paddingTop: theme.spacing.xs,
    marginTop: 2,
  },
  toolbarAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    borderRadius: theme.radius.pill,
    paddingHorizontal: 4,
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
  contentCard: {
    paddingVertical: theme.spacing.xs,
  },
  workspaceBody: {
    gap: theme.spacing.xs,
  },
  list: {
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
  rowIdentity: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
