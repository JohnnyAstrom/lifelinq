import Constants from 'expo-constants';
import { Platform } from 'react-native';

const DEFAULT_BASE_URL = 'http://localhost:8080';

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}

function getExpoDevHost(): string | null {
  const hostUriFromExpoConfig = (Constants.expoConfig as { hostUri?: string } | null)?.hostUri;
  if (hostUriFromExpoConfig) {
    return hostUriFromExpoConfig.split(':')[0] ?? null;
  }

  const hostUriFromManifest = (
    Constants.manifest2 as { extra?: { expoClient?: { hostUri?: string } } } | null
  )?.extra?.expoClient?.hostUri;
  if (hostUriFromManifest) {
    return hostUriFromManifest.split(':')[0] ?? null;
  }

  return null;
}

export function getApiBaseUrl(): string {
  const configured = process.env.EXPO_PUBLIC_API_BASE_URL?.trim();
  if (configured) {
    return trimTrailingSlash(configured);
  }

  if (Platform.OS === 'android') {
    const devHost = getExpoDevHost();
    if (devHost) {
      return `http://${devHost}:8080`;
    }
    return 'http://10.0.2.2:8080';
  }

  return DEFAULT_BASE_URL;
}

