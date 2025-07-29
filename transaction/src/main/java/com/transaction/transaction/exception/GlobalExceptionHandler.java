package com.transaction.transaction.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.transaction.transaction.dto.ErrorResponse;
import com.transaction.transaction.service.impl.KafkaLogger;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @Autowired
        private KafkaLogger kafkaLogger;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String errorMessage = "Validation failed: ";
        
        if (!fieldErrors.isEmpty()) {
            errorMessage += fieldErrors.get(0).getDefaultMessage();
        }
        
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", errorMessage);
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(404, "Not Found", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TransactionExecutionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionExecutionException(TransactionExecutionException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(404, "Not Found", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(503, "Service Unavailable", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}