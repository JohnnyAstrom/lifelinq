package app.lifelinq.features.group.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.application.GroupMemberView;
import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.group.contract.CreateInvitationOutput;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipId;
import app.lifelinq.features.group.domain.LastAdminRemovalException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupController {
    private final GroupApplicationService groupApplicationService;

    public GroupController(
            GroupApplicationService groupApplicationService
    ) {
        this.groupApplicationService = groupApplicationService;
    }

    @PostMapping("/groups")
    public ResponseEntity<?> create(@RequestBody CreateGroupRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new CreateGroupResponse(
                groupApplicationService.createGroup(request.getName(), context.getUserId())
        ));
    }

    @PostMapping("/groups/members")
    public ResponseEntity<?> addMember(
            @RequestBody AddMemberRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        try {
            Membership membership = groupApplicationService.addMember(
                    context.getGroupId(),
                    context.getUserId(),
                    request.getUserId()
            );
            return ResponseEntity.ok(new AddMemberResponse(
                    membership.getGroupId(),
                    membership.getUserId(),
                    membership.getRole()
            ));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(403).body("Access denied");
        }
    }

    @PostMapping("/groups/members/remove")
    public ResponseEntity<?> removeMember(@RequestBody RemoveMemberRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        try {
            boolean removed = groupApplicationService.removeMember(
                    context.getGroupId(),
                    context.getUserId(),
                    request.getUserId()
            );
            return ResponseEntity.ok(new RemoveMemberResponse(removed));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(403).body("Access denied");
        } catch (LastAdminRemovalException ex) {
            return ResponseEntity.status(409).body("Cannot remove the last admin while other members exist");
        }
    }

    @GetMapping("/groups/members")
    public ResponseEntity<?> listMembers() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new ListMembersResponse(toResponseItems(
                groupApplicationService.listMembers(context.getGroupId())
        )));
    }

    @PostMapping("/groups/invitations/accept")
    public ResponseEntity<?> acceptInvitation(@RequestBody AcceptInvitationRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        MembershipId membershipId = groupApplicationService.acceptInvitation(
                request.getToken(),
                context.getUserId()
        );
        return ResponseEntity.ok(new AcceptInvitationResponse(
                membershipId.getGroupId(),
                membershipId.getUserId()
        ));
    }

    @PostMapping("/groups/invitations")
    public ResponseEntity<?> createInvitation(@RequestBody CreateInvitationRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        Duration ttl = request == null || request.getTtlSeconds() == null
                ? null
                : Duration.ofSeconds(request.getTtlSeconds());
        CreateInvitationOutput result = groupApplicationService.createInvitation(
                context.getGroupId(),
                context.getUserId(),
                request == null ? null : request.getEmail(),
                ttl
        );
        return ResponseEntity.status(201).body(new CreateInvitationResponse(
                result.invitationId(),
                result.token(),
                result.expiresAt()
        ));
    }

    @DeleteMapping("/groups/invitations/{invitationId}")
    public ResponseEntity<?> revokeInvitation(@PathVariable UUID invitationId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        boolean revoked = groupApplicationService.revokeInvitation(
                context.getGroupId(),
                context.getUserId(),
                invitationId
        );
        if (!revoked) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private List<MemberItemResponse> toResponseItems(List<GroupMemberView> memberships) {
        List<MemberItemResponse> items = new ArrayList<>();
        for (GroupMemberView membership : memberships) {
            items.add(new MemberItemResponse(membership.userId(), membership.role(), membership.displayName()));
        }
        return items;
    }
}
