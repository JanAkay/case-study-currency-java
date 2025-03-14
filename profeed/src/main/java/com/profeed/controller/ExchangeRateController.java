package com.profeed.controller;

import com.profeed.model.ExchangeRate;
import com.profeed.repository.ExchangeRateRepository;
import com.profeed.dto.PagedResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rates")
public class ExchangeRateController {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateController(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @GetMapping("/{currency}")
    public @ResponseBody ExchangeRate getLatestRate(@PathVariable String currency) {
        ExchangeRate latestRate = exchangeRateRepository.findTopByCurrencyOrderByRateDateDesc(currency);
        System.out.println("API called: /rates/" + currency);
        System.out.println("Returned data: " + latestRate);
        return latestRate;
    }

    @GetMapping
    public PagedResponse<ExchangeRate> getFilteredRates(
        @RequestParam(name = "rateSource", required = false) String rateSource,
        @RequestParam(name = "currency", required = false) String currency,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        System.out.println("Filter Parameters - Page: " + page + ", Size: " + size + ", Rate Source: " + rateSource + ", Currency: " + currency);

        Page<ExchangeRate> exchangeRates;

        //filters
        if (rateSource != null && currency != null) {
            exchangeRates = exchangeRateRepository.findByRateSourceAndCurrency(rateSource, currency, pageable);
        } else if (rateSource != null) {
            exchangeRates = exchangeRateRepository.findByRateSource(rateSource, pageable);
        } else if (currency != null) {
            exchangeRates = exchangeRateRepository.findByCurrency(currency, pageable);
        } else {
            exchangeRates = exchangeRateRepository.findAll(pageable);
        }

        if (exchangeRates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data found");
        }

        System.out.println("Number of entries in db: " + exchangeRates.getTotalElements());

        return new PagedResponse<>(
            exchangeRates.getContent(),
            exchangeRates.getNumber(),
            exchangeRates.getSize(),
            exchangeRates.getTotalElements(),
            exchangeRates.getTotalPages()
        );
    }
}


