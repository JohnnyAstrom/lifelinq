import * as SecureStore from 'expo-secure-store';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const TOKEN_KEY = 'access_token';

function hasSecureStore(): boolean {
  return typeof SecureStore?.getItemAsync === 'function'
    && typeof SecureStore?.setItemAsync === 'function'
    && typeof SecureStore?.deleteItemAsync === 'function';
}

async function getFromStore(): Promise<string | null> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    return SecureStore.getItemAsync(TOKEN_KEY);
  }
  return AsyncStorage.getItem(TOKEN_KEY);
}

async function setInStore(token: string): Promise<void> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    await SecureStore.setItemAsync(TOKEN_KEY, token);
    return;
  }
  await AsyncStorage.setItem(TOKEN_KEY, token);
}

async function clearFromStore(): Promise<void> {
  if (Platform.OS !== 'web' && hasSecureStore()) {
    await SecureStore.deleteItemAsync(TOKEN_KEY);
    return;
  }
  await AsyncStorage.removeItem(TOKEN_KEY);
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
