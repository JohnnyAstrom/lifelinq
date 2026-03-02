package app.lifelinq.features.group.api;

public final class CreateLinkInvitationRequest {
    private Long ttlSeconds;
    private Integer maxUses;

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
}
