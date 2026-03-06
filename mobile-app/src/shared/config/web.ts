const LOCAL_WEB_PORT = process.env.EXPO_PUBLIC_LOCAL_WEB_PORT?.trim() || "8080";
// Keep this default host aligned with backend dev property:
// lifelinq.group.invitation.previewBaseUrl (application-dev.properties).

function normalizeBaseUrl(url: string): string {
  return url.replace(/\/+$/, "");
}

function resolveWebBaseUrl(): string {
  const configured = process.env.EXPO_PUBLIC_WEB_BASE_URL?.trim();
  if (configured) {
    return normalizeBaseUrl(configured);
  }

  if (__DEV__) {
    return normalizeBaseUrl(`http://10.0.2.2:${LOCAL_WEB_PORT}`);
  }

  throw new Error("Missing EXPO_PUBLIC_WEB_BASE_URL for production build.");
}

export const WEB_BASE_URL = resolveWebBaseUrl();
