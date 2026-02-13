import { Platform } from 'react-native';

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

export const theme = {
  colors: {
    bg: '#f3f1ed',
    surface: '#ffffff',
    surfaceAlt: '#fcfaf6',
    text: '#1f1b16',
    subtle: '#6f6a61',
    border: '#e5dfd6',
    borderStrong: '#d4c9bc',
    primary: '#1b6f6a',
    primarySoft: '#dceeed',
    accent: '#e09f3e',
    accentSoft: '#f9e6c9',
    danger: '#b04444',
    success: '#2f7a4f',
  },
  spacing: {
    xs: 6,
    sm: 10,
    md: 14,
    lg: 18,
    xl: 24,
    xxl: 32,
  },
  radius: {
    sm: 8,
    md: 12,
    lg: 18,
    xl: 24,
  },
  typography: {
    heading: headingFont,
    body: bodyFont,
  },
};

export const textStyles = {
  h1: {
    fontSize: 26,
    fontWeight: '700' as const,
    color: theme.colors.text,
    fontFamily: theme.typography.heading,
  },
  h2: {
    fontSize: 20,
    fontWeight: '700' as const,
    color: theme.colors.text,
    fontFamily: theme.typography.heading,
  },
  h3: {
    fontSize: 16,
    fontWeight: '600' as const,
    color: theme.colors.text,
    fontFamily: theme.typography.heading,
  },
  body: {
    fontSize: 15,
    color: theme.colors.text,
    fontFamily: theme.typography.body,
  },
  subtle: {
    fontSize: 13,
    color: theme.colors.subtle,
    fontFamily: theme.typography.body,
  },
};

export const shadow = {
  shadowColor: '#000000',
  shadowOpacity: 0.08,
  shadowRadius: 10,
  shadowOffset: { width: 0, height: 6 },
  elevation: 2,
};
