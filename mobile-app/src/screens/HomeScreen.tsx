import { Button, ScrollView, StyleSheet, Text, View } from 'react-native';
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
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.headerCard}>
        <Text style={styles.title}>Welcome</Text>
        <Text style={styles.subtle}>userId: {me.userId}</Text>
        <Text style={styles.subtle}>householdId: {me.householdId || 'null'}</Text>
      </View>

      <View style={styles.menuCard}>
        <Text style={styles.sectionTitle}>Navigate</Text>
        <View style={styles.buttonRow}>
          <Button title="Todos" onPress={onCreateTodo} />
        </View>
        <View style={styles.buttonRow}>
          <Button title="Meals" onPress={onMeals} />
        </View>
        <View style={styles.buttonRow}>
          <Button title="Shopping" onPress={onCreateShopping} />
        </View>
        <View style={styles.buttonRow}>
          <Button title="Household members" onPress={onManageMembers} />
        </View>
      </View>

      <View style={styles.footerCard}>
        <Button title="Logout" onPress={onLogout} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 12,
    backgroundColor: '#f6f5f2',
  },
  headerCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: '#e7e1d7',
    gap: 6,
  },
  menuCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: '#e7e1d7',
  },
  footerCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: '#e7e1d7',
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    color: '#1e1c16',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1e1c16',
    marginBottom: 8,
  },
  subtle: {
    color: '#6f675b',
  },
  buttonRow: {
    marginBottom: 8,
  },
});
