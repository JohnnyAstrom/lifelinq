import { ActivityIndicator, Linking, Pressable, StyleSheet, Text, View } from 'react-native';
import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { BottomSheetModalProvider } from '@gorhom/bottom-sheet';
import { KeyboardProvider } from 'react-native-keyboard-controller';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '../shared/auth/AuthContext';
import { PendingInviteProvider, usePendingInvite } from '../shared/invite/PendingInviteContext';
import { setActiveGroup, updateProfile } from '../features/auth/api/meApi';
import { acceptInvitation, createGroup, renameCurrentPlace } from '../features/group/api/groupApi';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../features/auth/screens/LoginScreen';
import { CompleteProfileScreen } from '../features/auth/screens/CompleteProfileScreen';
import { CreateGroupScreen } from '../features/group/screens/CreateGroupScreen';
import { GroupDetailsScreen } from '../features/group/screens/GroupDetailsScreen';
import { SelectActiveGroupScreen } from '../features/group/screens/SelectActiveGroupScreen';
import { TodoListScreen } from '../features/todo/screens/TodoListScreen';
import { MealsWeekScreen } from '../features/meals/screens/MealsWeekScreen';
import { ShoppingListsScreen } from '../features/shopping/screens/ShoppingListsScreen';
import { ShoppingListDetailScreen } from '../features/shopping/screens/ShoppingListDetailScreen';
import { DocumentsScreen } from '../features/documents/screens/DocumentsScreen';
import { SettingsScreen } from '../screens/SettingsScreen';
import { ManagePlaceScreen } from '../screens/ManagePlaceScreen';
import { SpacesScreen } from '../screens/SpacesScreen';
import { AcceptInviteScreen } from '../screens/AcceptInviteScreen';
import { AppToast } from '../shared/ui/AppToast';
import { ApiError, UnauthorizedError, formatApiError } from '../shared/api/client';
import { ActionSheet } from '../shared/ui/ActionSheet';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Screen =
  | 'home'
  | 'todos'
  | 'members'
  | 'shopping'
  | 'shopping-detail'
  | 'meals'
  | 'settings'
  | 'manage-place'
  | 'spaces'
  | 'documents'
  | 'accept-invite';

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <KeyboardProvider>
          <BottomSheetModalProvider>
            <View style={{ flex: 1 }}>
              <AuthProvider>
                <PendingInviteProvider>
                  <AppShell />
                </PendingInviteProvider>
              </AuthProvider>
            </View>
          </BottomSheetModalProvider>
        </KeyboardProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

function HydratingSplashScreen() {
  return (
    <AppScreen scroll={false} contentStyle={{ justifyContent: 'center', alignItems: 'center' }}>
      <ActivityIndicator color={theme.colors.primary} />
    </AppScreen>
  );
}

function parseAuthCompleteUrl(url: string): { token?: string; refresh?: string; error?: string } | null {
  const [base, fragment = ''] = url.split('#', 2);
  const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base;
  if (normalizedBase !== 'mobileapp://auth/complete') {
    return null;
  }
  const params = new URLSearchParams(fragment);
  const token = params.get('token')?.trim();
  const refresh = params.get('refresh')?.trim();
  const error = params.get('error')?.trim();
  if (token) {
    return { token, refresh };
  }
  if (error) {
    return { error };
  }
  return null;
}

function parseInviteUrl(url: string): { token: string } | null {
  const [baseWithPath, queryAndMaybeFragment = ''] = url.split('?', 2);
  const normalizedBase = baseWithPath.endsWith('/') ? baseWithPath.slice(0, -1) : baseWithPath;
  if (normalizedBase !== 'mobileapp://invite') {
    return null;
  }
  const query = queryAndMaybeFragment.split('#', 1)[0];
  const params = new URLSearchParams(query);
  const token = params.get('token')?.trim();
  if (!token) {
    return null;
  }
  return { token };
}

