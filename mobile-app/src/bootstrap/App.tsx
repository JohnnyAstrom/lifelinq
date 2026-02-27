import { Text, View } from 'react-native';
import { useEffect, useState } from 'react';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { BottomSheetModalProvider } from '@gorhom/bottom-sheet';
import { KeyboardProvider } from 'react-native-keyboard-controller';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '../shared/auth/AuthContext';
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
import { SettingsScreen } from '../screens/SettingsScreen';
import { ManagePlaceScreen } from '../screens/ManagePlaceScreen';
import { SpacesScreen } from '../screens/SpacesScreen';
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
  const { token, isAuthenticated, isInitializing, login, logout } = useAuth();
  const [screen, setScreen] = useState<Screen>('login');
  const [activeShoppingListId, setActiveShoppingListId] = useState<string | null>(null);
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
      <SettingsScreen
        me={me.data}
        onDone={() => {
          setScreen('home');
        }}
        onManagePlace={() => {
          setScreen('manage-place');
        }}
        onSwitchPlace={() => {
          setScreen('home');
        }}
        onLogout={async () => {
          await logout();
          setScreen('login');
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
    const currentMe = me.data;
    if (!currentMe) {
      return <SplashScreen />;
    }
    const activeMembership = currentMe.activeGroupId
      ? currentMe.memberships.find((membership) => membership.groupId === currentMe.activeGroupId) ?? null
      : null;
    const currentSpaceName = activeMembership?.groupName ?? 'My space';

    return (
      <HomeScreen
        token={token}
        spaceName={currentSpaceName}
        memberships={currentMe.memberships}
        activeGroupId={currentMe.activeGroupId}
        onSwitchedGroup={() => {
          me.reload();
        }}
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
  }

  return (
    <View>
      <Text>{strings.unknownScreen}</Text>
    </View>
  );
}
