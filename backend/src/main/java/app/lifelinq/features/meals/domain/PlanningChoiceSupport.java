package app.lifelinq.features.meals.domain;

import java.util.List;

public record PlanningChoiceSupport(
        PlanningContext context,
        List<ReuseCandidate> recentCandidates,
        List<ReuseCandidate> familiarCandidates,
        List<ReuseCandidate> fallbackCandidates,
        List<ReuseCandidate> makeSoonCandidates
) {
    public PlanningChoiceSupport {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (recentCandidates == null || familiarCandidates == null || fallbackCandidates == null || makeSoonCandidates == null) {
            throw new IllegalArgumentException("candidate lists must not be null");
        }
        recentCandidates = List.copyOf(recentCandidates);
        familiarCandidates = List.copyOf(familiarCandidates);
        fallbackCandidates = List.copyOf(fallbackCandidates);
        makeSoonCandidates = List.copyOf(makeSoonCandidates);
    }
}
