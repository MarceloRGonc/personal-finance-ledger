package com.finances.repository;

import com.finances.domain.BankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findAllByOrderBySortOrderAscIdAsc();

    Optional<BankAccount> findByNameIgnoreCase(String name);
}
