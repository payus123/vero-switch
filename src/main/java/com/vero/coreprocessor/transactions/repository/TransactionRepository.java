package com.vero.coreprocessor.transactions.repository;

import com.vero.coreprocessor.transactions.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    @Query("SELECT t FROM Transaction t WHERE t.rrn = :rrn AND t.stan = :stan AND t.mti = :mti")
    Optional<Transaction> findUniqueTransaction(@Param("rrn") String rrn,
                                                            @Param("stan") String stan,
                                                            @Param("mti") String mti);

    @Query("SELECT t FROM Transaction t WHERE t.stan = :stan AND t.mti = :mti AND t.tranDateAndTime = :dateTime ")
    Optional<Transaction> findOriginalTransaction(
            @Param("stan") String stan,
            @Param("mti") String mti,
            @Param("dateTime") String dateTime);

}
