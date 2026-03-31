import { fetchJson, type ApiClientOptions } from './client';

export type PlannedMealResponse = {
  dayOfWeek: number;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  recipeId: string | null;
  mealTitle: string;
  recipeTitle: string | null;
  shoppingHandledAt: string | null;
  shoppingListId: string | null;
};

export type RecentPlannedMealResponse = {
  year: number;
  isoWeek: number;
  dayOfWeek: number;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  mealTitle: string;
  recipeId: string | null;
  recipeTitle: string | null;
};

export type MealChoiceCandidateResponse = {
  family: 'recent' | 'familiar' | 'fallback' | 'make_soon';
  mealIdentityKey: string;
  mealIdentityKind: 'recipe' | 'title_only';
  title: string;
  recipeId: string | null;
  lastPlannedDate: string;
  totalOccurrences: number;
  recent: boolean;
  frequent: boolean;
  familiar: boolean;
  fallback: boolean;
  slotFit: boolean;
  preferenceFit: boolean;
  deprioritized: boolean;
  makeSoon: boolean;
  surfacedBecause: string;
};

export type PlanningChoiceSupportResponse = {
  scenario: 'slot' | 'tonight' | 'week_start' | 'recipe_context';
  referenceDate: string;
  year: number | null;
  isoWeek: number | null;
  dayOfWeek: number | null;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' | null;
  recipeId: string | null;
  recentCandidates: MealChoiceCandidateResponse[];
  familiarCandidates: MealChoiceCandidateResponse[];
  fallbackCandidates: MealChoiceCandidateResponse[];
  makeSoonCandidates: MealChoiceCandidateResponse[];
};

export type WeekPlanResponse = {
  weekPlanId: string | null;
  year: number;
  isoWeek: number;
  createdAt: string | null;
  hasReviewableWeekShopping: boolean;
  meals: PlannedMealResponse[];
};

export type AddMealRequest = {
  mealTitle?: string | null;
  recipeId?: string | null;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  targetShoppingListId?: string | null;
  selectedIngredientPositions?: number[] | null;
};

export type AddMealResponse = {
  weekPlanId: string;
  year: number;
  isoWeek: number;
  meal: PlannedMealResponse;
};

export async function getWeekPlan(
  year: number,
  isoWeek: number,
  clientOptions: ApiClientOptions = {}
): Promise<WeekPlanResponse> {
  return fetchJson<WeekPlanResponse>(
    `/meals/weeks/${year}/${isoWeek}`,
    {},
    clientOptions
  );
}

export async function addOrReplaceMeal(
  year: number,
  isoWeek: number,
  dayOfWeek: number,
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
  payload: AddMealRequest,
  clientOptions: ApiClientOptions = {}
): Promise<AddMealResponse> {
  return fetchJson<AddMealResponse>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}/meals/${mealType}`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function removeMeal(
  year: number,
  isoWeek: number,
  dayOfWeek: number,
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
  clientOptions: ApiClientOptions = {}
): Promise<void> {
  return fetchJson<void>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}/meals/${mealType}`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}

export type IngredientRequest = {
  name: string;
  rawText?: string | null;
  quantity?: number | null;
  unit?: string | null;
  position: number;
};

export type CreateOrUpdateRecipeRequest = {
  name: string;
  sourceName?: string | null;
  sourceUrl?: string | null;
  originKind?: string | null;
  servings?: string | null;
  shortNote?: string | null;
  instructions?: string | null;
  savedInRecipes?: boolean | null;
  ingredients: IngredientRequest[];
};

export type IngredientResponse = {
  id: string;
  name: string;
  rawText: string | null;
  quantity: number | null;
  unit: string | null;
  position: number;
};

export type RecipeResponse = {
  recipeId: string;
  groupId: string;
  name: string;
  sourceName: string | null;
  sourceUrl: string | null;
  originKind: string;
  servings: string | null;
  makeSoonAt: string | null;
  shortNote: string | null;
  instructions: string | null;
  createdAt: string;
  updatedAt: string;
  archivedAt: string | null;
  savedInRecipes: boolean;
  deleteEligible: boolean;
  deleteBlockedReason: string | null;
  ingredients: IngredientResponse[];
};

export type RecipeImportDraftIngredientResponse = {
  name: string;
  rawText: string | null;
  quantity: number | null;
  unit: string | null;
  position: number;
};

export type RecipeImportDraftResponse = {
  name: string;
  sourceName: string | null;
  sourceUrl: string;
  originKind: string;
  servings: string | null;
  shortNote: string | null;
  instructions: string | null;
  ingredients: RecipeImportDraftIngredientResponse[];
};

export type RecipeSourceSummaryResponse = {
  sourceName: string | null;
  sourceUrl: string | null;
};

export type RecipeProvenanceResponse = {
  originKind: string | null;
  referenceUrl: string | null;
};

