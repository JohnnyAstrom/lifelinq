import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useShopping } from '../hooks/useShopping';
import { AppButton, AppCard, AppInput, AppScreen, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

export function CreateShoppingItemScreen({ token, onDone }: Props) {
  const [name, setName] = useState('');
  const shopping = useShopping(token);
  const strings = {
    title: 'Create shopping item',
    subtitle: 'Keep the list fresh and accurate.',
    placeholder: 'Coffee beans',
    create: 'Create',
    creating: 'Creating...',
    back: 'Back',
  };

  async function handleCreate() {
    if (!name.trim()) {
      return;
    }
    await shopping.add(name.trim());
    setName('');
    onDone();
  }

  return (
    <AppScreen scroll={false} contentStyle={styles.container}>
      <AppCard style={styles.card}>
        <Text style={textStyles.h2}>{strings.title}</Text>
        <Subtle>{strings.subtitle}</Subtle>
        <AppInput
          value={name}
          onChangeText={setName}
          placeholder={strings.placeholder}
        />
        {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}
        <AppButton
          title={shopping.loading ? strings.creating : strings.create}
          onPress={handleCreate}
          fullWidth
          disabled={shopping.loading}
        />
        <AppButton title={strings.back} onPress={onDone} variant="ghost" fullWidth />
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  card: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
