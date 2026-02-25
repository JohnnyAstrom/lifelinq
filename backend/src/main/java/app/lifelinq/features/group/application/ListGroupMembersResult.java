package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Membership;
import java.util.List;

public final class ListGroupMembersResult {
    private final List<Membership> members;

    public ListGroupMembersResult(List<Membership> members) {
        this.members = members;
    }

    public List<Membership> getMembers() {
        return members;
    }
}
