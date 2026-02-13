import { Button, ScrollView, Text, View } from 'react-native';
import type { MeResponse } from '../features/auth/api/meApi';

type Props = {
  token: string;
  me: MeResponse;
  onCreateTodo: () => void;
  onManageMembers: () => void;
  onCreateShopping: () => void;
  onMeals: () => void;
  onLogout: () => void;
};

export function HomeScreen({
  token,
  me,
  onCreateTodo,
  onManageMembers,
  onCreateShopping,
  onMeals,
  onLogout,
}: Props) {
  return (
    <ScrollView>
      <View>
        <Text>Welcome</Text>
        <Text>userId: {me.userId}</Text>
        <Text>householdId: {me.householdId || 'null'}</Text>

        <Button title="Todos" onPress={onCreateTodo} />
        <Button title="Meals" onPress={onMeals} />
        <Button title="Shopping" onPress={onCreateShopping} />
        <Button title="Household members" onPress={onManageMembers} />
        <Button title="Logout" onPress={onLogout} />
      </View>
    </ScrollView>
  );
}
