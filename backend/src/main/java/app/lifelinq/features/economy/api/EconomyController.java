package app.lifelinq.features.economy.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.economy.application.CalculateSettlementResult;
import app.lifelinq.features.economy.application.CalculateSettlementUseCase;
import app.lifelinq.features.economy.application.CloseSettlementPeriodResult;
import app.lifelinq.features.economy.application.CloseSettlementPeriodUseCase;
import app.lifelinq.features.economy.application.CreateSettlementTransactionCommand;
import app.lifelinq.features.economy.application.CreateSettlementTransactionUseCase;
import app.lifelinq.features.economy.application.GetActiveSettlementPeriodUseCase;
import app.lifelinq.features.economy.application.ListSettlementTransactionsUseCase;
import app.lifelinq.features.economy.application.SoftDeleteSettlementTransactionUseCase;
import app.lifelinq.features.economy.application.UpdateSettlementStrategyUseCase;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementStrategySnapshot;
import app.lifelinq.features.economy.domain.SettlementStrategyType;
import app.lifelinq.features.economy.domain.SettlementTransaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EconomyController {
    private final GetActiveSettlementPeriodUseCase getActiveSettlementPeriodUseCase;
    private final CloseSettlementPeriodUseCase closeSettlementPeriodUseCase;
    private final ListSettlementTransactionsUseCase listSettlementTransactionsUseCase;
    private final CreateSettlementTransactionUseCase createSettlementTransactionUseCase;
    private final SoftDeleteSettlementTransactionUseCase softDeleteSettlementTransactionUseCase;
    private final CalculateSettlementUseCase calculateSettlementUseCase;
    private final UpdateSettlementStrategyUseCase updateSettlementStrategyUseCase;

    public EconomyController(
            GetActiveSettlementPeriodUseCase getActiveSettlementPeriodUseCase,
            CloseSettlementPeriodUseCase closeSettlementPeriodUseCase,
            ListSettlementTransactionsUseCase listSettlementTransactionsUseCase,
            CreateSettlementTransactionUseCase createSettlementTransactionUseCase,
            SoftDeleteSettlementTransactionUseCase softDeleteSettlementTransactionUseCase,
            CalculateSettlementUseCase calculateSettlementUseCase,
            UpdateSettlementStrategyUseCase updateSettlementStrategyUseCase
    ) {
        this.getActiveSettlementPeriodUseCase = getActiveSettlementPeriodUseCase;
        this.closeSettlementPeriodUseCase = closeSettlementPeriodUseCase;
        this.listSettlementTransactionsUseCase = listSettlementTransactionsUseCase;
        this.createSettlementTransactionUseCase = createSettlementTransactionUseCase;
        this.softDeleteSettlementTransactionUseCase = softDeleteSettlementTransactionUseCase;
        this.calculateSettlementUseCase = calculateSettlementUseCase;
        this.updateSettlementStrategyUseCase = updateSettlementStrategyUseCase;
    }

    @GetMapping("/economy/periods/active")
    public ResponseEntity<?> getActivePeriod() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        Optional<SettlementPeriod> activePeriod = getActiveSettlementPeriodUseCase.execute(context.getGroupId());
        if (activePeriod.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toActivePeriodResponse(activePeriod.get()));
    }

    @PostMapping("/economy/periods/close")
    public ResponseEntity<?> closePeriod() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        CloseSettlementPeriodResult result = closeSettlementPeriodUseCase.execute(context.getGroupId());
        return ResponseEntity.ok(new CloseSettlementPeriodResponse(
                result.closedPeriodId(),
                result.newOpenPeriodId()
        ));
    }

    @GetMapping("/economy/periods/{periodId}/transactions")
    public ResponseEntity<?> listTransactions(
            @PathVariable UUID periodId,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<SettlementTransaction> transactions = listSettlementTransactionsUseCase.execute(periodId, includeDeleted);
        List<SettlementTransactionResponse> response = new ArrayList<>();
        for (SettlementTransaction transaction : transactions) {
            response.add(toSettlementTransactionResponse(transaction));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/economy/periods/{periodId}/transactions")
    public ResponseEntity<?> createTransaction(
            @PathVariable UUID periodId,
            @RequestBody CreateSettlementTransactionRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        var result = createSettlementTransactionUseCase.execute(new CreateSettlementTransactionCommand(
                periodId,
                request == null ? null : request.getAmount(),
                request == null ? null : request.getDescription(),
                context.getUserId(),
                request == null ? null : request.getPaidByUserId(),
                request == null ? null : request.getCategory()
        ));
        // Read from list endpoint source of truth to return full DTO shape.
        SettlementTransaction created = listSettlementTransactionsUseCase.execute(periodId, true).stream()
                .filter(tx -> result.transactionId().equals(tx.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("created transaction not found"));
        return ResponseEntity.status(HttpStatus.CREATED).body(toSettlementTransactionResponse(created));
    }

    @DeleteMapping("/economy/transactions/{transactionId}")
    public ResponseEntity<?> softDeleteTransaction(@PathVariable UUID transactionId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        boolean deleted = softDeleteSettlementTransactionUseCase.execute(transactionId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/economy/periods/{periodId}/settlement")
    public ResponseEntity<?> calculateSettlement(@PathVariable UUID periodId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        CalculateSettlementResult result = calculateSettlementUseCase.execute(periodId);
        List<CalculateSettlementResponse.BalanceItem> balances = result.balances().stream()
                .sorted(Comparator.comparing(item -> item.userId().toString()))
                .map(item -> new CalculateSettlementResponse.BalanceItem(item.userId(), item.amount()))
                .toList();
        List<CalculateSettlementResponse.PaymentItem> payments = result.recommendedPayments().stream()
                .map(item -> new CalculateSettlementResponse.PaymentItem(item.fromUserId(), item.toUserId(), item.amount()))
                .toList();
        return ResponseEntity.ok(new CalculateSettlementResponse(balances, payments));
    }

    @PatchMapping("/economy/periods/{periodId}/strategy")
    public ResponseEntity<?> updateStrategy(
            @PathVariable UUID periodId,
            @RequestBody UpdateSettlementStrategyRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getGroupId() == null || context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        SettlementStrategySnapshot strategySnapshot = toStrategySnapshot(request);
        updateSettlementStrategyUseCase.execute(periodId, strategySnapshot);
        return ResponseEntity.noContent().build();
    }

    private ActiveSettlementPeriodResponse toActivePeriodResponse(SettlementPeriod period) {
        return new ActiveSettlementPeriodResponse(
                period.getId(),
                period.getGroupId(),
                period.getStartDate(),
                period.getEndDate(),
                period.getStatus(),
                period.getStrategySnapshot().getStrategyType(),
                period.getStrategySnapshot().getPercentageShares(),
                period.getParticipants().stream().map(participant -> participant.getUserId()).toList()
        );
    }

    private SettlementTransactionResponse toSettlementTransactionResponse(SettlementTransaction transaction) {
        return new SettlementTransactionResponse(
                transaction.getId(),
                transaction.getPeriodId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCreatedByUserId(),
                transaction.getPaidByUserId(),
                transaction.getCreatedAt(),
                transaction.getDeletedAt(),
                transaction.getCategory()
        );
    }

    private SettlementStrategySnapshot toStrategySnapshot(UpdateSettlementStrategyRequest request) {
        if (request == null || request.getStrategyType() == null) {
            throw new IllegalArgumentException("strategyType must not be null");
        }
        SettlementStrategyType strategyType = request.getStrategyType();
        if (strategyType == SettlementStrategyType.PERCENTAGE_COST) {
            Map<UUID, BigDecimal> shares = request.getPercentageShares();
            return SettlementStrategySnapshot.percentageCost(shares);
        }
        return SettlementStrategySnapshot.of(strategyType);
    }
}
