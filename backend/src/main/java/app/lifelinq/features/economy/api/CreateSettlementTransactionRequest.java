package app.lifelinq.features.economy.api;

import java.math.BigDecimal;
import java.util.UUID;

public final class CreateSettlementTransactionRequest {
    private BigDecimal amount;
    private String description;
    private UUID paidByUserId;
    private String category;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(UUID paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
