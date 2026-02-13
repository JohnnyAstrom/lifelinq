import { useState } from 'react';
import { Button, Text, TextInput, View } from 'react-native';
import { devLogin } from '../features/auth/api/devLoginApi';
import { formatApiError } from '../shared/api/client';

type Props = {
  onLoggedIn: (token: string) => void;
};

export function LoginScreen({ onLoggedIn }: Props) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleLogin() {
    if (!email.trim() || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await devLogin(email.trim());
      onLoggedIn(response.token);
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <View>
      <Text>Dev login email:</Text>
      <TextInput value={email} onChangeText={setEmail} />
      {error ? <Text>{error}</Text> : null}
      <Button title={loading ? 'Logging in...' : 'Login'} onPress={handleLogin} />
    </View>
  );
}
