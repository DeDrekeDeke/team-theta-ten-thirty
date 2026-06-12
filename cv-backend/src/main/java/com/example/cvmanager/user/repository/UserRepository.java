package com.example.cvmanager.user.repository;

import com.example.cvmanager.user.model.UserAccount;
import com.example.cvmanager.user.model.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<UserAccount> findByIdAndDeletedAtIsNull(Long id);

    List<UserAccount> findByDeletedAtIsNull(Sort sort);

    long countByRoleAndDeletedAtIsNull(UserRole role);
}
