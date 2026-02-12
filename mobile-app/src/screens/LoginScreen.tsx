import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { devLogin } from '../features/auth/api/devLoginApi';
import { setToken } from '../features/auth/utils/tokenStore';

type Props = {
  onLoggedIn: (token: string) => void;
};

export function LoginScreen({ onLoggedIn }: Props) {
  const [email, setEmail] = useState('');

  async function handleLogin() {
    if (!email.trim()) {
      return;
    }
    const response = await devLogin(email.trim());
    await setToken(response.token);
    onLoggedIn(response.token);
  }

  return (
    <View>
      <Text>Dev login email:</Text>
      <TextInput value={email} onChangeText={setEmail} />
      <Button title="Login" onPress={handleLogin} />
    </View>
  );
}
