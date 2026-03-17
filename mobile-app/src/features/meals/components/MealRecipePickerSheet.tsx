import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type RecipeOption = {
  recipeId: string;
  name: string;
  ingredientCount: number;
};

type Strings = {
  title: string;
  hint: string;
  loadingRecipes: string;
  noRecipes: string;
  close: string;
};

type Props = {
  recipes: RecipeOption[];
  selectedRecipeId: string | null;
  isLoading: boolean;
  error: string | null;
  onSelectRecipe: (recipeId: string) => void;
  onClose: () => void;
  strings: Strings;
};

export function MealRecipePickerSheet({
  recipes,
  selectedRecipeId,
  isLoading,
  error,
  onSelectRecipe,
  onClose,
  strings,
}: Props) {
  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
        </View>

        <View style={styles.body}>
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <Subtle>{strings.hint}</Subtle>

            {isLoading ? <Subtle>{strings.loadingRecipes}</Subtle> : null}
            {error ? <Text style={styles.error}>{error}</Text> : null}
            {!isLoading && !error && recipes.length === 0 ? (
              <Subtle>{strings.noRecipes}</Subtle>
            ) : null}

            {!isLoading && recipes.length > 0 ? (
              <View style={styles.recipeList}>
                {recipes.map((recipe) => {
                  const ingredientLabel = recipe.ingredientCount === 1
                    ? '1 ingredient'
                    : `${recipe.ingredientCount} ingredients`;

                  return (
                    <Pressable
                      key={recipe.recipeId}
                      onPress={() => onSelectRecipe(recipe.recipeId)}
                      style={({ pressed }) => [
                        styles.recipeRow,
                        recipe.recipeId === selectedRecipeId ? styles.recipeRowActive : null,
                        pressed ? styles.recipeRowPressed : null,
                      ]}
                    >
                      <View style={styles.recipeRowCopy}>
                        <Text style={styles.recipeName}>{recipe.name}</Text>
                        <Text style={styles.recipeMeta}>{ingredientLabel}</Text>
                      </View>
                      {recipe.recipeId === selectedRecipeId ? (
                        <Ionicons name="checkmark-circle" size={18} color={theme.colors.feature.meals} />
                      ) : null}
                    </Pressable>
                  );
                })}
              </View>
            ) : null}

            <AppButton
              title={strings.close}
              onPress={onClose}
              variant="ghost"
              fullWidth
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
  recipeList: {
    gap: theme.spacing.xs,
  },
  recipeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
  },
  recipeRowActive: {
    borderColor: theme.colors.feature.meals,
    backgroundColor: theme.colors.surfaceAlt,
  },
  recipeRowPressed: {
    opacity: 0.8,
  },
  recipeRowCopy: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  recipeName: {
    ...textStyles.body,
    fontWeight: '600',
  },
  recipeMeta: {
    ...textStyles.subtle,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
