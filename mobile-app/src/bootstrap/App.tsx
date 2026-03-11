import { Keyboard, Pressable, StatusBar, StyleSheet, Text, View } from 'react-native';
import { useEffect, useState, type ReactNode } from 'react';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { BottomSheetModalProvider } from '@gorhom/bottom-sheet';
import { KeyboardProvider } from 'react-native-keyboard-controller';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '../shared/auth/AuthContext';
import { PendingInviteProvider, usePendingInvite } from '../shared/invite/PendingInviteContext';
import { setActiveGroup, updateProfile } from '../features/auth/api/meApi';
import { createGroup, renameCurrentPlace } from '../features/group/api/groupApi';
import { HomeScreen } from '../screens/HomeScreen';
import { CompleteProfileScreen } from '../features/auth/screens/CompleteProfileScreen';
import { CreateGroupScreen } from '../features/group/screens/CreateGroupScreen';
import { GroupDetailsScreen } from '../features/group/screens/GroupDetailsScreen';
import { SelectActiveGroupScreen } from '../features/group/screens/SelectActiveGroupScreen';
import { TodoListScreen } from '../features/todo/screens/TodoListScreen';
import { MealsWeekScreen } from '../features/meals/screens/MealsWeekScreen';
import { ShoppingListsScreen } from '../features/shopping/screens/ShoppingListsScreen';
import { ShoppingListDetailScreen } from '../features/shopping/screens/ShoppingListDetailScreen';
import { DocumentsScreen } from '../features/documents/screens/DocumentsScreen';
import { EconomyScreen } from '../features/economy/screens/EconomyScreen';
import { SettingsScreen } from '../screens/SettingsScreen';
import { ManagePlaceScreen } from '../screens/ManagePlaceScreen';
import { ManageInvitationsScreen } from '../screens/ManageInvitationsScreen';
import { SpacesScreen } from '../screens/SpacesScreen';
import { AcceptInviteScreen } from '../screens/AcceptInviteScreen';
import { AppToast } from '../shared/ui/AppToast';
import { formatApiError } from '../shared/api/client';
import { ActionSheet } from '../shared/ui/ActionSheet';
import { OverlayHost } from '../shared/ui/OverlayHost';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';
import { AuthGate } from './AuthGate';
import { HydratingSplashScreen } from './HydratingSplashScreen';
import { useAuthBootstrap } from './useAuthBootstrap';
import { useDeepLinkBootstrap } from './useDeepLinkBootstrap';
import { useInviteBootstrap } from './useInviteBootstrap';
import { useJoinPlaceFlow } from '../flows/useJoinPlaceFlow';

type Screen =
  | 'home'
  | 'todos'
  | 'members'
  | 'shopping'
  | 'shopping-detail'
  | 'meals'
  | 'settings'
  | 'manage-place'
  | 'manage-invitations'
  | 'spaces'
  | 'documents'
  | 'economy'
  | 'accept-invite';

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <KeyboardProvider statusBarTranslucent navigationBarTranslucent>
          <BottomSheetModalProvider>
            <StatusBar
              translucent={false}
              backgroundColor={theme.colors.background}
              barStyle="dark-content"
            />
            <AuthProvider>
              <PendingInviteProvider>
                <View style={styles.appRoot}>
                  <AppShell />
                  <OverlayHost />
                </View>
              </PendingInviteProvider>
            </AuthProvider>
          </BottomSheetModalProvider>
        </KeyboardProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