function AppShell() {
  const { status, token, reloadMe, login, handleApiError } = useAuth();
  const { pendingInviteToken, setPendingInviteToken, clearPendingInviteToken } = usePendingInvite();
  const [authError, setAuthError] = useState<string | null>(null);
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [autoAccepting, setAutoAccepting] = useState(false);
  const lastHandledUrlRef = useRef<string | null>(null);
  const tokenInFlightRef = useRef<string | null>(null);
  const previousStatusRef = useRef(status);
  const autoAcceptInFlightRef = useRef<string | null>(null);
  const lastAutoAcceptedTokenRef = useRef<string | null>(null);

  const handleIncomingUrl = useCallback(
    async (url: string | null | undefined) => {
      if (!url || lastHandledUrlRef.current === url) {
        return;
      }
      const parsed = parseAuthCompleteUrl(url);
      if (parsed) {
        lastHandledUrlRef.current = url;
        if (parsed.token) {
          if (status === 'authenticated') {
            return;
          }
          if (tokenInFlightRef.current === parsed.token) {
            return;
          }
          tokenInFlightRef.current = parsed.token;
          setAuthError(null);
          try {
            await login(parsed.token, parsed.refresh ?? null);
          } finally {
            tokenInFlightRef.current = null;
          }
          return;
        }
        if (parsed.error) {
          setAuthError('Magic link is invalid or expired.');
        }
        return;
      }

      const invite = parseInviteUrl(url);
      if (invite) {
        lastHandledUrlRef.current = url;
        setInviteError(null);
        setPendingInviteToken(invite.token);
      }
    },
    [login, setPendingInviteToken, status]
  );

  useEffect(() => {
    let active = true;

    (async () => {
      const initialUrl = await Linking.getInitialURL();
      if (!active) {
        return;
      }
      void handleIncomingUrl(initialUrl);
    })();

    const subscription = Linking.addEventListener('url', (event) => {
      void handleIncomingUrl(event.url);
    });

    return () => {
      active = false;
      subscription.remove();
    };
  }, [handleIncomingUrl]);

  useEffect(() => {
    if (status !== 'unauthenticated') {
      return;
    }
    clearPendingInviteToken();
    setInviteError(null);
    setAuthError(null);
  }, [status, clearPendingInviteToken]);

  useEffect(() => {
    const previousStatus = previousStatusRef.current;
    previousStatusRef.current = status;

    const transitionedToAuthenticated = previousStatus === 'unauthenticated' && status === 'authenticated';
    if (!transitionedToAuthenticated || !pendingInviteToken || !token) {
      return;
    }
    if (autoAcceptInFlightRef.current === pendingInviteToken) {
      return;
    }
    if (lastAutoAcceptedTokenRef.current === pendingInviteToken) {
      return;
    }

    let cancelled = false;
    autoAcceptInFlightRef.current = pendingInviteToken;
    setAutoAccepting(true);

    (async () => {
      try {
        await acceptInvitation(token, pendingInviteToken);
        if (cancelled) {
          return;
        }
        lastAutoAcceptedTokenRef.current = pendingInviteToken;
        clearPendingInviteToken();
        await reloadMe();
        setInviteError(null);
      } catch (err) {
        if (cancelled) {
          return;
        }
        clearPendingInviteToken();
        if (err instanceof UnauthorizedError || (err instanceof ApiError && err.status === 401)) {
          await handleApiError(err);
          setInviteError(null);
          return;
        }
        if (err instanceof ApiError && err.status === 409) {
          setInviteError('This invitation is invalid or expired.');
          return;
        }
        setInviteError('Invitation could not be accepted.');
      } finally {
        if (!cancelled) {
          autoAcceptInFlightRef.current = null;
          setAutoAccepting(false);
        }
      }
    })();

    return () => {
      cancelled = true;
      autoAcceptInFlightRef.current = null;
      setAutoAccepting(false);
    };
  }, [status, pendingInviteToken, token, clearPendingInviteToken, reloadMe, handleApiError]);

  if (status === 'hydrating') {
    return <HydratingSplashScreen />;
  }

  if (status === 'unauthenticated') {
    return (
      <AuthStack
        onLoggedIn={login}
        authError={authError}
        onClearAuthError={() => setAuthError(null)}
      />
    );
  }

  return (
    <AppStack
      pendingInviteToken={pendingInviteToken}
      inviteError={inviteError}
      clearInviteError={() => setInviteError(null)}
      autoAccepting={autoAccepting}
    />
  );
}

function AuthStack({
  onLoggedIn,
  authError,
  onClearAuthError,
}: {
  onLoggedIn: (accessToken: string, refreshToken?: string | null) => Promise<void>;
  authError: string | null;
  onClearAuthError: () => void;
}) {
  return <LoginScreen onLoggedIn={onLoggedIn} authError={authError} onClearAuthError={onClearAuthError} />;
}

