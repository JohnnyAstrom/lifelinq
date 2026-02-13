import { Text, View } from 'react-native';
import { useEffect, useState } from 'react';
import { AuthProvider, useAuth } from '../shared/auth/AuthContext';
import { useMe } from '../features/auth/hooks/useMe';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { CreateHouseholdScreen } from '../screens/CreateHouseholdScreen';
import { HouseholdMembersScreen } from '../screens/HouseholdMembersScreen';
import { TodoListScreen } from '../screens/TodoListScreen';
import { MealsWeekScreen } from '../screens/MealsWeekScreen';
import { ShoppingListsScreen } from '../screens/ShoppingListsScreen';
import { ShoppingListDetailScreen } from '../screens/ShoppingListDetailScreen';
import { DocumentsScreen } from '../screens/DocumentsScreen';
import { SettingsScreen } from '../screens/SettingsScreen';
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
  | 'documents';

export default function App() {
  return (
    <AuthProvider>
      <AppShell />
    </AuthProvider>
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
    return <SplashScreen />;
  }

  if (!isAuthenticated) {
    return (
      <View>
        <LoginScreen
          onLoggedIn={async (value) => {
            await login(value);
            setScreen('home');
          }}
        />
      </View>
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

  if (!me.data.householdId) {
    return (
      <CreateHouseholdScreen
        token={token}
        onCreated={async () => {
          me.reload();
          setScreen('home');
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
      <HouseholdMembersScreen
        token={token}
        onDone={() => {
          setScreen('settings');
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
        token={token}
        onDone={() => {
          setScreen('home');
        }}
        onManageMembers={() => {
          setScreen('members');
        }}
        onLogout={async () => {
          await logout();
          setScreen('login');
        }}
      />
    );
  }

  if (screen === 'home') {
    return (
      <HomeScreen
        token={token}
        me={me.data}
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
        onLogout={async () => {
          await logout();
          setScreen('login');
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
