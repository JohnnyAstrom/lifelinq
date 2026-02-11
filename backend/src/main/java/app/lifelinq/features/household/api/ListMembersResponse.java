package app.lifelinq.features.household.api;

import java.util.List;

public final class ListMembersResponse {
    private final List<MemberItemResponse> members;

    public ListMembersResponse(List<MemberItemResponse> members) {
        this.members = members;
    }

    public List<MemberItemResponse> getMembers() {
        return members;
    }
}
