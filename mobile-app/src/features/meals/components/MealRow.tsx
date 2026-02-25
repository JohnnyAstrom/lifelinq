import { Pressable, Text, View } from 'react-native';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type Props = {
  mealType: MealType;
  recipeTitle: string;
  onPress: () => void;
  mealTypeLabels: Record<MealType, string>;
  styles: Record<string, any>;
};

export function MealRow({
  mealType,
  recipeTitle,
  onPress,
  mealTypeLabels,
  styles,
}: Props) {
  return (
    <Pressable
      style={styles.mealRow}
      onPress={onPress}
    >
      <View style={[styles.mealTypeBadge, styles[`mealType_${mealType}`]]}>
        <Text style={styles.mealTypeText}>{mealTypeLabels[mealType]}</Text>
      </View>
      <Text style={styles.mealTitle}>{recipeTitle}</Text>
    </Pressable>
  );
}
