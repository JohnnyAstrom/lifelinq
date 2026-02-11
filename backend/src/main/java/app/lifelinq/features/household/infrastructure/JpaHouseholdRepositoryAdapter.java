package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import java.util.Optional;
import java.util.UUID;

public final class JpaHouseholdRepositoryAdapter implements HouseholdRepository {
    private final HouseholdJpaRepository householdJpaRepository;
    private final HouseholdMapper mapper;

    public JpaHouseholdRepositoryAdapter(HouseholdJpaRepository householdJpaRepository, HouseholdMapper mapper) {
        if (householdJpaRepository == null) {
            throw new IllegalArgumentException("householdJpaRepository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.householdJpaRepository = householdJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Household household) {
        HouseholdEntity entity = mapper.toEntity(household);
        householdJpaRepository.save(entity);
    }

    @Override
    public Optional<Household> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return householdJpaRepository.findById(id).map(mapper::toDomain);
    }
}
