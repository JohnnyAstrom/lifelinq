import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppButton, AppCard, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type RecipeListItem = {
  recipeId: string;
  name: string;
  ingredientCount: number;
};

type Strings = {
  title: string;
  newRecipe: string;
  loadingRecipes: string;
  noRecipes: string;
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
  return (
    <View style={styles.layout}>
      <View style={styles.header}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <AppButton title={strings.newRecipe} onPress={onCreateRecipe} accentKey="meals" />
      </View>

      {isLoading ? <Subtle>{strings.loadingRecipes}</Subtle> : null}
      {error ? <Text style={styles.error}>{error}</Text> : null}

      {!isLoading && !error && recipes.length === 0 ? (
        <AppCard>
          <Subtle>{strings.noRecipes}</Subtle>
        </AppCard>
      ) : null}

      {!isLoading && recipes.length > 0 ? (
        <AppCard style={styles.listCard}>
          <View style={styles.list}>
            {recipes.map((recipe) => {
              const ingredientLabel = recipe.ingredientCount === 1
                ? '1 ingredient'
                : `${recipe.ingredientCount} ingredients`;
              return (
                <Pressable
                  key={recipe.recipeId}
                  onPress={() => onOpenRecipe(recipe.recipeId)}
                  style={({ pressed }) => [
                    styles.row,
                    pressed ? styles.rowPressed : null,
                  ]}
                >
                  <View style={styles.rowCopy}>
                    <Text style={styles.rowTitle}>{recipe.name}</Text>
                    <Text style={styles.rowMeta}>{ingredientLabel}</Text>
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
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  listCard: {
    paddingVertical: theme.spacing.xs,
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
  rowPressed: {
    opacity: 0.8,
  },
  rowCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  rowTitle: {
    ...textStyles.body,
    fontWeight: '600',
  },
  rowMeta: {
    ...textStyles.subtle,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
