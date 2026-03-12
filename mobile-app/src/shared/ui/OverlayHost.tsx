import { useSyncExternalStore } from 'react';
import { StyleSheet, View } from 'react-native';

type OverlayEntry = {
  id: string;
  node: React.ReactNode;
};

const overlays = new Map<string, React.ReactNode>();
const listeners = new Set<() => void>();
let snapshot: OverlayEntry[] = [];
let hostHeight = 0;

function emitChange() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function getSnapshot(): OverlayEntry[] {
  return snapshot;
}

function getHostHeightSnapshot(): number {
  return hostHeight;
}

function rebuildSnapshot() {
  snapshot = Array.from(overlays.entries()).map(([id, node]) => ({ id, node }));
}

export function setOverlayNode(id: string, node: React.ReactNode) {
  overlays.set(id, node);
  rebuildSnapshot();
  emitChange();
}

export function removeOverlayNode(id: string) {
  if (!overlays.has(id)) {
    return;
  }
  overlays.delete(id);
  rebuildSnapshot();
  emitChange();
}

export function setOverlayHostHeight(nextHeight: number) {
  if (hostHeight === nextHeight) {
    return;
  }
  hostHeight = nextHeight;
  emitChange();
}

export function useOverlayHostHeight() {
  return useSyncExternalStore(subscribe, getHostHeightSnapshot);
}

export function OverlayHost() {
  const entries = useSyncExternalStore(subscribe, getSnapshot);

  return (
    <View
      style={styles.host}
      pointerEvents="box-none"
      onLayout={(event) => {
        const nextHeight = event.nativeEvent.layout.height;
        setOverlayHostHeight(nextHeight);
      }}
    >
      {entries.map((entry) => (
        <View key={entry.id} style={StyleSheet.absoluteFill} pointerEvents="box-none">
          {entry.node}
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  host: {
    ...StyleSheet.absoluteFillObject,
  },
});
