import { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { getToken } from '../features/auth/utils/tokenStore';
import { useMe } from '../features/auth/hooks/useMe';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { CreateTodoScreen } from '../screens/CreateTodoScreen';
import { CreateHouseholdScreen } from '../screens/CreateHouseholdScreen';

type Screen = 'login' | 'home' | 'create';

export default function App() {
  const [token, setTokenState] = useState<string | null>(null);
  const [screen, setScreen] = useState<Screen>('login');
  const me = useMe(token);

  useEffect(() => {
    async function load() {
      const stored = await getToken();
      if (stored) {
        setTokenState(stored);
        setScreen('home');
      }
    }

    load();
  }, []);

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

  if (me.data && !me.data.householdId) {
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

  if (screen === 'home') {
    return (
      <HomeScreen
        token={token}
        me={me.data!}
        onCreateTodo={() => {
          setScreen('create');
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
