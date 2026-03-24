import { Platform, type DimensionValue } from 'react-native';

const headingFont = Platform.select({
  ios: 'Avenir Next',
  android: 'sans-serif-medium',
  default: 'Avenir Next',
});

const bodyFont = Platform.select({
  ios: 'Avenir Next',
  android: 'sans-serif',
  default: 'Avenir Next',
});

function hexToRgba(hex: string, alpha: number): string {
  const normalized = hex.replace('#', '');
  const value = normalized.length === 3
    ? normalized.split('').map((char) => `${char}${char}`).join('')
    : normalized;

  const red = Number.parseInt(value.slice(0, 2), 16);
  const green = Number.parseInt(value.slice(2, 4), 16);
  const blue = Number.parseInt(value.slice(4, 6), 16);

  return `rgba(${red}, ${green}, ${blue}, ${alpha})`;
}

const featureAccents = {
  todos: '#4CAF7A',
  meals: '#E49B55',
  shopping: '#5D8FD8',
  economy: '#8A6BD8',
  documents: '#A3A3A3',
} as const;

export type FeatureAccentKey = keyof typeof featureAccents;

const sheetMaxHeight = {
  web: '94%',
  compact: '76%',
  standard: '86%',
  tall: '95%',
} as const satisfies Record<string, DimensionValue>;

const sheetPadding = 16;

export const theme = {
  colors: {
    background: '#FCFAF7',
    scrim: 'rgba(0,0,0,0.4)',
    card: '#FFFFFF',
    surfaceSubtle: '#F2ECE5',
    border: '#E3E6EA',
    textPrimary: '#1F2328',
    textSecondary: '#6B7280',
    feature: featureAccents,
    danger: '#B04444',
    success: '#2F7A4F',

    // Backward-compatible aliases for existing UI code.
    bg: '#FCFAF7',
    surface: '#FFFFFF',
    surfaceAlt: '#F7F3EE',
    text: '#1F2328',
    subtle: '#6B7280',
    borderStrong: '#E3E6EA',
    primary: '#4CAF7A',
    primarySoft: hexToRgba(featureAccents.todos, 0.15),
    accent: '#E49B55',
    accentSoft: hexToRgba(featureAccents.meals, 0.15),
  },
  spacing: {
    xs: 8,
    sm: 16,
    md: 24,
    lg: 32,
    xl: 40,
    xxl: 48,
  },
  radius: {
    cardRadius: 16,
    sm: 6,
    pill: 999,
    circle: 999,
    md: 16,
    lg: 16,
    xl: 24,
  },
  layout: {
    maxContentWidth: 460,
    topBarOffset: 90,
    sheetMaxWidth: 760,
    sheetMaxHeight,
    sheetPadding,
  },
  elevation: {
    card: {
      shadowColor: '#000000',
      shadowOpacity: 0.08,
      shadowRadius: 10,
      shadowOffset: { width: 0, height: 6 },
      elevation: 2,
    },
    floating: {
      shadowColor: '#000000',
      shadowOpacity: 0.16,
      shadowRadius: 14,
      shadowOffset: { width: 0, height: 8 },
      elevation: 6,
    },
  },
  typography: {
    heading: headingFont,
    body: bodyFont,
  },
};

export function iconBackground(color: string, alpha = 0.15): string {
  return hexToRgba(color, alpha);
}

export const textStyles = {
  h1: {
    fontSize: 26,
    fontWeight: '700' as const,
    color: theme.colors.textPrimary,
    fontFamily: theme.typography.heading,
  },
  h2: {
    fontSize: 20,
    fontWeight: '700' as const,
    color: theme.colors.textPrimary,
    fontFamily: theme.typography.heading,
  },
  h3: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: theme.colors.textPrimary,
    fontFamily: theme.typography.heading,
  },
  body: {
    fontSize: 15,
    color: theme.colors.textPrimary,
    fontFamily: theme.typography.body,
  },
  subtle: {
    fontSize: 13,
    color: theme.colors.textSecondary,
    fontFamily: theme.typography.body,
  },
};

export const shadow = theme.elevation.card;