export type RecipeLifecycleResponse = {
  state: string;
  deleteEligible: boolean;
  deleteBlockedReason: string | null;
};

export type RecipeDraftResponse = {
  draftId: string;
  groupId: string;
  state: string;
  name: string | null;
  source: RecipeSourceSummaryResponse;
  provenance: RecipeProvenanceResponse;
  servings: string | null;
  shortNote: string | null;
  instructions: string | null;
  createdAt: string;
  updatedAt: string;
  ingredients: IngredientResponse[];
};

export type RecipeIdentitySummaryResponse = {
  recipeId: string;
  name: string;
  source: RecipeSourceSummaryResponse;
  lifecycle: RecipeLifecycleResponse;
};

export type RecipeDuplicateAssessmentResponse = {
  attentionRequired: boolean;
  matchType: string | null;
  reason: string | null;
  matchingRecipe: RecipeIdentitySummaryResponse | null;
};

export type RecipeDetailResponse = {
  recipeId: string;
  groupId: string;
  name: string;
  source: RecipeSourceSummaryResponse;
  provenance: RecipeProvenanceResponse;
  lifecycle: RecipeLifecycleResponse;
  servings: string | null;
  makeSoonAt: string | null;
  shortNote: string | null;
  instructions: string | null;
  createdAt: string;
  updatedAt: string;
  savedInRecipes: boolean;
  ingredients: IngredientResponse[];
};

export type RecipeLibraryItemResponse = {
  recipeId: string;
  name: string;
  source: RecipeSourceSummaryResponse;
  lifecycle: RecipeLifecycleResponse;
  makeSoonAt: string | null;
  updatedAt: string;
  ingredientCount: number;
};

export type RecipeUsageSummaryResponse = {
  recipeId: string;
  recipeTitle: string;
  lastUsedDate: string;
  totalUses: number;
  recentUses: number;
  distinctWeeks: number;
  frequent: boolean;
  familiar: boolean;
  makeSoon: boolean;
  preferenceFit: boolean;
  deprioritized: boolean;
};

export type ShoppingLinkReferenceResponse = {
  shoppingListId: string | null;
  shoppingListName: string | null;
  shoppingHandledAt: string | null;
  status: 'not_linked' | 'linked' | 'missing_list';
};

export type MealIngredientNeedResponse = {
  ingredientId: string;
  position: number;
  ingredientName: string;
  normalizedShoppingName: string;
  rawText: string | null;
  quantity: number | null;
  unitName: string | null;
};

export type IngredientCoverageResponse = {
  need: MealIngredientNeedResponse;
  coverageState: 'covered' | 'partially_covered' | 'missing' | 'unknown';
  shoppingState: 'none' | 'to_buy' | 'bought' | 'mixed' | 'unknown';
  matchingItemCount: number;
  coveredQuantity: number | null;
  uncoveredQuantity: number | null;
  uncertaintyReason: string | null;
};

export type ShoppingDeltaResponse = {
  unresolvedIngredientCount: number;
  partialIngredientCount: number;
  missingIngredientCount: number;
  unknownIngredientCount: number;
  unresolvedIngredients: IngredientCoverageResponse[];
};

export type MealReadinessResponse = {
  state: 'needs_shopping' | 'partially_ready' | 'ready_from_shopping_view' | 'readiness_unclear';
  coveredIngredientCount: number;
  partiallyCoveredIngredientCount: number;
  missingIngredientCount: number;
  unknownIngredientCount: number;
  boughtIngredientCount: number;
  toBuyIngredientCount: number;
};

export type MealShoppingProjectionResponse = {
  year: number;
  isoWeek: number;
  dayOfWeek: number;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  mealTitle: string;
  recipeId: string | null;
  recipeTitle: string | null;
  recipeBacked: boolean;
  assessedShoppingListId: string | null;
  assessedShoppingListName: string | null;
  shoppingLink: ShoppingLinkReferenceResponse;
  readiness: MealReadinessResponse;
  delta: ShoppingDeltaResponse;
  ingredientCoverage: IngredientCoverageResponse[];
};

export type ContributorMealReferenceResponse = {
  dayOfWeek: number;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  mealTitle: string;
};

export type AggregatedIngredientNeedResponse = {
  lineId: string;
  ingredientName: string;
  normalizedShoppingName: string;
  totalQuantity: number | null;
  unitName: string | null;
  quantityConfidence: 'exact' | 'uncertain' | 'none';
  contributors: ContributorMealReferenceResponse[];
};

export type AggregatedIngredientComparisonResponse = {
  need: AggregatedIngredientNeedResponse;
  comparisonState: 'already_on_list' | 'add_to_list';
  quantityOnList: number | null;
  remainingQuantity: number | null;
};

