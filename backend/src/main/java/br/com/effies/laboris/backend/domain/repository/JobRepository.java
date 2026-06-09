package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND j.status <> DELETED ORDER BY j.createdAt DESC")
    List<Job> findAllByCompanyId(UUID companyId);
}
