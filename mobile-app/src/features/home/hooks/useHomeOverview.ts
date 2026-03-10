import { useMemo } from 'react';
import { useGroupMembers } from '../../group/hooks/useGroupMembers';
import { useWeekPlan } from '../../meals/hooks/useWeekPlan';
import { useShoppingLists } from '../../shopping/hooks/useShoppingLists';
import { useDocuments } from '../../documents/hooks/useDocuments';
import { useActiveSettlementPeriod } from '../../economy/hooks/useActiveSettlementPeriod';
import { useTodos } from '../../todo/hooks/useTodos';
import {
  buildQuickActions,
  buildTodaySummary,
  createFeatureModules,
  formatMemberSubtitle,
  type HomeFeatureModule,
  type HomeQuickAction,
  type TodaySummary,
} from '../utils/homeOverview';
import { getIsoDayOfWeek, getIsoWeekInfo, parseApiDate } from '../utils/date';

type UseHomeOverviewArgs = {
  token: string;
  spaceName?: string;
  canSwitchSpaces: boolean;
  onContextInvalidated: () => void;
  onCreateTodo: () => void;
  onCreateShopping: () => void;
  onMeals: () => void;
  onDocuments: () => void;
  onEconomy: () => void;
};

type UseHomeOverviewResult = {
  place: {
    title: string;
    subtitle?: string;
    canSwitch: boolean;
  };
  today: TodaySummary;
  quickActions: HomeQuickAction[];
  modules: HomeFeatureModule[];
  loading: boolean;
};

export function useHomeOverview({
  token,
  spaceName,
  canSwitchSpaces,
  onContextInvalidated,
  onCreateTodo,
  onCreateShopping,
  onMeals,
  onDocuments,
  onEconomy,
}: UseHomeOverviewArgs): UseHomeOverviewResult {
  const todayDate = new Date();
  const isoWeek = getIsoWeekInfo(todayDate);
  const isoDayOfWeek = getIsoDayOfWeek(todayDate);

  const todos = useTodos(token, 'OPEN', undefined, {
    onContextInvalidated,
  });
  const members = useGroupMembers(token);
  const shopping = useShoppingLists(token);
  const documents = useDocuments(token);
  const meals = useWeekPlan(token, isoWeek.year, isoWeek.isoWeek);
  const economy = useActiveSettlementPeriod(token);

  const todayItems = useMemo(
    () => todos.items.filter((item) => {
      if (!item.dueDate) {
        return false;
      }
      const due = parseApiDate(item.dueDate);
      return due ? due.toDateString() === todayDate.toDateString() : false;
    }),
    [todayDate, todos.items]
  );

  const todayMeals = useMemo(
    () => meals.data?.meals.filter((meal) => meal.dayOfWeek === isoDayOfWeek) ?? [],
    [isoDayOfWeek, meals.data]
  );

  const place = useMemo(() => {
    const memberCount = members.items.length;
    return {
      title: spaceName?.trim() || 'My place',
      subtitle: formatMemberSubtitle(memberCount, members.loading),
      canSwitch: canSwitchSpaces,
    };
  }, [canSwitchSpaces, members.items.length, members.loading, spaceName]);

  const today = useMemo(
    () => buildTodaySummary(todayItems),
    [todayItems]
  );

  const quickActions = useMemo(
    () => buildQuickActions({
      onCreateTodo,
      onCreateShopping,
    }),
    [onCreateShopping, onCreateTodo]
  );

  const modules = useMemo(
    () => createFeatureModules({
      todayTodoCount: todayItems.length,
      todayMealCount: todayMeals.length,
      shoppingListCount: shopping.lists.length,
      documentCount: documents.items.length,
      participantCount: economy.period?.participantUserIds.length ?? 0,
      hasOpenSettlement: economy.period?.status === 'OPEN',
      loading: {
        todos: todos.loading,
        meals: meals.loading,
        shopping: shopping.loading,
        documents: documents.loading,
        economy: economy.loading,
      },
      onPress: {
        meals: onMeals,
        todos: onCreateTodo,
        shopping: onCreateShopping,
        documents: onDocuments,
        economy: onEconomy,
      },
    }),
    [
      documents.items.length,
      documents.loading,
      economy.loading,
      economy.period?.participantUserIds.length,
      economy.period?.status,
      meals.loading,
      onCreateShopping,
      onCreateTodo,
      onDocuments,
      onEconomy,
      onMeals,
      shopping.lists.length,
      shopping.loading,
      todayItems.length,
      todayMeals.length,
      todos.loading,
    ]
  );

  return {
    place,
    today,
    quickActions,
    modules,
    loading: todos.loading || members.loading || shopping.loading || documents.loading || meals.loading || economy.loading,
  };
}
