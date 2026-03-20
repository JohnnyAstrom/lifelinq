import type { RecipeResponse } from '../api/mealsApi';

export type RecipeDuplicateMatchType = 'source-url' | 'name-and-source';

export type RecipeDuplicateCandidate = {
  recipeId: string;
  name: string;
  sourceName: string | null;
  sourceUrl: string | null;
  archivedAt: string | null;
  matchType: RecipeDuplicateMatchType;
};

type FindRecipeDuplicateParams = {
  recipes: RecipeResponse[];
  name: string;
  sourceName?: string | null;
  sourceUrl?: string | null;
  excludeRecipeId?: string | null;
};

function normalizeText(value?: string | null) {
  if (!value) {
    return null;
  }
  const normalized = value
    .trim()
    .toLocaleLowerCase()
    .replace(/\s+/g, ' ');
  return normalized.length > 0 ? normalized : null;
}

function normalizeSourceUrl(value?: string | null) {
  const normalized = normalizeText(value);
  if (!normalized) {
    return null;
  }
  return normalized.replace(/\/+$/, '');
}

export function findLikelyRecipeDuplicate({
  recipes,
  name,
  sourceName,
  sourceUrl,
  excludeRecipeId,
}: FindRecipeDuplicateParams): RecipeDuplicateCandidate | null {
  const normalizedSourceUrl = normalizeSourceUrl(sourceUrl);
  if (normalizedSourceUrl) {
    const exactSourceMatch = recipes.find((recipe) => (
      recipe.recipeId !== excludeRecipeId
      && normalizeSourceUrl(recipe.sourceUrl) === normalizedSourceUrl
    ));
    if (exactSourceMatch) {
      return {
        recipeId: exactSourceMatch.recipeId,
        name: exactSourceMatch.name,
        sourceName: exactSourceMatch.sourceName ?? null,
        sourceUrl: exactSourceMatch.sourceUrl ?? null,
        archivedAt: exactSourceMatch.archivedAt,
        matchType: 'source-url',
      };
    }
  }

  const normalizedName = normalizeText(name);
  const normalizedSourceName = normalizeText(sourceName);
  if (normalizedName && normalizedSourceName) {
    const matchingNameAndSource = recipes.find((recipe) => (
      recipe.recipeId !== excludeRecipeId
      && normalizeText(recipe.name) === normalizedName
      && normalizeText(recipe.sourceName) === normalizedSourceName
    ));
    if (matchingNameAndSource) {
      return {
        recipeId: matchingNameAndSource.recipeId,
        name: matchingNameAndSource.name,
        sourceName: matchingNameAndSource.sourceName ?? null,
        sourceUrl: matchingNameAndSource.sourceUrl ?? null,
        archivedAt: matchingNameAndSource.archivedAt,
        matchType: 'name-and-source',
      };
    }
  }

  return null;
}
