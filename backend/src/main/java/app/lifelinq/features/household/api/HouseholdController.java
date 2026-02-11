package app.lifelinq.features.household.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.household.application.HouseholdApplicationService;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HouseholdController {
    private final HouseholdApplicationService householdApplicationService;

    public HouseholdController(
            HouseholdApplicationService householdApplicationService
    ) {
        this.householdApplicationService = householdApplicationService;
    }

    @PostMapping("/households")
    public CreateHouseholdResponse create(@RequestBody CreateHouseholdRequest request) {
        return new CreateHouseholdResponse(
                householdApplicationService.createHousehold(request.getName(), request.getOwnerUserId())
        );
    }

    @PostMapping("/household/members")
    public ResponseEntity<?> addMember(
            @RequestBody AddMemberRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        Membership membership = householdApplicationService.addMember(
                context.getHouseholdId(),
                request.getUserId()
        );
        return ResponseEntity.ok(new AddMemberResponse(
                membership.getHouseholdId(),
                membership.getUserId(),
                membership.getRole()
        ));
    }

    @GetMapping("/household/members")
    public ResponseEntity<?> listMembers() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        return ResponseEntity.ok(new ListMembersResponse(toResponseItems(
                householdApplicationService.listMembers(context.getHouseholdId())
        )));
    }

    @PostMapping("/households/invitations/accept")
    public AcceptInvitationResponse acceptInvitation(@RequestBody AcceptInvitationRequest request) {
        MembershipId membershipId = householdApplicationService.acceptInvitation(
                request.getToken(),
                request.getUserId(),
                Instant.now()
        );
        return new AcceptInvitationResponse(membershipId.getHouseholdId(), membershipId.getUserId());
    }

    private List<MemberItemResponse> toResponseItems(List<Membership> memberships) {
        List<MemberItemResponse> items = new ArrayList<>();
        for (Membership membership : memberships) {
            items.add(new MemberItemResponse(membership.getUserId(), membership.getRole()));
        }
        return items;
    }
}
