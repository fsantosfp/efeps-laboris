package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.Displacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisplacementRepository extends JpaRepository<Displacement, UUID> {

    Optional<Displacement> findByUser_IdAndEndTimestampIsNull(UUID userId);

    @Query("SELECT d FROM Displacement d WHERE d.destinationJob.id = :jobId AND d.startTimestamp BETWEEN :start AND :end ORDER BY d.startTimestamp ASC")
    List<Displacement> findAllByDestinationJobIdAndPeriod(UUID jobId, Instant start, Instant end);

    @Query("SELECT d FROM Displacement d WHERE d.destinationJob.id = :jobId ORDER BY d.startTimestamp ASC")
    List<Displacement> findAllByDestinationJobId(UUID jobId);

    @Query("SELECT d FROM Displacement d WHERE d.user.id = :userId AND d.destinationJob.id = :jobId AND d.startTimestamp BETWEEN :start AND :end ORDER BY d.startTimestamp ASC")
    List<Displacement> findAllByUserIdAndDestinationJobIdAndPeriod(UUID userId, UUID jobId, Instant start, Instant end);
}
