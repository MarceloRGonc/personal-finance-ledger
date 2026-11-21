package com.finances.repository;

import com.finances.domain.LoanEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanEntryRepository extends JpaRepository<LoanEntry, Long> {
    List<LoanEntry> findAllByOrderByLoanNameAscSortOrderAscIdAsc();
}
