package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, UUID> {
    Optional<SalaryHistory> findTopByUser_IdOrderByEffectiveDateDesc(UUID userId);

    Optional<SalaryHistory> findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(UUID userId, LocalDate date);

    java.util.List<SalaryHistory> findAllByUser_IdOrderByEffectiveDateDesc(UUID userId);
}
