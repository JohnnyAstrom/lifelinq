package app.lifelinq.features.household.api;

import app.lifelinq.features.household.application.AddMemberToHouseholdCommand;
import app.lifelinq.features.household.application.AddMemberToHouseholdResult;
import app.lifelinq.features.household.application.AddMemberToHouseholdUseCase;
import app.lifelinq.features.household.application.CreateHouseholdCommand;
import app.lifelinq.features.household.application.CreateHouseholdResult;
import app.lifelinq.features.household.application.CreateHouseholdUseCase;
import app.lifelinq.features.household.application.ListHouseholdMembersCommand;
import app.lifelinq.features.household.application.ListHouseholdMembersResult;
import app.lifelinq.features.household.application.ListHouseholdMembersUseCase;
import app.lifelinq.features.household.domain.Membership;
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
    private final CreateHouseholdUseCase createHouseholdUseCase;
    private final AddMemberToHouseholdUseCase addMemberToHouseholdUseCase;
    private final ListHouseholdMembersUseCase listHouseholdMembersUseCase;

    public HouseholdController(
            CreateHouseholdUseCase createHouseholdUseCase,
            AddMemberToHouseholdUseCase addMemberToHouseholdUseCase,
            ListHouseholdMembersUseCase listHouseholdMembersUseCase
    ) {
        this.createHouseholdUseCase = createHouseholdUseCase;
        this.addMemberToHouseholdUseCase = addMemberToHouseholdUseCase;
        this.listHouseholdMembersUseCase = listHouseholdMembersUseCase;
    }

    @PostMapping("/households")
    public CreateHouseholdResponse create(@RequestBody CreateHouseholdRequest request) {
        CreateHouseholdCommand command = new CreateHouseholdCommand(
                request.getName(),
                request.getOwnerUserId()
        );
        CreateHouseholdResult result = createHouseholdUseCase.execute(command);
        return new CreateHouseholdResponse(result.getHouseholdId());
    }

    @PostMapping("/households/{id}/members")
    public AddMemberResponse addMember(
            @PathVariable("id") UUID householdId,
            @RequestBody AddMemberRequest request
    ) {
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(householdId, request.getUserId());
        AddMemberToHouseholdResult result = addMemberToHouseholdUseCase.execute(command);
        return new AddMemberResponse(result.getHouseholdId(), result.getUserId(), result.getRole());
    }

    @GetMapping("/households/{id}/members")
    public ListMembersResponse listMembers(@PathVariable("id") UUID householdId) {
        ListHouseholdMembersResult result = listHouseholdMembersUseCase.execute(
                new ListHouseholdMembersCommand(householdId)
        );
        return new ListMembersResponse(toResponseItems(result.getMembers()));
    }

    private List<MemberItemResponse> toResponseItems(List<Membership> memberships) {
        List<MemberItemResponse> items = new ArrayList<>();
        for (Membership membership : memberships) {
            items.add(new MemberItemResponse(membership.getUserId(), membership.getRole()));
        }
        return items;
    }
}
