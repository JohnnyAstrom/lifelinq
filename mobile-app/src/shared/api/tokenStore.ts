import * as SecureStore from 'expo-secure-store';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

function hasSecureStore(): boolean {
  return typeof SecureStore?.getItemAsync === 'function'
    && typeof SecureStore?.setItemAsync === 'function'
    && typeof SecureStore?.deleteItemAsync === 'function';
}

async function getFromStore(): Promise<string | null> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    return SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
  }
  return AsyncStorage.getItem(ACCESS_TOKEN_KEY);
}

async function setInStore(token: string): Promise<void> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, token);
    return;
  }
  await AsyncStorage.setItem(ACCESS_TOKEN_KEY, token);
}

async function clearFromStore(): Promise<void> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
    return;
  }
  await AsyncStorage.removeItem(ACCESS_TOKEN_KEY);
}

async function getRefreshFromStore(): Promise<string | null> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    return SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
  }
  return AsyncStorage.getItem(REFRESH_TOKEN_KEY);
}

async function setRefreshInStore(token: string): Promise<void> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, token);
    return;
  }
  await AsyncStorage.setItem(REFRESH_TOKEN_KEY, token);
}

async function clearRefreshFromStore(): Promise<void> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
    return;
  }
  await AsyncStorage.removeItem(REFRESH_TOKEN_KEY);
}

export async function getToken(): Promise<string | null> {
  return getFromStore();
}

export async function setToken(token: string): Promise<void> {
  await setInStore(token);
}

export async function clearToken(): Promise<void> {
  await clearFromStore();
}

export async function getRefreshToken(): Promise<string | null> {
  return getRefreshFromStore();
}

export async function setRefreshToken(token: string): Promise<void> {
  await setRefreshInStore(token);
}

export async function clearRefreshToken(): Promise<void> {
  await clearRefreshFromStore();
}

export async function setAuthTokens(accessToken: string, refreshToken: string): Promise<void> {
  await setInStore(accessToken);
  await setRefreshInStore(refreshToken);
}

export async function clearAuthTokens(): Promise<void> {
  await clearFromStore();
  await clearRefreshFromStore();
}
