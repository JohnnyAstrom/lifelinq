package app.lifelinq.features.meals.domain;

import java.time.LocalDate;

public record ReuseCandidate(
        ReuseCandidateFamily family,
        MealIdentity identity,
        LocalDate lastPlannedDate,
        int totalOccurrences,
        boolean recent,
        boolean frequent,
        boolean familiar,
        boolean fallback,
        boolean slotFit,
        boolean preferenceFit,
        boolean deprioritized,
        boolean makeSoon,
        String surfacedBecause
) {
    public ReuseCandidate {
        if (family == null) {
            throw new IllegalArgumentException("family must not be null");
        }
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        if (lastPlannedDate == null) {
            throw new IllegalArgumentException("lastPlannedDate must not be null");
        }
        if (totalOccurrences < 0) {
            throw new IllegalArgumentException("totalOccurrences must not be negative");
        }
        if (surfacedBecause == null || surfacedBecause.isBlank()) {
            throw new IllegalArgumentException("surfacedBecause must not be blank");
        }
        surfacedBecause = surfacedBecause.trim();
    }
}
