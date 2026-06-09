package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    List<TimeEntry> findAllByJobIdOrderByEntryTimestampAsc(UUID jobId);

    @Query("SELECT te FROM TimeEntry te WHERE te.employee.id IN :employeeIds AND te.payrollId IS NULL AND te.entryTimestamp BETWEEN :start AND :end")
    List<TimeEntry> findOpenEntriesForEmployeesInPeriod(List<UUID> employeeIds, Instant start, Instant end);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TimeEntry te SET te.payrollId = :payrollId WHERE te.id IN :entryIds")
    void updatePayrollIdForEntries(List<UUID> entryIds, UUID payrollId);
}
