import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppButton, AppCard, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type RecipeListItem = {
  recipeId: string;
  name: string;
  ingredientCount: number;
  duplicateNameCount: number;
  createdLabel: string;
};

type Strings = {
  title: string;
  subtitle: string;
  newRecipe: string;
  loadingRecipes: string;
  noRecipes: string;
  noRecipesHint: string;
  savedRecipeLabel: string;
  createdLabel: string;
  duplicateNameHint: (count: number) => string;
  recipeCountLabel: (count: number) => string;
};

type Props = {
  recipes: RecipeListItem[];
  isLoading: boolean;
  error: string | null;
  onOpenRecipe: (recipeId: string) => void;
  onCreateRecipe: () => void;
  strings: Strings;
};

export function MealsRecipesView({
  recipes,
  isLoading,
  error,
  onOpenRecipe,
  onCreateRecipe,
  strings,
}: Props) {
  const recipeCount = recipes.length;

  return (
    <View style={styles.layout}>
      <View style={styles.header}>
        <View style={styles.headerCopy}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
        </View>
        <AppButton title={strings.newRecipe} onPress={onCreateRecipe} accentKey="meals" />
      </View>

      {isLoading ? <Subtle>{strings.loadingRecipes}</Subtle> : null}
      {error ? <Text style={styles.error}>{error}</Text> : null}

      {!isLoading && !error && recipes.length === 0 ? (
        <AppCard>
          <View style={styles.emptyState}>
            <Text style={styles.emptyStateTitle}>{strings.noRecipes}</Text>
            <Subtle>{strings.noRecipesHint}</Subtle>
          </View>
        </AppCard>
      ) : null}

      {!isLoading && recipes.length > 0 ? (
        <AppCard style={styles.listCard}>
          <View style={styles.listSummaryRow}>
            <Subtle>{strings.recipeCountLabel(recipeCount)}</Subtle>
          </View>
          <View style={styles.list}>
            {recipes.map((recipe, index) => {
              const ingredientLabel = recipe.ingredientCount === 1
                ? '1 ingredient'
                : `${recipe.ingredientCount} ingredients`;
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
                    <View style={styles.rowMetaTop}>
                      <View style={styles.identityBadge}>
                        <Text style={styles.identityBadgeText}>{strings.savedRecipeLabel}</Text>
                      </View>
                      {recipe.duplicateNameCount > 1 ? (
                        <Text style={styles.duplicateHint}>
                          {strings.duplicateNameHint(recipe.duplicateNameCount)}
                        </Text>
                      ) : null}
                    </View>
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
        </AppCard>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  layout: {
    gap: theme.spacing.sm,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  headerCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  listCard: {
    paddingVertical: theme.spacing.xs,
  },
  listSummaryRow: {
    paddingHorizontal: theme.spacing.sm,
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
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
