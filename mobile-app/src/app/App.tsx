import { Text, View } from 'react-native';
import { useEffect, useState } from 'react';
import { AuthProvider, useAuth } from '../shared/auth/AuthContext';
import { useMe } from '../features/auth/hooks/useMe';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { CreateTodoScreen } from '../screens/CreateTodoScreen';
import { CreateHouseholdScreen } from '../screens/CreateHouseholdScreen';
import { HouseholdMembersScreen } from '../screens/HouseholdMembersScreen';
import { CreateShoppingItemScreen } from '../screens/CreateShoppingItemScreen';

type Screen = 'login' | 'home' | 'create' | 'members' | 'shopping';

export default function App() {
  return (
    <AuthProvider>
      <AppShell />
    </AuthProvider>
  );
}

function SplashScreen() {
  return null;
}

function AppShell() {
  const { token, isAuthenticated, isInitializing, login, logout } = useAuth();
  const [screen, setScreen] = useState<Screen>('login');
  const me = useMe(token);

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
      <View>
        <Text>Loading /me...</Text>
      </View>
    );
  }

  if (me.error) {
    return (
      <View>
        <Text>{me.error}</Text>
      </View>
    );
  }

  if (!me.data) {
    return (
      <View>
        <Text>Loading /me...</Text>
      </View>
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

  if (screen === 'create') {
    return (
      <CreateTodoScreen
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
          setScreen('home');
        }}
      />
    );
  }

  if (screen === 'shopping') {
    return (
      <CreateShoppingItemScreen
        token={token}
        onDone={() => {
          setScreen('home');
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
          setScreen('create');
        }}
        onManageMembers={() => {
          setScreen('members');
        }}
        onCreateShopping={() => {
          setScreen('shopping');
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
      <Text>Unknown screen</Text>
    </View>
  );
}
