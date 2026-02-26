import { ApiError } from './client';

export type ScopedApiErrorOptions = {
  onContextInvalidated?: () => void | Promise<void>;
};

export async function handleScopedApiError(
  err: unknown,
  options: ScopedApiErrorOptions = {}
): Promise<void> {
  if (err instanceof ApiError && err.status === 409) {
    await options.onContextInvalidated?.();
  }
}