export type WeekShoppingReviewLinkResponse = {
  shoppingListId: string;
  shoppingListName: string | null;
  reviewedAt: string;
};

export type WeekShoppingReviewResponse = {
  weekPlanId: string | null;
  year: number;
  isoWeek: number;
  assessedShoppingListId: string | null;
  assessedShoppingListName: string | null;
  reviewLink: WeekShoppingReviewLinkResponse | null;
  ingredients: AggregatedIngredientComparisonResponse[];
};

export type AddWeekShoppingReviewLinesRequest = {
  shoppingListId: string;
  selectedLineIds: string[];
};

export async function createRecipe(
  payload: CreateOrUpdateRecipeRequest,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    '/meals/recipes',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function listRecentPlannedMeals(
  clientOptions: ApiClientOptions = {}
): Promise<RecentPlannedMealResponse[]> {
  return fetchJson<RecentPlannedMealResponse[]>(
    '/meals/recently-planned',
    {},
    clientOptions
  );
}

export async function getSlotPlanningChoiceSupport(
  params: {
    year: number;
    isoWeek: number;
    dayOfWeek: number;
    mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  },
  clientOptions: ApiClientOptions = {}
): Promise<PlanningChoiceSupportResponse> {
  const query = new URLSearchParams({
    year: String(params.year),
    isoWeek: String(params.isoWeek),
    dayOfWeek: String(params.dayOfWeek),
    mealType: params.mealType,
  });
  return fetchJson<PlanningChoiceSupportResponse>(
    `/meals/choice-support/slot?${query.toString()}`,
    {},
    clientOptions
  );
}

export async function getRecipeChoiceSupportMemory(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeUsageSummaryResponse> {
  return fetchJson<RecipeUsageSummaryResponse>(
    `/meals/choice-support/recipes/${recipeId}/memory`,
    {},
    clientOptions
  );
}

export async function getMealShoppingProjection(
  year: number,
  isoWeek: number,
  dayOfWeek: number,
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
  params: {
    shoppingListId?: string | null;
  } = {},
  clientOptions: ApiClientOptions = {}
): Promise<MealShoppingProjectionResponse> {
  const query = new URLSearchParams();
  if (params.shoppingListId) {
    query.set('shoppingListId', params.shoppingListId);
  }
  const querySuffix = query.size > 0 ? `?${query.toString()}` : '';
  return fetchJson<MealShoppingProjectionResponse>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}/meals/${mealType}/shopping-impact${querySuffix}`,
    {},
    clientOptions
  );
}

export async function getWeekShoppingReview(
  year: number,
  isoWeek: number,
  params: {
    shoppingListId?: string | null;
  } = {},
  clientOptions: ApiClientOptions = {}
): Promise<WeekShoppingReviewResponse> {
  const query = new URLSearchParams();
  if (params.shoppingListId) {
    query.set('shoppingListId', params.shoppingListId);
  }
  const querySuffix = query.size > 0 ? `?${query.toString()}` : '';
  return fetchJson<WeekShoppingReviewResponse>(
    `/meals/weeks/${year}/${isoWeek}/shopping-review${querySuffix}`,
    {},
    clientOptions
  );
}

export async function addWeekShoppingReviewLines(
  year: number,
  isoWeek: number,
  payload: AddWeekShoppingReviewLinesRequest,
  clientOptions: ApiClientOptions = {}
): Promise<WeekShoppingReviewResponse> {
  return fetchJson<WeekShoppingReviewResponse>(
    `/meals/weeks/${year}/${isoWeek}/shopping-review/add`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function listRecipes(
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse[]> {
  return fetchJson<RecipeResponse[]>(
    '/meals/recipes',
    {},
    clientOptions
  );
}

export async function listArchivedRecipes(
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse[]> {
  return fetchJson<RecipeResponse[]>(
    '/meals/recipes/archived',
    {},
    clientOptions
  );
}

export async function listRecentlyUsedRecipes(
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse[]> {
  return fetchJson<RecipeResponse[]>(
    '/meals/recipes/recently-used',
    {},
    clientOptions
  );
}

export async function getRecipe(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    `/meals/recipes/${recipeId}`,
    {},
    clientOptions
  );
}

export async function updateRecipe(
  recipeId: string,
  payload: CreateOrUpdateRecipeRequest,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    `/meals/recipes/${recipeId}`,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function archiveRecipe(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    `/meals/recipes/${recipeId}/archive`,
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function markRecipeMakeSoon(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    `/meals/recipes/${recipeId}/make-soon`,
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function clearRecipeMakeSoon(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    `/meals/recipes/${recipeId}/make-soon`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}

export async function restoreRecipe(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeResponse> {
  return fetchJson<RecipeResponse>(
    `/meals/recipes/${recipeId}/restore`,
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function deleteRecipe(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<void> {
  return fetchJson<void>(
    `/meals/recipes/${recipeId}`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}

export async function createRecipeImportDraft(
  payload: { url: string },
  clientOptions: ApiClientOptions = {}
): Promise<RecipeImportDraftResponse> {
  return fetchJson<RecipeImportDraftResponse>(
    '/meals/recipes/import-drafts',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function createManualRecipeDraft(
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDraftResponse> {
  return fetchJson<RecipeDraftResponse>(
    '/meals/recipe-drafts/manual',
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function createRecipeDraftFromUrl(
  payload: { url: string },
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDraftResponse> {
  return fetchJson<RecipeDraftResponse>(
    '/meals/recipe-drafts/from-url',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function createRecipeDraftFromText(
  payload: { text: string },
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDraftResponse> {
  return fetchJson<RecipeDraftResponse>(
    '/meals/recipe-drafts/from-text',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export type CreateRecipeDraftFromAssetRequest = {
  assetKind: 'document' | 'image';
  referenceId: string;
  sourceLabel?: string | null;
  originalFilename?: string | null;
  mimeType?: string | null;
};

export type StageRecipeDocumentAssetResponse = {
  assetKind: 'document';
  referenceId: string;
  sourceLabel: string | null;
  originalFilename: string | null;
  mimeType: string | null;
};

export async function stageRecipeDocumentAsset(
  payload: {
    uri: string;
    name: string;
    mimeType?: string | null;
  },
  clientOptions: ApiClientOptions = {}
): Promise<StageRecipeDocumentAssetResponse> {
  const formData = new FormData();
  formData.append(
    'file',
    {
      uri: payload.uri,
      name: payload.name,
      type: payload.mimeType ?? 'application/octet-stream',
    } as any
  );
  return fetchJson<StageRecipeDocumentAssetResponse>(
    '/meals/recipe-assets/documents',
    {
      method: 'POST',
      body: formData,
    },
    clientOptions
  );
}

export async function createRecipeDraftFromAsset(
  payload: CreateRecipeDraftFromAssetRequest,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDraftResponse> {
  return fetchJson<RecipeDraftResponse>(
    '/meals/recipe-drafts/from-asset',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export type UpdateRecipeDraftRequest = {
  name?: string | null;
  sourceName?: string | null;
  sourceUrl?: string | null;
  servings?: string | null;
  shortNote?: string | null;
  instructions?: string | null;
  markReady?: boolean | null;
  ingredients?: IngredientRequest[] | null;
};

export async function getRecipeDraft(
  draftId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDraftResponse> {
  return fetchJson<RecipeDraftResponse>(
    `/meals/recipe-drafts/${draftId}`,
    {},
    clientOptions
  );
}

export async function updateRecipeDraft(
  draftId: string,
  payload: UpdateRecipeDraftRequest,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDraftResponse> {
  return fetchJson<RecipeDraftResponse>(
    `/meals/recipe-drafts/${draftId}`,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function getRecipeDraftDuplicateAssessment(
  draftId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDuplicateAssessmentResponse> {
  return fetchJson<RecipeDuplicateAssessmentResponse>(
    `/meals/recipe-drafts/${draftId}/duplicate-assessment`,
    {},
    clientOptions
  );
}

export async function acceptRecipeDraft(
  draftId: string,
  payload: { allowDuplicate?: boolean | null } = {},
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-drafts/${draftId}/accept`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function listRecipeLibraryItems(
  state: 'active' | 'archived' = 'active',
  clientOptions: ApiClientOptions = {}
): Promise<RecipeLibraryItemResponse[]> {
  return fetchJson<RecipeLibraryItemResponse[]>(
    `/meals/recipe-library/items?state=${state}`,
    {},
    clientOptions
  );
}

export async function listRecentRecipeLibraryItems(
  clientOptions: ApiClientOptions = {}
): Promise<RecipeLibraryItemResponse[]> {
  return fetchJson<RecipeLibraryItemResponse[]>(
    '/meals/recipe-library/recent-items',
    {},
    clientOptions
  );
}

export async function getRecipeDetail(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-details/${recipeId}`,
    {},
    clientOptions
  );
}

export async function updateRecipeDetail(
  recipeId: string,
  payload: CreateOrUpdateRecipeRequest,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-details/${recipeId}`,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function archiveRecipeDetail(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-details/${recipeId}/archive`,
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function restoreRecipeDetail(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-details/${recipeId}/restore`,
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function markRecipeDetailMakeSoon(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-details/${recipeId}/make-soon`,
    {
      method: 'POST',
    },
    clientOptions
  );
}

export async function clearRecipeDetailMakeSoon(
  recipeId: string,
  clientOptions: ApiClientOptions = {}
): Promise<RecipeDetailResponse> {
  return fetchJson<RecipeDetailResponse>(
    `/meals/recipe-details/${recipeId}/make-soon`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}
