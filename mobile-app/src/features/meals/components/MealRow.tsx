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
      style={({ pressed }) => [
        styles.mealRow,
        pressed ? styles.mealRowPressed : null,
      ]}
      onPress={onPress}
    >
      <View style={styles.mealRowContent}>
        <View style={[styles.mealTypeBadge, styles[`mealType_${mealType}`]]}>
          <Text style={styles.mealTypeText}>{mealTypeLabels[mealType]}</Text>
        </View>
        <Text style={styles.mealTitle} numberOfLines={1}>
          {recipeTitle}
        </Text>
      </View>
    </Pressable>
  );
}
