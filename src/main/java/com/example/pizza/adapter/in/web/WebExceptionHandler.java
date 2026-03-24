package com.example.pizza.adapter.in.web;

import com.example.pizza.application.service.exception.BusinessRuleViolationException;
import com.example.pizza.application.service.exception.NotFoundException;
import com.example.pizza.application.service.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;

@RestControllerAdvice
public class WebExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiError> handleValidation(ValidationException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI()));
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ApiError> handleBusiness(BusinessRuleViolationException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequestURI()));
  }

  public record ApiError(
      OffsetDateTime timestamp,
      int status,
      String error,
      String message,
      String path
  ) {
    static ApiError of(HttpStatus status, String message, String path) {
      return new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
  }

  // FAQ:
  // Q: Почему exceptions из application, а не ResponseStatusException?
  // A: Чтобы application не зависел от Spring. Адаптер переводит в HTTP.
  // Q: Почему 409 на нарушение бизнес-правила?
  // A: Это конфликт состояния ресурса. Важно: решает web слой, не domain/application.
  // Q: Почему общий handler(Exception)?
  // A: Чтобы не сливать stacktrace наружу и дать стабильный контракт ошибок.
}