function AppStack({
  pendingInviteToken,
  inviteError,
  clearInviteError,
  autoAccepting,
}: {
  pendingInviteToken: string | null;
  inviteError: string | null;
  clearInviteError: () => void;
  autoAccepting: boolean;
}) {
  const { token, me, meLoading, meError, reloadMe, logout, handleApiError } = useAuth();
  const { setPendingInviteToken, clearPendingInviteToken } = usePendingInvite();
  const [screen, setScreen] = useState<Screen>('home');
  const [acceptInviteToken, setAcceptInviteToken] = useState<string | null>(null);
  const [activeShoppingListId, setActiveShoppingListId] = useState<string | null>(null);
  const [switchSheetOpen, setSwitchSheetOpen] = useState(false);
  const [switchingGroupId, setSwitchingGroupId] = useState<string | null>(null);
  const [switchError, setSwitchError] = useState<string | null>(null);
  const [joinSheetOpen, setJoinSheetOpen] = useState(false);
  const [joinTokenInput, setJoinTokenInput] = useState('');
  const [joinLoading, setJoinLoading] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);
  const [createSheetOpen, setCreateSheetOpen] = useState(false);
  const [createNameInput, setCreateNameInput] = useState('');
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [pendingCreatedGroupId, setPendingCreatedGroupId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; key: number } | null>(null);
  const strings = {
    loadingProfileTitle: 'Loading your profile',
    loadingProfileSubtitle: 'Please wait a moment.',
    genericErrorTitle: 'Something went wrong',
    unknownScreen: 'Unknown screen',
  };

  function showToast(message: string) {
    setToast((prev) => ({ message, key: (prev?.key ?? 0) + 1 }));
  }

  async function switchPlace(groupId: string) {
    if (!token) {
      return;
    }
    await setActiveGroup(token, groupId);
    const updatedMe = await reloadMe();
    const activeMembership = updatedMe?.activeGroupId
      ? updatedMe.memberships.find((membership) => membership.groupId === updatedMe.activeGroupId) ?? null
      : null;
    const placeName = activeMembership?.groupName?.trim() || 'My space';
    showToast(`Now in ${placeName}.`);
  }

  function openSwitchSheet() {
    setSwitchError(null);
    setSwitchSheetOpen(true);
  }

  function openJoinSheet() {
    clearInviteError();
    setJoinError(null);
    setJoinTokenInput(pendingInviteToken ?? '');
    setJoinSheetOpen(true);
  }

  function openCreateSheet() {
    setCreateError(null);
    setCreateNameInput('');
    setPendingCreatedGroupId(null);
    setCreateSheetOpen(true);
  }

  function closeJoinSheet() {
    if (joinLoading) {
      return;
    }
    clearInviteError();
    setJoinSheetOpen(false);
    setJoinError(null);
  }

  function closeCreateSheet() {
    if (createLoading) {
      return;
    }
    setCreateSheetOpen(false);
    setCreateError(null);
  }

  async function handleJoinPlaceSubmit() {
    const normalizedToken = joinTokenInput.trim();
    if (!token || !normalizedToken || joinLoading) {
      return;
    }
    setPendingInviteToken(normalizedToken);
    setJoinError(null);
    setJoinLoading(true);
    try {
      await acceptInvitation(token, normalizedToken);
      const updatedMe = await reloadMe();
      const activeMembership = updatedMe?.activeGroupId
        ? updatedMe.memberships.find((membership) => membership.groupId === updatedMe.activeGroupId) ?? null
        : null;
      const placeName = activeMembership?.groupName?.trim() || 'My space';
      showToast(`You joined ${placeName}.`);
      clearPendingInviteToken();
      setJoinSheetOpen(false);
      setJoinTokenInput('');
    } catch (err) {
      await handleApiError(err);
      setJoinError('Could not join this place. Check the code and try again.');
    } finally {
      setJoinLoading(false);
    }
  }

  async function handleSwitchPlaceSelection(groupId: string) {
    if (switchingGroupId) {
      return;
    }
    if (groupId === me?.activeGroupId) {
      setSwitchSheetOpen(false);
      return;
    }
    setSwitchError(null);
    setSwitchingGroupId(groupId);
    try {
      await switchPlace(groupId);
      setSwitchSheetOpen(false);
    } catch (err) {
      await handleApiError(err);
      setSwitchError(formatApiError(err));
    } finally {
      setSwitchingGroupId(null);
    }
  }

  async function handleCreatePlaceSubmit() {
    const nextName = createNameInput.trim();
    if (!token || !nextName || createLoading) {
      return;
    }
    setCreateLoading(true);
    setCreateError(null);
    setPendingCreatedGroupId(null);
    try {
      const created = await createGroup(token, nextName);
      setPendingCreatedGroupId(created.groupId);
      try {
        await switchPlace(created.groupId);
        setCreateSheetOpen(false);
        setCreateNameInput('');
        setPendingCreatedGroupId(null);
      } catch (activateErr) {
        await handleApiError(activateErr);
        setCreateError(`Place created, but we could not switch yet. ${formatApiError(activateErr)}`);
      }
    } catch (err) {
      await handleApiError(err);
      setCreateError(formatApiError(err));
    } finally {
      setCreateLoading(false);
    }
  }

  async function handleRetryCreatePlaceSwitch() {
    if (!pendingCreatedGroupId || createLoading) {
      return;
    }
    setCreateLoading(true);
    setCreateError(null);
    try {
      await switchPlace(pendingCreatedGroupId);
      setCreateSheetOpen(false);
      setCreateNameInput('');
      setPendingCreatedGroupId(null);
    } catch (err) {
      await handleApiError(err);
      setCreateError(`Could not switch place yet. ${formatApiError(err)}`);
    } finally {
      setCreateLoading(false);
    }
  }

  if (!token) {
    return <HydratingSplashScreen />;
  }

  if (meLoading) {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard>
          <Text style={textStyles.h3}>{strings.loadingProfileTitle}</Text>
          <Subtle>{strings.loadingProfileSubtitle}</Subtle>
        </AppCard>
      </AppScreen>
    );
  }

  if (meError) {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard>
          <Text style={textStyles.h3}>{strings.genericErrorTitle}</Text>
          <Text style={{ color: theme.colors.danger }}>{meError}</Text>
        </AppCard>
      </AppScreen>
    );
  }

  if (!me) {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard>
          <Text style={textStyles.h3}>{strings.loadingProfileTitle}</Text>
          <Subtle>{strings.loadingProfileSubtitle}</Subtle>
        </AppCard>
      </AppScreen>
    );
  }
  const meData = me;

  const hasCompletedProfile =
    !!meData.firstName?.trim() &&
    !!meData.lastName?.trim();

  const activeMembershipForProfile = meData.activeGroupId
    ? meData.memberships.find((membership) => membership.groupId === meData.activeGroupId) ?? null
    : null;

  if (!hasCompletedProfile) {
    return (
      <CompleteProfileScreen
        token={token}
        initialFirstName={meData.firstName}
        initialLastName={meData.lastName}
        initialPlaceName="Personal"
        onSubmitProfile={async (firstName, lastName, initialPlaceName) => {
          await updateProfile(token, firstName, lastName);

          if (initialPlaceName == null || activeMembershipForProfile?.isDefault !== true) {
            return;
          }

          const currentPlaceName = activeMembershipForProfile.groupName?.trim() ?? '';
          if (currentPlaceName === initialPlaceName) {
            return;
          }

          await renameCurrentPlace(token, initialPlaceName);
        }}
        onCompleted={() => {
          reloadMe();
        }}
      />
    );
  }

  if (me.memberships.length === 0) {
    return (
      <CreateGroupScreen
        token={token}
        onCreated={async () => {
          reloadMe();
          setScreen('home');
        }}
      />
    );
  }

  if (me.activeGroupId == null) {
    return (
      <SelectActiveGroupScreen
        token={token}
        memberships={me.memberships}
        onSelected={() => {
          reloadMe();
        }}
      />
    );
  }

  const switchItems = me.memberships;
  const activeGroupId = me.activeGroupId;
  const shouldRenderSwitchSheet = switchSheetOpen && switchItems.length > 1;
  const switchSheet = shouldRenderSwitchSheet ? (
    <ActionSheet visible={switchSheetOpen} onClose={() => setSwitchSheetOpen(false)} presentation="compact">
      <View style={styles.switchSheetContent}>
        <Subtle>Switch place</Subtle>
        {switchError ? <Text style={styles.switchError}>{switchError}</Text> : null}
        <View style={styles.switchList}>
          {switchItems.map((membership) => {
            const isActive = membership.groupId === activeGroupId;
            const isSwitching = switchingGroupId === membership.groupId;
            const label = membership.groupName?.trim() || 'My space';
            return (
              <Pressable
                key={membership.groupId}
                onPress={() => {
                  void handleSwitchPlaceSelection(membership.groupId);
                }}
                disabled={!!switchingGroupId}
                style={({ pressed }) => [
                  styles.switchRow,
                  isActive ? styles.switchRowActive : null,
                  pressed ? styles.switchRowPressed : null,
                ]}
              >
                <Text style={styles.switchRowLabel}>{label}</Text>
                {isActive ? <Text style={styles.switchCheck}>✓</Text> : null}
                {isSwitching ? <Subtle>…</Subtle> : null}
              </Pressable>
            );
          })}
        </View>
      </View>
    </ActionSheet>
  ) : null;

  const joinSheet = joinSheetOpen ? (
    <ActionSheet visible={joinSheetOpen} onClose={closeJoinSheet} presentation="large">
      <View style={styles.joinSheetContent}>
        <Text style={textStyles.h3}>Join a place</Text>
        <AppInput
          value={joinTokenInput}
          onChangeText={setJoinTokenInput}
          placeholder="Paste invitation code"
          returnKeyType="done"
          onSubmitEditing={() => {
            void handleJoinPlaceSubmit();
          }}
        />
        {joinLoading ? <Subtle>Joining...</Subtle> : null}
        {joinError || inviteError ? <Text style={styles.joinError}>{joinError ?? inviteError}</Text> : null}
        <View style={styles.joinActions}>
          <AppButton
            title={joinLoading ? 'Joining...' : 'Join'}
            onPress={() => {
              void handleJoinPlaceSubmit();
            }}
            disabled={joinLoading}
            fullWidth
          />
          <AppButton
            title="Cancel"
            onPress={closeJoinSheet}
            variant="ghost"
            disabled={joinLoading}
            fullWidth
          />
        </View>
      </View>
    </ActionSheet>
  ) : null;

  const createSheet = createSheetOpen ? (
    <ActionSheet visible={createSheetOpen} onClose={closeCreateSheet} presentation="large">
      <View style={styles.createSheetContent}>
        <Text style={textStyles.h3}>Create new place</Text>
        <AppInput
          value={createNameInput}
          onChangeText={setCreateNameInput}
          placeholder="Place name"
          returnKeyType="done"
          onSubmitEditing={() => {
            void handleCreatePlaceSubmit();
          }}
        />
        {createError ? <Text style={styles.createError}>{createError}</Text> : null}
        <View style={styles.createActions}>
          <AppButton
            title={createLoading ? 'Creating...' : 'Create'}
            onPress={() => {
              void handleCreatePlaceSubmit();
            }}
            disabled={createLoading || createNameInput.trim().length === 0}
            fullWidth
          />
          {pendingCreatedGroupId && createError ? (
            <AppButton
              title="Try again"
              onPress={() => {
                void handleRetryCreatePlaceSwitch();
              }}
              disabled={createLoading}
              variant="ghost"
              fullWidth
            />
          ) : null}
          <AppButton
            title="Cancel"
            onPress={closeCreateSheet}
            variant="ghost"
            disabled={createLoading}
            fullWidth
          />
        </View>
      </View>
    </ActionSheet>
  ) : null;

  const toastOverlay = (
    <AppToast
      message={toast?.message ?? null}
      toastKey={toast?.key ?? 0}
      onDone={() => setToast(null)}
    />
  );

  let screenContent: ReactNode;

  if (screen === 'todos') {
    screenContent = (
      <TodoListScreen
        token={token}
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  } else if (screen === 'members') {
    screenContent = (
      <GroupDetailsScreen
        token={token}
        me={me}
        onDone={() => {
          setScreen('spaces');
        }}
      />
    );
  } else if (screen === 'shopping') {
    screenContent = (
      <ShoppingListsScreen
        token={token}
        onSelectList={(listId) => {
          setActiveShoppingListId(listId);
          setScreen('shopping-detail');
        }}
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  } else if (screen === 'shopping-detail' && activeShoppingListId) {
    screenContent = (
      <ShoppingListDetailScreen
        token={token}
        listId={activeShoppingListId}
        onBack={() => {
          setScreen('shopping');
        }}
      />
    );
  } else if (screen === 'meals') {
    screenContent = (
      <MealsWeekScreen
        token={token}
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  } else if (screen === 'documents') {
    screenContent = (
      <DocumentsScreen
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  } else if (screen === 'settings') {
    screenContent = (
      <SettingsScreen
        me={me}
        onDone={() => {
          setScreen('home');
        }}
        onManagePlace={() => {
          setScreen('manage-place');
        }}
        onSwitchPlace={openSwitchSheet}
        onCreatePlace={openCreateSheet}
        onJoinPlace={openJoinSheet}
        onLogout={async () => {
          clearPendingInviteToken();
          await logout();
        }}
      />
    );
  } else if (screen === 'manage-place') {
    const meData = me;
    const activeMembership = meData.activeGroupId
      ? meData.memberships.find((membership) => membership.groupId === meData.activeGroupId) ?? null
      : null;
    const isDefaultGroupAction = activeMembership?.isDefault === true;

    screenContent = (
      <ManagePlaceScreen
        token={token}
        me={meData}
        onPlaceRenamed={async () => {
          await reloadMe();
          showToast('Place renamed.');
        }}
        onPlaceLeft={async (oldPlaceName) => {
          if (isDefaultGroupAction) {
            if (__DEV__) {
              console.warn('Blocked default place side effects for leave callback.');
            }
            return;
          }
          await reloadMe();
          setScreen('settings');
          showToast(`You left ${oldPlaceName}.`);
        }}
        onPlaceDeleted={async (_oldPlaceName) => {
          if (isDefaultGroupAction) {
            if (__DEV__) {
              console.warn('Blocked default place side effects for delete callback.');
            }
            return;
          }
          await reloadMe();
          setScreen('settings');
          showToast('Place deleted.');
        }}
        onShowToast={showToast}
        onDone={() => {
          setScreen('settings');
        }}
      />
    );
  } else if (screen === 'spaces') {
    screenContent = (
      <SpacesScreen
        me={me}
        onDone={() => {
          setScreen('settings');
        }}
        onOpenSpace={() => {
          setScreen('members');
        }}
      />
    );
  } else if (screen === 'home') {
    const activeMembership = activeGroupId
      ? switchItems.find((membership) => membership.groupId === activeGroupId) ?? null
      : null;
    const currentSpaceName = activeMembership?.groupName ?? 'My space';

    screenContent = (
      <HomeScreen
        token={token}
        spaceName={currentSpaceName}
        onContextInvalidated={() => {
          reloadMe();
        }}
        onOpenSwitchSheet={openSwitchSheet}
        onCreateTodo={() => {
          setScreen('todos');
        }}
        onCreateShopping={() => {
          setScreen('shopping');
        }}
        onMeals={() => {
          setScreen('meals');
        }}
        onDocuments={() => {
          setScreen('documents');
        }}
        onSettings={() => {
          setScreen('settings');
        }}
      />
    );
  } else if (screen === 'accept-invite') {
    screenContent = (
      <AcceptInviteScreen
        token={token}
        inviteToken={acceptInviteToken}
        onAccepted={async () => {
          const updatedMe = await reloadMe();
          const activeMembership = updatedMe?.activeGroupId
            ? updatedMe.memberships.find((membership) => membership.groupId === updatedMe.activeGroupId) ?? null
            : null;
          const placeName = activeMembership?.groupName?.trim() || 'My space';
          showToast(`You joined ${placeName}.`);
          setAcceptInviteToken(null);
          setScreen('home');
        }}
        onBackHome={() => {
          setAcceptInviteToken(null);
          setScreen('home');
        }}
      />
    );
  } else {
    screenContent = (
      <View>
        <Text>{strings.unknownScreen}</Text>
      </View>
    );
  }

  return (
    <>
      {screenContent}
      {switchSheet}
      {joinSheet}
      {createSheet}
      {autoAccepting ? (
        <View style={styles.autoAcceptOverlay} pointerEvents="none">
          <AppCard style={styles.autoAcceptCard}>
            <Subtle>Joining place...</Subtle>
          </AppCard>
        </View>
      ) : null}
      {toastOverlay}
    </>
  );
}

const styles = StyleSheet.create({
  switchSheetContent: {
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.xl,
    gap: theme.spacing.md,
  },
  switchList: {
    gap: theme.spacing.xs,
  },
  switchRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surfaceAlt,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  switchRowActive: {
    borderColor: theme.colors.borderStrong,
  },
  switchRowPressed: {
    opacity: 0.85,
  },
  switchRowLabel: {
    ...textStyles.body,
    flex: 1,
  },
  switchCheck: {
    ...textStyles.body,
  },
  switchError: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  joinSheetContent: {
    gap: theme.spacing.md,
  },
  joinActions: {
    gap: theme.spacing.sm,
  },
  joinError: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  createSheetContent: {
    gap: theme.spacing.md,
  },
  createActions: {
    gap: theme.spacing.sm,
  },
  createError: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  autoAcceptOverlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: theme.spacing.lg,
  },
  autoAcceptCard: {
    width: '100%',
    maxWidth: 320,
    alignItems: 'center',
  },
});

