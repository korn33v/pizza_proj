package com.example.pizza.application.service.exception;

public class BusinessRuleViolationException extends RuntimeException {
  public BusinessRuleViolationException(String message) {
    super(message);
  }
}
