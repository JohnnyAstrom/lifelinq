import { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { getToken } from '../features/auth/utils/tokenStore';
import { HomeScreen } from '../screens/HomeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { CreateTodoScreen } from '../screens/CreateTodoScreen';

type Screen = 'login' | 'home' | 'create';

export default function App() {
  const [token, setTokenState] = useState<string | null>(null);
  const [screen, setScreen] = useState<Screen>('login');

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
