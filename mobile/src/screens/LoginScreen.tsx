import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { setToken } from '../features/auth/utils/tokenStore';

type Props = {
  onLoggedIn: (token: string) => void;
};

export function LoginScreen({ onLoggedIn }: Props) {
  const [token, setTokenInput] = useState('');

  async function handleLogin() {
    if (!token.trim()) {
      return;
    }
    await setToken(token.trim());
    onLoggedIn(token.trim());
  }

  return (
    <View>
      <Text>Paste JWT token:</Text>
      <TextInput value={token} onChangeText={setTokenInput} />
      <Button title="Save token" onPress={handleLogin} />
    </View>
  );
}