function AppShell() {
  const { status, token, me, reloadMe, login, handleApiError, authError, setAuthError, clearAuthError } =
    useAuthBootstrap();
  const { pendingInviteToken, inviteOnboardingActive, setPendingInviteToken, clearPendingInviteToken } =
    usePendingInvite();
  const {
    inviteError,
    clearInviteError,
    autoAccepting,
    setManualInviteAcceptInFlight,
  } = useInviteBootstrap({
    status,
    token,
    me,
    pendingInviteToken,
    clearPendingInviteToken,
    reloadMe,
    handleApiError,
  });

  useDeepLinkBootstrap({
    onLoginFromDeepLink: login,
    onAuthError: setAuthError,
    onInviteToken: setPendingInviteToken,
    onClearInviteError: clearInviteError,
  });

  return (
    <AuthGate
      authError={authError}
      onClearAuthError={clearAuthError}
      onClearInviteError={clearInviteError}
    >
      <AppStack
        setManualInviteAcceptInFlight={setManualInviteAcceptInFlight}
        pendingInviteToken={pendingInviteToken}
        inviteOnboardingActive={inviteOnboardingActive}
        inviteError={inviteError}
        clearInviteError={clearInviteError}
        autoAccepting={autoAccepting}
      />
    </AuthGate>
  );
}

