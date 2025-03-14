package com.profeed.repository;

import com.profeed.model.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    List<ExchangeRate> findByCurrency(String currency);

    Page<ExchangeRate> findByCurrencyAndRateSourceAndRateDateBetween(
        String currency, 
        String rateSource, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );

    Page<ExchangeRate> findByCurrencyAndRateSource(
        String currency, 
        String rateSource, 
        Pageable pageable
    );

    Page<ExchangeRate> findByRateSource(
        String rateSource, 
        Pageable pageable
    );

    Page<ExchangeRate> findByCurrency(
        String currency, 
        Pageable pageable
    );

    Page<ExchangeRate> findByRateDateBetween(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );

    ExchangeRate findTopByCurrencyOrderByRateDateDesc(String currency);

    Page<ExchangeRate> findByRateSourceAndCurrencyAndRateDateBetween(String rateSource, String currency,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ExchangeRate> findByRateSourceAndCurrency(String rateSource, String currency, Pageable pageable);
    
}
