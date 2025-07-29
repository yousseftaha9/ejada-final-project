package com.user.user.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.user.user.dto.ErrorResponse;
import com.user.user.service.impl.KafkaLogger;

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

    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleUserRegistrationException(UserRegistrationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(409, "Conflict", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(404, "Not Found", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(401, "Unauthorized", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", ex.getMessage());
        kafkaLogger.log(errorResponse, "Response");
        return ResponseEntity.internalServerError().body(errorResponse);
    }
} 