function AppStack({
  setManualInviteAcceptInFlight,
  pendingInviteToken,
  inviteOnboardingActive,
  inviteError,
  clearInviteError,
  autoAccepting,
}: {
  setManualInviteAcceptInFlight: (value: boolean) => void;
  pendingInviteToken: string | null;
  inviteOnboardingActive: boolean;
  inviteError: string | null;
  clearInviteError: () => void;
  autoAccepting: boolean;
}) {
  const { token, me, meLoading, meError, reloadMe, logout, handleApiError } = useAuth();
  const { setPendingInviteToken, clearPendingInviteToken, clearInviteOnboarding } = usePendingInvite();
  const { joinPlace } = useJoinPlaceFlow({
    token,
    reloadMe,
    handleApiError,
    setPendingInviteToken,
    clearPendingInviteToken,
    setManualInviteAcceptInFlight,
  });
  const [screen, setScreen] = useState<Screen>('home');
  const [membersReturnScreen, setMembersReturnScreen] = useState<Screen>('spaces');
  const [acceptInviteToken, setAcceptInviteToken] = useState<string | null>(null);
  const [activeShoppingListId, setActiveShoppingListId] = useState<string | null>(null);
  const [switchSheetOpen, setSwitchSheetOpen] = useState(false);
  const [switchingGroupId, setSwitchingGroupId] = useState<string | null>(null);
  const [switchError, setSwitchError] = useState<string | null>(null);
  const [joinSheetOpen, setJoinSheetOpen] = useState(false);
  const [joinTokenInput, setJoinTokenInput] = useState('');
  const [joinTokenInputFocused, setJoinTokenInputFocused] = useState(false);
  const [joinLoading, setJoinLoading] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);
  const [createSheetOpen, setCreateSheetOpen] = useState(false);
  const [createNameInput, setCreateNameInput] = useState('');
  const [createNameInputFocused, setCreateNameInputFocused] = useState(false);
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
    setJoinTokenInputFocused(false);
    setJoinSheetOpen(true);
  }

  function openCreateSheet() {
    setCreateError(null);
    setCreateNameInput('');
    setCreateNameInputFocused(false);
    setPendingCreatedGroupId(null);
    setCreateSheetOpen(true);
  }

  function closeJoinSheet() {
    if (joinLoading) {
      return;
    }
    clearInviteError();
    setJoinSheetOpen(false);
    setJoinTokenInputFocused(false);
    setJoinError(null);
  }

  function closeCreateSheet() {
    if (createLoading) {
      return;
    }
    setCreateSheetOpen(false);
    setCreateNameInputFocused(false);
    setCreateError(null);
  }

  useEffect(() => {
    if (!joinSheetOpen) {
      return;
    }
    const showSubscription = Keyboard.addListener('keyboardDidShow', () => {
      setJoinTokenInputFocused(true);
    });
    const hideSubscription = Keyboard.addListener('keyboardDidHide', () => {
      setJoinTokenInputFocused(false);
    });

    return () => {
      showSubscription.remove();
      hideSubscription.remove();
    };
  }, [joinSheetOpen]);

  useEffect(() => {
    if (!createSheetOpen) {
      return;
    }
    const showSubscription = Keyboard.addListener('keyboardDidShow', () => {
      setCreateNameInputFocused(true);
    });
    const hideSubscription = Keyboard.addListener('keyboardDidHide', () => {
      setCreateNameInputFocused(false);
    });

    return () => {
      showSubscription.remove();
      hideSubscription.remove();
    };
  }, [createSheetOpen]);

  async function handleJoinPlaceSubmit() {
    const normalizedInput = joinTokenInput.trim();
    if (!token || !normalizedInput || joinLoading) {
      return;
    }
    setJoinError(null);
    setJoinLoading(true);
    try {
      const result = await joinPlace(normalizedInput);

      if (result.status === 'success') {
        showToast(`You joined ${result.placeName}.`);
        setJoinSheetOpen(false);
        setJoinTokenInputFocused(false);
        setJoinTokenInput('');
        return;
      }
      setJoinError(result.message);
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
        setCreateNameInputFocused(false);
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
      setCreateNameInputFocused(false);
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
  const isInviteFlow = inviteOnboardingActive || !!pendingInviteToken;
  const inviteFlowGroupName =
    isInviteFlow && activeMembershipForProfile?.isDefault !== true
      ? activeMembershipForProfile?.groupName ?? null
      : null;

  if (!hasCompletedProfile) {
    return (
      <CompleteProfileScreen
        token={token}
        initialFirstName={meData.firstName}
        initialLastName={meData.lastName}
        initialPlaceName="Personal"
        isInviteFlow={isInviteFlow}
        inviteGroupName={inviteFlowGroupName}
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
          clearInviteOnboarding();
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
    <ActionSheet
      visible={joinSheetOpen}
      onClose={closeJoinSheet}
      presentation={joinTokenInputFocused ? 'large' : 'standard'}
    >
      <View style={styles.joinSheetContent}>
        <Text style={textStyles.h3}>Join a place</Text>
        <AppInput
          value={joinTokenInput}
          onChangeText={setJoinTokenInput}
          onFocus={() => setJoinTokenInputFocused(true)}
          onBlur={() => setJoinTokenInputFocused(false)}
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
    <ActionSheet
      visible={createSheetOpen}
      onClose={closeCreateSheet}
      presentation={createNameInputFocused ? 'large' : 'standard'}
    >
      <View style={styles.createSheetContent}>
        <Text style={textStyles.h3}>Create new place</Text>
        <AppInput
          value={createNameInput}
          onChangeText={setCreateNameInput}
          onFocus={() => setCreateNameInputFocused(true)}
          onBlur={() => setCreateNameInputFocused(false)}
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
          setScreen(membersReturnScreen);
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
  } else if (screen === 'economy') {
    screenContent = (
      <EconomyScreen
        token={token}
        onShowToast={showToast}
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
          clearInviteOnboarding();
          await logout();
        }}
      />
    );
  } else if (screen === 'manage-place') {
    screenContent = (
      <ManagePlaceScreen
        token={token}
        me={me}
        onPlaceRenamed={async () => {
          await reloadMe();
          showToast('Place renamed.');
        }}
        onManageMembers={() => {
          setMembersReturnScreen('manage-place');
          setScreen('members');
        }}
        onManageInvitations={() => {
          setScreen('manage-invitations');
        }}
        onDone={() => {
          setScreen('settings');
        }}
      />
    );
  } else if (screen === 'manage-invitations') {
    screenContent = (
      <ManageInvitationsScreen
        token={token}
        me={me}
        onShowToast={showToast}
        onDone={() => {
          setScreen('manage-place');
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
          setMembersReturnScreen('spaces');
          setScreen('members');
        }}
      />
    );
  } else if (screen === 'home') {
    const activeMembership = activeGroupId
      ? switchItems.find((membership) => membership.groupId === activeGroupId) ?? null
      : null;
    const currentSpaceName = activeMembership?.groupName ?? 'My place';

    screenContent = (
      <HomeScreen
        token={token}
        spaceName={currentSpaceName}
        canSwitchSpaces={switchItems.length > 1}
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
        onEconomy={() => {
          setScreen('economy');
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
  appRoot: {
    flex: 1,
  },
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


