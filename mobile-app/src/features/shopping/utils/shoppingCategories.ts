import type { ShoppingListType } from '../api/shoppingApi';

export type ShoppingCategoryKey =
  | 'produce'
  | 'dairy'
  | 'bakery'
  | 'meat-seafood'
  | 'pantry'
  | 'frozen'
  | 'snacks-drinks'
  | 'household'
  | 'health-beauty'
  | 'other';

export type ShoppingCategoryDefinition = {
  key: ShoppingCategoryKey;
  label: string;
  order: number;
  keywords: string[];
};

const CATEGORY_DEFINITIONS: ShoppingCategoryDefinition[] = [
  {
    key: 'produce',
    label: 'Produce',
    order: 10,
    keywords: [
      'apple',
      'apples',
      'banana',
      'bananas',
      'tomato',
      'tomatoes',
      'potato',
      'potatoes',
      'onion',
      'onions',
      'carrot',
      'carrots',
      'cucumber',
      'avocado',
      'broccoli',
      'lettuce',
      'salad',
      'spinach',
      'pepper',
      'paprika',
      'lemon',
      'lime',
      'orange',
      'berries',
      'berry',
      'fruit',
      'fruits',
      'vegetable',
      'vegetables',
      'apple',
      'banan',
      'bananer',
      'tomat',
      'tomater',
      'potatis',
      'lok',
      'morot',
      'moroter',
      'gurka',
      'avokado',
      'broccoli',
      'sallad',
      'spenat',
      'paprika',
      'citron',
      'lime',
      'apelsin',
      'bar',
      'frukt',
      'frukter',
      'gronsak',
      'gronsaker',
    ],
  },
  {
    key: 'dairy',
    label: 'Dairy',
    order: 20,
    keywords: [
      'milk',
      'yogurt',
      'yoghurt',
      'cheese',
      'butter',
      'cream',
      'creme fraiche',
      'egg',
      'eggs',
      'mjolk',
      'yoghurt',
      'ost',
      'smor',
      'gradde',
      'fraiche',
      'agg',
    ],
  },
  {
    key: 'bakery',
    label: 'Bakery',
    order: 30,
    keywords: [
      'bread',
      'bun',
      'buns',
      'bagel',
      'bagels',
      'wrap',
      'wraps',
      'tortilla',
      'tortillas',
      'pita',
      'loaf',
      'croissant',
      'brod',
      'fralla',
      'frallor',
      'bulle',
      'bullar',
      'tortilla',
      'tortillas',
      'knackebrod',
    ],
  },
  {
    key: 'meat-seafood',
    label: 'Meat & seafood',
    order: 40,
    keywords: [
      'chicken',
      'beef',
      'pork',
      'mince',
      'meat',
      'steak',
      'bacon',
      'sausage',
      'sausages',
      'salmon',
      'tuna',
      'shrimp',
      'prawn',
      'prawns',
      'fish',
      'kyckling',
      'notkott',
      'flask',
      'kottfars',
      'kott',
      'bacon',
      'korv',
      'lax',
      'tonfisk',
      'raka',
      'rakor',
      'fisk',
    ],
  },
  {
    key: 'pantry',
    label: 'Pantry',
    order: 50,
    keywords: [
      'pasta',
      'rice',
      'flour',
      'sugar',
      'salt',
      'oil',
      'vinegar',
      'beans',
      'lentils',
      'oats',
      'cereal',
      'muesli',
      'coffee',
      'tea',
      'spice',
      'spices',
      'sauce',
      'broth',
      'stock',
      'tomato sauce',
      'pasta sauce',
      'ris',
      'mjol',
      'socker',
      'salt',
      'olja',
      'vinager',
      'bonor',
      'linser',
      'havre',
      'flingor',
      'kaffe',
      'te',
      'krydda',
      'kryddor',
      'buljong',
      'sas',
      'krossade tomater',
    ],
  },
  {
    key: 'frozen',
    label: 'Frozen',
    order: 60,
    keywords: [
      'frozen',
      'ice cream',
      'fryst',
      'frysta',
      'glass',
    ],
  },
  {
    key: 'snacks-drinks',
    label: 'Snacks & drinks',
    order: 70,
    keywords: [
      'chips',
      'candy',
      'chocolate',
      'cookie',
      'cookies',
      'cracker',
      'crackers',
      'soda',
      'cola',
      'juice',
      'water',
      'sparkling water',
      'beer',
      'wine',
      'nuts',
      'snack',
      'snacks',
      'godis',
      'choklad',
      'kex',
      'lask',
      'juice',
      'vatten',
      'ol',
      'vin',
      'notter',
      'snacks',
    ],
  },
  {
    key: 'household',
    label: 'Household',
    order: 80,
    keywords: [
      'toilet paper',
      'paper towel',
      'paper towels',
      'dish soap',
      'detergent',
      'laundry detergent',
      'trash bag',
      'trash bags',
      'garbage bag',
      'garbage bags',
      'foil',
      'cling film',
      'battery',
      'batteries',
      'light bulb',
      'light bulbs',
      'cleaner',
      'cleaning spray',
      'toalettpapper',
      'hushallspapper',
      'diskmedel',
      'tvattmedel',
      'soppase',
      'soppasar',
      'plastpase',
      'plastpasar',
      'folie',
      'plastfolie',
      'batteri',
      'batterier',
      'glodlampa',
      'glodlampor',
      'rengoring',
      'rengoringsspray',
    ],
  },
  {
    key: 'health-beauty',
    label: 'Health & beauty',
    order: 90,
    keywords: [
      'shampoo',
      'conditioner',
      'toothpaste',
      'toothbrush',
      'deodorant',
      'lotion',
      'soap',
      'makeup',
      'vitamin',
      'vitamins',
      'medicine',
      'bandage',
      'bandages',
      'painkiller',
      'schampo',
      'balsam',
      'tandkram',
      'tandborste',
      'deodorant',
      'hudkram',
      'tval',
      'smink',
      'vitaminer',
      'medicin',
      'plaster',
      'varktablett',
      'varktabletter',
    ],
  },
  {
    key: 'other',
    label: 'Other',
    order: 100,
    keywords: [],
  },
];

