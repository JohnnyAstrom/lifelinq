import { View } from 'react-native';
import { Subtle } from '../../../shared/ui/components';

type Props = {
  done: number;
  total: number;
  ratio: number;
  styles: Record<string, any>;
  labelFormatter?: (done: number, total: number) => string;
};

export function ProgressBar({
  done,
  total,
  ratio,
  styles,
  labelFormatter = (d, t) => `${d} / ${t} completed`,
}: Props) {
  if (total <= 0) {
    return null;
  }

  return (
    <View style={styles.dailyProgressBlock}>
      <Subtle>{labelFormatter(done, total)}</Subtle>
      <View style={styles.weekProgressTrack}>
        <View style={[styles.weekProgressFill, { width: `${Math.round(ratio * 100)}%` }]} />
      </View>
    </View>
  );
}

