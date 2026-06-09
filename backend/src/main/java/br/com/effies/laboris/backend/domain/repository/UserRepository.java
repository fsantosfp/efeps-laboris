package br.com.effies.laboris.backend.domain.repository;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByCompanyIdAndRoleAndStatus(UUID companyId, UserRole role, UserStatus status);

    List<User> findByCompanyIdAndRole(UUID companyId, UserRole role);
}
