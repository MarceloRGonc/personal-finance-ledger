package com.finances.repository;

import com.finances.domain.BankBalance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankBalanceRepository extends JpaRepository<BankBalance, Long> {
    Optional<BankBalance> findByAccountIdAndYear(long accountId, int year);

    void deleteByAccountId(long accountId);
}
