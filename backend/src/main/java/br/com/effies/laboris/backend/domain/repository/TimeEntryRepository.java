package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    Optional<TimeEntry> findTopByEmployee_IdOrderByEntryTimestampDesc(UUID userId);
}
