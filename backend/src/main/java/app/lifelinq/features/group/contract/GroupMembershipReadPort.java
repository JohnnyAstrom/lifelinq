package app.lifelinq.features.group.contract;

import java.util.List;
import java.util.UUID;

public interface GroupMembershipReadPort {
    List<UUID> listMemberUserIds(UUID groupId);
}
