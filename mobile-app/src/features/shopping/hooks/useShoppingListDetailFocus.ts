import { useMemo } from 'react';

type Args = {
  openItemCount: number;
  canReorderOpenItems: boolean;
};

export function useShoppingListDetailFocus({ openItemCount, canReorderOpenItems }: Args) {
  return useMemo(() => {
    const isShoppingFocused = openItemCount > 0;

    return {
      isShoppingFocused,
      showReorderHint: canReorderOpenItems && openItemCount > 0 && !isShoppingFocused,
      summaryTitle: 'Shopping now',
      summarySubtitle:
        openItemCount === 1 ? '1 item left to check off' : `${openItemCount} items left to check off`,
    };
  }, [canReorderOpenItems, openItemCount]);
}
