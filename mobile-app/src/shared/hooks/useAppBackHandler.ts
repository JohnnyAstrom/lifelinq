import { useEffect, useState } from 'react';
import { BackHandler, Keyboard, Platform } from 'react-native';

type UseAppBackHandlerOptions = {
  canGoBack?: boolean;
  onGoBack?: () => void;
  isOverlayOpen?: boolean;
  onCloseOverlay?: () => void;
};

export function useAppBackHandler({
  canGoBack = false,
  onGoBack,
  isOverlayOpen = false,
  onCloseOverlay,
}: UseAppBackHandlerOptions) {
  const [isKeyboardVisible, setIsKeyboardVisible] = useState(false);

  useEffect(() => {
    const showEvent = Platform.OS === 'ios' ? 'keyboardWillShow' : 'keyboardDidShow';
    const hideEvent = Platform.OS === 'ios' ? 'keyboardWillHide' : 'keyboardDidHide';
    const showSub = Keyboard.addListener(showEvent, () => setIsKeyboardVisible(true));
    const hideSub = Keyboard.addListener(hideEvent, () => setIsKeyboardVisible(false));
    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, []);

  useEffect(() => {
    if (Platform.OS !== 'android') {
      return;
    }
    const subscription = BackHandler.addEventListener('hardwareBackPress', () => {
      if (isKeyboardVisible) {
        Keyboard.dismiss();
        return true;
      }
      if (isOverlayOpen && onCloseOverlay) {
        onCloseOverlay();
        return true;
      }
      if (canGoBack && onGoBack) {
        onGoBack();
        return true;
      }
      return false;
    });
    return () => {
      subscription.remove();
    };
  }, [canGoBack, isKeyboardVisible, isOverlayOpen, onCloseOverlay, onGoBack]);
}

