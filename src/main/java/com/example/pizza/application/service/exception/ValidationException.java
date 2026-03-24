package com.example.pizza.application.service.exception;

public class ValidationException extends RuntimeException {
  public ValidationException(String message) {
    super(message);
  }
}
