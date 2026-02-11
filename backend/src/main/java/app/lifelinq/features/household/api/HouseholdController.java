package app.lifelinq.features.household.api;

import app.lifelinq.features.household.application.AddMemberToHouseholdCommand;
import app.lifelinq.features.household.application.AcceptInvitationCommand;
import app.lifelinq.features.household.application.AcceptInvitationResult;
import app.lifelinq.features.household.application.CreateHouseholdCommand;
import app.lifelinq.features.household.application.CreateHouseholdResult;
import app.lifelinq.features.household.application.ListHouseholdMembersCommand;
import app.lifelinq.features.household.application.ListHouseholdMembersResult;
import app.lifelinq.features.household.application.ListHouseholdMembersUseCase;
import app.lifelinq.features.household.application.HouseholdTransactionalService;
import app.lifelinq.features.household.domain.Membership;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HouseholdController {
    private final ListHouseholdMembersUseCase listHouseholdMembersUseCase;
    private final HouseholdTransactionalService householdTransactionalService;

    public HouseholdController(
            ListHouseholdMembersUseCase listHouseholdMembersUseCase,
            HouseholdTransactionalService householdTransactionalService
    ) {
        this.listHouseholdMembersUseCase = listHouseholdMembersUseCase;
        this.householdTransactionalService = householdTransactionalService;
    }

    @PostMapping("/households")
    public CreateHouseholdResponse create(@RequestBody CreateHouseholdRequest request) {
        CreateHouseholdCommand command = new CreateHouseholdCommand(
                request.getName(),
                request.getOwnerUserId()
        );
        CreateHouseholdResult result = householdTransactionalService.createHousehold(command);
        return new CreateHouseholdResponse(result.getHouseholdId());
    }

    @PostMapping("/households/{id}/members")
    public AddMemberResponse addMember(
            @PathVariable("id") UUID householdId,
            @RequestBody AddMemberRequest request
    ) {
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(householdId, request.getUserId());
        AddMemberToHouseholdResult result = householdTransactionalService.addMember(command);
        return new AddMemberResponse(result.getHouseholdId(), result.getUserId(), result.getRole());
    }

    @GetMapping("/households/{id}/members")
    public ListMembersResponse listMembers(@PathVariable("id") UUID householdId) {
        ListHouseholdMembersResult result = listHouseholdMembersUseCase.execute(
                new ListHouseholdMembersCommand(householdId)
        );
        return new ListMembersResponse(toResponseItems(result.getMembers()));
    }

    @PostMapping("/households/invitations/accept")
    public AcceptInvitationResponse acceptInvitation(@RequestBody AcceptInvitationRequest request) {
        AcceptInvitationCommand command = new AcceptInvitationCommand(
                request.getToken(),
                request.getUserId(),
                Instant.now()
        );
        AcceptInvitationResult result = householdTransactionalService.acceptInvitation(command);
        return new AcceptInvitationResponse(result.getHouseholdId(), result.getUserId());
    }

    private List<MemberItemResponse> toResponseItems(List<Membership> memberships) {
        List<MemberItemResponse> items = new ArrayList<>();
        for (Membership membership : memberships) {
            items.add(new MemberItemResponse(membership.getUserId(), membership.getRole()));
        }
        return items;
    }
}
