import { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { clearToken, getToken } from '../features/auth/utils/tokenStore';
import { useMe } from '../features/auth/hooks/useMe';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { CreateTodoScreen } from '../screens/CreateTodoScreen';
import { CreateHouseholdScreen } from '../screens/CreateHouseholdScreen';
import { HouseholdMembersScreen } from '../screens/HouseholdMembersScreen';
import { CreateShoppingItemScreen } from '../screens/CreateShoppingItemScreen';

type Screen = 'login' | 'home' | 'create' | 'members' | 'shopping';

export default function App() {
  const [token, setTokenState] = useState<string | null>(null);
  const [screen, setScreen] = useState<Screen>('login');
  const [booting, setBooting] = useState(true);
  const me = useMe(token);

  useEffect(() => {
    async function load() {
      const stored = await getToken();
      if (stored) {
        setTokenState(stored);
        setScreen('home');
      }
      setBooting(false);
    }

    load();
  }, []);

  if (booting) {
    return (
      <View>
        <Text>Loading app...</Text>
      </View>
    );
  }

  if (!token) {
    return (
      <View>
        <LoginScreen
          onLoggedIn={(value) => {
            setTokenState(value);
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
          await clearToken();
          setTokenState(null);
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
