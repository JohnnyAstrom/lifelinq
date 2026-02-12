let currentToken: string | null = null;

export async function getToken(): Promise<string | null> {
  return currentToken;
}

export async function setToken(token: string): Promise<void> {
  currentToken = token;
}

export async function clearToken(): Promise<void> {
  currentToken = null;
}
