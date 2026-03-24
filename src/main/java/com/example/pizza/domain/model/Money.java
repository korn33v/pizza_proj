package com.example.pizza.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

  public Money {
    Objects.requireNonNull(amount, "amount");
    if (currency == null || currency.isBlank()) {
      throw new IllegalArgumentException("currency must be non-blank");
    }
    amount = amount.setScale(2, RoundingMode.HALF_UP);
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
  }

  public static Money eur(BigDecimal amount) {
    return new Money(amount, "EUR");
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money multiply(int factor) {
    if (factor <= 0) {
      throw new IllegalArgumentException("factor must be > 0");
    }
    return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other");
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("currency mismatch: " + this.currency + " vs " + other.currency);
    }
  }
}
