import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useEffect, useState } from 'react';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { BottomSheetModalProvider } from '@gorhom/bottom-sheet';
import { KeyboardProvider } from 'react-native-keyboard-controller';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '../shared/auth/AuthContext';
import { setActiveGroup } from '../features/auth/api/meApi';
import { useMe } from '../features/auth/hooks/useMe';
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
import { CreatePlaceScreen } from '../screens/CreatePlaceScreen';
import { SettingsScreen } from '../screens/SettingsScreen';
import { ManagePlaceScreen } from '../screens/ManagePlaceScreen';
import { SpacesScreen } from '../screens/SpacesScreen';
import { AppToast } from '../shared/ui/AppToast';
import { formatApiError } from '../shared/api/client';
import { OverlaySheet } from '../shared/ui/OverlaySheet';
import { AppCard, AppScreen, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Screen =
  | 'login'
  | 'home'
  | 'todos'
  | 'members'
  | 'shopping'
  | 'shopping-detail'
  | 'meals'
  | 'settings'
  | 'createPlace'
  | 'manage-place'
  | 'spaces'
  | 'documents';

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <KeyboardProvider>
          <BottomSheetModalProvider>
            <View style={{ flex: 1 }}>
              <AuthProvider>
                <AppShell />
              </AuthProvider>
            </View>
          </BottomSheetModalProvider>
        </KeyboardProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

function SplashScreen() {
  const strings = {
    title: 'Loading app',
    subtitle: 'Initializing your workspace...',
  };
  return (
    <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
      <AppCard>
        <Text style={textStyles.h3}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
      </AppCard>
    </AppScreen>
  );
}

function AppShell() {
  const { token, isAuthenticated, isInitializing, login, logout, handleApiError } = useAuth();
  const [screen, setScreen] = useState<Screen>('login');
  const [activeShoppingListId, setActiveShoppingListId] = useState<string | null>(null);
  const [switchSheetOpen, setSwitchSheetOpen] = useState(false);
  const [switchingGroupId, setSwitchingGroupId] = useState<string | null>(null);
  const [switchError, setSwitchError] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; key: number } | null>(null);
  const me = useMe(token);
  const strings = {
    loadingProfileTitle: 'Loading your profile',
    loadingProfileSubtitle: 'Please wait a moment.',
    genericErrorTitle: 'Something went wrong',
    unknownScreen: 'Unknown screen',
  };

  useEffect(() => {
    if (isAuthenticated && screen === 'login') {
      setScreen('home');
    }
  }, [isAuthenticated, screen]);

  function showToast(message: string) {
    setToast((prev) => ({ message, key: (prev?.key ?? 0) + 1 }));
  }

  async function switchPlace(groupId: string) {
    if (!token) {
      return;
    }
    await setActiveGroup(token, groupId);
    const updatedMe = await me.reload();
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

  async function handleSwitchPlaceSelection(groupId: string) {
    if (switchingGroupId) {
      return;
    }
    if (groupId === me.data?.activeGroupId) {
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

  if (isInitializing) {
    return (
      <SplashScreen />
    );
  }

  if (!isAuthenticated) {
    // Keep LoginScreen as the top-level screen to avoid flex layout collapse.
    return (
      <LoginScreen
        onLoggedIn={async (value) => {
          await login(value);
          setScreen('home');
        }}
      />
    );
  }

  // Type narrowing for downstream screens: authenticated flow should always have a token.
  if (!token) {
    return (
      <SplashScreen />
    );
  }

  if (me.loading) {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard>
          <Text style={textStyles.h3}>{strings.loadingProfileTitle}</Text>
          <Subtle>{strings.loadingProfileSubtitle}</Subtle>
        </AppCard>
      </AppScreen>
    );
  }

  if (me.error) {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard>
          <Text style={textStyles.h3}>{strings.genericErrorTitle}</Text>
          <Text style={{ color: theme.colors.danger }}>{me.error}</Text>
        </AppCard>
      </AppScreen>
    );
  }

  if (!me.data) {
    return (
      <AppScreen scroll={false} contentStyle={{ justifyContent: 'center' }}>
        <AppCard>
          <Text style={textStyles.h3}>{strings.loadingProfileTitle}</Text>
          <Subtle>{strings.loadingProfileSubtitle}</Subtle>
        </AppCard>
      </AppScreen>
    );
  }

  const hasCompletedProfile =
    !!me.data.firstName?.trim() &&
    !!me.data.lastName?.trim();

  if (!hasCompletedProfile) {
    return (
      <CompleteProfileScreen
        token={token}
        initialFirstName={me.data.firstName}
        initialLastName={me.data.lastName}
        onCompleted={() => {
          me.reload();
        }}
      />
    );
  }

  if (me.data.memberships.length === 0) {
    return (
      <CreateGroupScreen
        token={token}
        onCreated={async () => {
          me.reload();
          setScreen('home');
        }}
      />
    );
  }

  if (me.data.activeGroupId == null) {
    return (
      <SelectActiveGroupScreen
        token={token}
        memberships={me.data.memberships}
        onSelected={() => {
          me.reload();
        }}
      />
    );
  }

  const switchItems = me.data.memberships;
  const activeGroupId = me.data.activeGroupId;
  const shouldRenderSwitchSheet = switchSheetOpen && switchItems.length > 1;
  const switchSheet = shouldRenderSwitchSheet ? (
    <OverlaySheet onClose={() => setSwitchSheetOpen(false)} sheetStyle={styles.switchSheet}>
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
    </OverlaySheet>
  ) : null;

  const toastOverlay = (
    <AppToast
      message={toast?.message ?? null}
      toastKey={toast?.key ?? 0}
      onDone={() => setToast(null)}
    />
  );

  if (screen === 'todos') {
    return (
      <TodoListScreen
        token={token}
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  }

  if (screen === 'members') {
    return (
      <GroupDetailsScreen
        token={token}
        me={me.data}
        onDone={() => {
          setScreen('spaces');
        }}
      />
    );
  }

  if (screen === 'shopping') {
    return (
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
  }

  if (screen === 'shopping-detail' && activeShoppingListId) {
    return (
      <ShoppingListDetailScreen
        token={token}
        listId={activeShoppingListId}
        onBack={() => {
          setScreen('shopping');
        }}
      />
    );
  }

  if (screen === 'meals') {
    return (
      <MealsWeekScreen
        token={token}
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  }

  if (screen === 'documents') {
    return (
      <DocumentsScreen
        onDone={() => {
          setScreen('home');
        }}
      />
    );
  }

  if (screen === 'settings') {
    return (
      <>
        <SettingsScreen
          me={me.data}
          onDone={() => {
            setScreen('home');
          }}
          onManagePlace={() => {
            setScreen('manage-place');
          }}
          onSwitchPlace={openSwitchSheet}
          onCreatePlace={() => {
            setScreen('createPlace');
          }}
          onLogout={async () => {
            await logout();
            setScreen('login');
          }}
        />
        {switchSheet}
        {toastOverlay}
      </>
    );
  }

  if (screen === 'createPlace') {
    return (
      <CreatePlaceScreen
        token={token}
        onDone={() => {
          setScreen('settings');
        }}
        onCreated={async (groupId) => {
          await switchPlace(groupId);
          setScreen('settings');
        }}
      />
    );
  }

  if (screen === 'manage-place') {
    return (
      <ManagePlaceScreen
        token={token}
        me={me.data}
        onDone={() => {
          setScreen('settings');
        }}
      />
    );
  }

  if (screen === 'spaces') {
    return (
      <SpacesScreen
        me={me.data}
        onDone={() => {
          setScreen('settings');
        }}
        onOpenSpace={() => {
          setScreen('members');
        }}
      />
    );
  }

  if (screen === 'home') {
    const activeMembership = activeGroupId
      ? switchItems.find((membership) => membership.groupId === activeGroupId) ?? null
      : null;
    const currentSpaceName = activeMembership?.groupName ?? 'My space';

    return (
      <>
        <HomeScreen
          token={token}
          spaceName={currentSpaceName}
          onContextInvalidated={() => {
            me.reload();
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
        {switchSheet}
        {toastOverlay}
      </>
    );
  }

  return (
    <View>
      <Text>{strings.unknownScreen}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  switchSheet: {
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.xl + theme.spacing.xl + theme.spacing.md,
  },
  switchSheetContent: {
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
});
