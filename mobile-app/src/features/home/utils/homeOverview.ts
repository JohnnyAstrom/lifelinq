import { iconBackground, theme } from '../../../shared/ui/theme';
import { Ionicons } from '@expo/vector-icons';

export type HomeQuickAction = {
  id: string;
  label: string;
  icon: keyof typeof Ionicons.glyphMap;
  onPress: () => void;
};

export type TodaySummary = {
  title: string;
  subtitle: string;
  itemCountLabel: string;
  preview: string[];
  emptyTitle: string;
  emptyBody: string;
};

export type HomeFeatureModule = {
  id: string;
  title: string;
  statusText: string;
  icon: keyof typeof Ionicons.glyphMap;
  accentColor: string;
  accentSoft: string;
  onPress: () => void;
};

type CreateFeatureModulesArgs = {
  todayTodoCount: number;
  todayMealCount: number;
  shoppingListCount: number;
  documentCount: number;
  participantCount: number;
  hasOpenSettlement: boolean;
  loading: {
    todos: boolean;
    meals: boolean;
    shopping: boolean;
    documents: boolean;
    economy: boolean;
  };
  onPress: {
    meals: () => void;
    todos: () => void;
    shopping: () => void;
    documents: () => void;
    economy: () => void;
  };
};

export function formatMemberSubtitle(memberCount: number, loading: boolean): string | undefined {
  if (loading && memberCount === 0) {
    return undefined;
  }
  if (memberCount <= 1) {
    return 'Just you here';
  }
  return `${memberCount} people here`;
}

export function buildTodaySummary(items: Array<{ text: string }>): TodaySummary {
  const itemCount = items.length;

  if (itemCount === 0) {
    return {
      title: 'Today',
      subtitle: 'A calm overview of what needs attention.',
      itemCountLabel: 'Nothing due today',
      preview: [],
      emptyTitle: 'Nothing scheduled yet',
      emptyBody: 'Add a dated todo and it will appear here.',
    };
  }

  return {
    title: 'Today',
    subtitle: 'A quick look at what needs attention today.',
    itemCountLabel: itemCount === 1 ? '1 thing due today' : `${itemCount} things due today`,
    preview: items.slice(0, 3).map((item) => item.text),
    emptyTitle: 'Nothing scheduled yet',
    emptyBody: 'Add a dated todo and it will appear here.',
  };
}

export function buildQuickActions(args: {
  onCreateTodo: () => void;
  onCreateShopping: () => void;
}): HomeQuickAction[] {
  return [
    {
      id: 'todo-create',
      label: 'New todo',
      icon: 'checkmark-circle-outline',
      onPress: args.onCreateTodo,
    },
    {
      id: 'shopping-create',
      label: 'New shopping list',
      icon: 'basket-outline',
      onPress: args.onCreateShopping,
    },
  ];
}

export function createFeatureModules(args: CreateFeatureModulesArgs): HomeFeatureModule[] {
  return [
    {
      id: 'meals',
      title: 'Meals',
      statusText: args.loading.meals
        ? 'Checking this week plan'
        : args.todayMealCount > 0
          ? args.todayMealCount === 1 ? '1 meal planned today' : `${args.todayMealCount} meals planned today`
          : 'Nothing planned today',
      icon: 'restaurant-outline',
      accentColor: theme.colors.feature.meals,
      accentSoft: iconBackground(theme.colors.feature.meals),
      onPress: args.onPress.meals,
    },
    {
      id: 'todos',
      title: 'Todos',
      statusText: args.loading.todos
        ? 'Checking today'
        : args.todayTodoCount > 0
          ? args.todayTodoCount === 1 ? '1 due today' : `${args.todayTodoCount} due today`
          : 'Nothing due today',
      icon: 'checkmark-done-outline',
      accentColor: theme.colors.feature.todos,
      accentSoft: iconBackground(theme.colors.feature.todos),
      onPress: args.onPress.todos,
    },
    {
      id: 'shopping',
      title: 'Shopping',
      statusText: args.loading.shopping
        ? 'Checking your lists'
        : args.shoppingListCount > 0
          ? args.shoppingListCount === 1 ? '1 active list' : `${args.shoppingListCount} active lists`
          : 'No lists yet',
      icon: 'cart-outline',
      accentColor: theme.colors.feature.shopping,
      accentSoft: iconBackground(theme.colors.feature.shopping),
      onPress: args.onPress.shopping,
    },
    {
      id: 'economy',
      title: 'Economy',
      statusText: args.loading.economy
        ? 'Checking shared activity'
        : args.hasOpenSettlement
          ? args.participantCount <= 1
            ? 'Open settlement period'
            : `Open for ${args.participantCount} people`
          : 'No shared updates',
      icon: 'wallet-outline',
      accentColor: theme.colors.feature.economy,
      accentSoft: iconBackground(theme.colors.feature.economy),
      onPress: args.onPress.economy,
    },
    {
      id: 'Documents',
      title: 'Documents',
      statusText: args.loading.documents
        ? 'Checking your records'
        : args.documentCount > 0
          ? args.documentCount === 1 ? '1 saved item' : `${args.documentCount} saved items`
          : 'No documents yet',
      icon: 'document-text-outline',
      accentColor: theme.colors.feature.documents,
      accentSoft: iconBackground(theme.colors.feature.documents),
      onPress: args.onPress.documents,
    },
  ];
}
