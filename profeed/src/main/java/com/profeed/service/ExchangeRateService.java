package com.profeed.service;

import com.profeed.model.ExchangeRate;
import com.profeed.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;

    @Value("${fixer.api.url}")
    private String fixerApiUrl;

    @Value("${fixer.api.key}")
    private String fixerApiKey;

    @Value("${currencylayer.api.url}")
    private String currencyLayerApiUrl;

    @Value("${currencylayer.api.key}")
    private String currencyLayerApiKey;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, RestTemplate restTemplate) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.restTemplate = restTemplate;
    }

    // İlk başlangıçta çalışacak olan metot
    @PostConstruct
    public void fetchInitialExchangeRates() {
        fetchAndSaveExchangeRates();  
    }

    // Her saat başı çalışacak olan metot
    @Scheduled(fixedRate = 3600000) 
    public void fetchAndSaveExchangeRates() {
        fetchAndSaveRatesFromFixer();
        fetchAndSaveRatesFromCurrencyLayer();
    }

    private void fetchAndSaveRatesFromFixer() {
        String url = fixerApiUrl + "?access_key=" + fixerApiKey + "&symbols=USD,TRY,EUR";
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
    
            if (response != null && response.containsKey("rates")) {
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");
    
                if (rates != null) {
                    //Magic
                    processUSDToTryRateFixer(rates);
                    processCurrencyRateFixer(rates, "TRY", "EUR");
                } else {
                    logger.error("Rates is missing in the response from Fixer: " + response);
                }
            } else {
                logger.error("Missing 'rates' field from the Fixer: " + response);
            }
        } catch (Exception e) {
            logger.error("Error fetching exchange rates from Fixer", e);
        }
    }
    
    private void processCurrencyRateFixer(Map<String, Object> rates, String baseCurrency, String rateLabel) {
        Object rateObject = rates.get(baseCurrency);
    
        if (rateObject == null) {
            logger.error("Rate for " + baseCurrency + " is missing in the response: " + rates);
            return;  
        }
    
        try {
            // convert double
            Double rate = getRateAsDouble(rateObject);
            saveExchangeRate("Fixer", rateLabel, rate);
        } catch (Exception e) {
            logger.error("Error processing rate for " + baseCurrency + ": " + rateObject, e);
        }
    }
    
    
    private void processUSDToTryRateFixer(Map<String, Object> rates) {
        Object usdTryObject = rates.get("TRY");
        Object usdEurObject = rates.get("USD");
    
        if (usdTryObject == null || usdEurObject == null) {
            logger.error("Missing exhcnage rate in the response: " + rates);
            return;
        }
    
        try {
            // convert double
            Double usdTryRate = getRateAsDouble(usdTryObject);
            Double usdEurRate = getRateAsDouble(usdEurObject);
    
            if (usdTryRate != null && usdEurRate != null) {
                Double eurTryRate = usdTryRate / usdEurRate;
                saveExchangeRate("Fixer", "USD", eurTryRate);
            } else {
                logger.error("Failed to convert USD/TRY or USD/EUR rates: " + usdTryObject + ", " + usdEurObject);
            }
        } catch (Exception e) {
            logger.error("Error processing USD/TRY conversion", e);
        }
    }
    

    private void fetchAndSaveRatesFromCurrencyLayer() {
        String url = currencyLayerApiUrl + "?access_key=" + currencyLayerApiKey + "&currencies=USD,EUR,TRY";
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
    
            if (response != null && response.containsKey("quotes")) {
                Map<String, Object> quotes = (Map<String, Object>) response.get("quotes");
    
                if (quotes != null) {
                    processCurrencyRate(quotes, "USD", "USD");
                    processCurrencyRate(quotes, "EUR", "EUR");
                    processCurrencyRate(quotes, "TRY", "TRY");
                } else {
                    logger.error("Quotes data missing : " + response);
                }
            } else {
                logger.error("missing quotes field CurrencyLayer: " + response);
            }
        } catch (Exception e) {
            logger.error("Error fetching rates CurrencyLayer ", e);
        }
    }
    
    private void processCurrencyRate(Map<String, Object> quotes, String baseCurrency, String targetCurrency) {

        if ("EUR".equals(targetCurrency)) {
            processEuroToTryRate(quotes);
        } else {
    
            String key = "USD" + targetCurrency; 
            Object rateObject = quotes.get(key);
    
            if (rateObject == null) {
                logger.error("Rate for " + targetCurrency + " is missing in the response: " + quotes);
                return;  
            }
    
            try {
                Double rate = getRateAsDouble(rateObject);
    
                // save
                saveExchangeRate("CurrencyLayer", "USD", rate);
            } catch (Exception e) {
                logger.error("Error processing rate for " + targetCurrency + ": " + rateObject, e);
            }
        }
    }
    
    private void processEuroToTryRate(Map<String, Object> quotes) {
        Object usdEurObject = quotes.get("USDEUR");
        Object usdTryObject = quotes.get("USDTRY");
    
        if (usdEurObject == null || usdTryObject == null) {
            logger.error("Missing USD/EUR or USD/TRY rate in the response: " + quotes);
            return;  
        }
    
        try {
            // convert double
            Double usdEurRate = getRateAsDouble(usdEurObject);
            Double usdTryRate = getRateAsDouble(usdTryObject);
    
            if (usdEurRate != null && usdTryRate != null) {
                Double eurTryRate = usdTryRate / usdEurRate;

                saveExchangeRate("CurrencyLayer", "EUR", eurTryRate);
            } else {
                logger.error("Failed to convert USD/EUR or USD/TRY rates: " + usdEurObject + ", " + usdTryObject);
            }
        } catch (Exception e) {
            logger.error("Error processing EUR/TRY conversion", e);
        }
    }
    

    private double getRateAsDouble(Object rate) {
        if (rate == null) {
            // handle null
            throw new IllegalArgumentException("Rate is null");
        }
        if (rate instanceof Number) {
            return ((Number) rate).doubleValue();
        }
        throw new IllegalArgumentException("Rate is neither Integer nor Double: " + rate);
    }
    

    private void saveExchangeRate(String source, String currency, double rate) {
        ExchangeRate exchangeRate = new ExchangeRate(
                source, LocalDateTime.now(), currency, rate, rate);
        exchangeRateRepository.save(exchangeRate);
        logger.info("Saved exchange rate: {} {} - {} {}", source, currency, rate, LocalDateTime.now());
    }
}