const CATEGORY_BY_KEY = new Map(
  CATEGORY_DEFINITIONS.map((definition) => [definition.key, definition])
);

export const DEFAULT_SHOPPING_CATEGORY = CATEGORY_BY_KEY.get('other')!;

export function getShoppingCategoryDefinition(key: ShoppingCategoryKey): ShoppingCategoryDefinition {
  return CATEGORY_BY_KEY.get(key) ?? DEFAULT_SHOPPING_CATEGORY;
}

export function getShoppingCategoryDefinitions(): ShoppingCategoryDefinition[] {
  return CATEGORY_DEFINITIONS;
}

export function inferShoppingCategory(
  normalizedTitle: string,
  listType: ShoppingListType
): ShoppingCategoryDefinition {
  for (const definition of getShoppingCategoryDefinitionsForListType(listType)) {
    if (definition.key === 'other') {
      continue;
    }
    if (definition.keywords.some((keyword) => matchesKeyword(normalizedTitle, keyword))) {
      return definition;
    }
  }
  return DEFAULT_SHOPPING_CATEGORY;
}

export function getShoppingCategoryOrder(
  key: ShoppingCategoryKey,
  listType: ShoppingListType
): number {
  const definitions = getShoppingCategoryDefinitionsForListType(listType);
  const index = definitions.findIndex((definition) => definition.key === key);
  if (index >= 0) {
    return index;
  }
  return definitions.length;
}

function getShoppingCategoryDefinitionsForListType(
  listType: ShoppingListType
): ShoppingCategoryDefinition[] {
  const ordering = getCategoryPriorityForListType(listType);
  return ordering.map((key) => getShoppingCategoryDefinition(key));
}

function getCategoryPriorityForListType(listType: ShoppingListType): ShoppingCategoryKey[] {
  switch (listType) {
    case 'grocery':
      return ['produce', 'dairy', 'bakery', 'meat-seafood', 'pantry', 'frozen', 'snacks-drinks', 'household', 'health-beauty', 'other'];
    case 'consumables':
      return ['household', 'health-beauty', 'dairy', 'pantry', 'snacks-drinks', 'frozen', 'produce', 'bakery', 'meat-seafood', 'other'];
    case 'supplies':
      return ['household', 'health-beauty', 'pantry', 'snacks-drinks', 'frozen', 'bakery', 'dairy', 'produce', 'meat-seafood', 'other'];
    case 'mixed':
    default:
      return ['pantry', 'household', 'health-beauty', 'snacks-drinks', 'produce', 'dairy', 'bakery', 'meat-seafood', 'frozen', 'other'];
  }
}

function matchesKeyword(normalizedTitle: string, keyword: string): boolean {
  if (!normalizedTitle) {
    return false;
  }

  const normalizedKeyword = keyword.trim().toLowerCase();
  if (!normalizedKeyword) {
    return false;
  }

  const tokens = new Set(normalizedTitle.split(' ').filter(Boolean));
  if (!normalizedKeyword.includes(' ')) {
    return tokens.has(normalizedKeyword);
  }

  const paddedTitle = ` ${normalizedTitle} `;
  return paddedTitle.includes(` ${normalizedKeyword} `);
}
