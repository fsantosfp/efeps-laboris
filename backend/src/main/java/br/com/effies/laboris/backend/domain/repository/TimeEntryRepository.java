package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    Optional<TimeEntry> findTopByEmployee_IdOrderByEntryTimestampDesc(UUID userId);

    List<TimeEntry> findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(UUID userId, Instant start, Instant end);

    @Query("SELECT te FROM TimeEntry te WHERE te.job.id = :jobId AND te.entryTimestamp BETWEEN :start AND :end ORDER BY te.entryTimestamp ASC")
    List<TimeEntry> findAllByJobIdAndPeriod(UUID jobId, Instant start, Instant end);
}
