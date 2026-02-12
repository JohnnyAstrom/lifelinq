import AsyncStorage from '@react-native-async-storage/async-storage';

const TOKEN_KEY = 'lifelinq.token';
let currentToken: string | null = null;

export async function getToken(): Promise<string | null> {
  if (currentToken) {
    return currentToken;
  }
  const stored = await AsyncStorage.getItem(TOKEN_KEY);
  currentToken = stored;
  return stored;
}

export async function setToken(token: string): Promise<void> {
  currentToken = token;
  await AsyncStorage.setItem(TOKEN_KEY, token);
}

export async function clearToken(): Promise<void> {
  currentToken = null;
  await AsyncStorage.removeItem(TOKEN_KEY);
}
