type MemberLike = {
  userId: string;
  displayName: string | null;
};

export function buildParticipantNameMap(members: MemberLike[]): Record<string, string> {
  const map: Record<string, string> = {};
  for (const member of members) {
    const normalizedName = member.displayName?.trim();
    if (normalizedName) {
      map[member.userId] = normalizedName;
    }
  }
  return map;
}

export function resolveParticipantDisplayName(
  map: Record<string, string>,
  userId: string
): string {
  return map[userId] ?? userId;
}

