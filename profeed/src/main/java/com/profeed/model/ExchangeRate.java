package com.profeed.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_source")  
    private String rateSource;
    private LocalDateTime rateDate;
    private String currency;
    private double buyPrice;
    private double sellPrice;

    // Default constructor
    public ExchangeRate() {}

    public ExchangeRate(String rateSource, LocalDateTime rateDate, String currency, double buyPrice, double sellPrice) {
        this.rateSource = rateSource;
        this.rateDate = rateDate;
        this.currency = currency;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRateSource() {
        return rateSource;
    }

    public void setRateSource(String rateSource) {
        this.rateSource = rateSource;
    }

    public LocalDateTime getRateDate() {
        return rateDate;
    }

    public void setRateDate(LocalDateTime rateDate) {
        this.rateDate = rateDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "id=" + id +
                ", rateSource='" + rateSource + '\'' +
                ", rateDate=" + rateDate +
                ", currency='" + currency + '\'' +
                ", buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                '}';
    }

    // hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return Double.compare(that.buyPrice, buyPrice) == 0 &&
                Double.compare(that.sellPrice, sellPrice) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(rateSource, that.rateSource) &&
                Objects.equals(rateDate, that.rateDate) &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rateSource, rateDate, currency, buyPrice, sellPrice);
    }
}
