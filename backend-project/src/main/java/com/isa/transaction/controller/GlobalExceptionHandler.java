package com.isa.transaction.controller;

import com.isa.transaction.dto.ApiResponse;
import com.isa.transaction.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Maneja la excepcion personalizada TransactionException
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiResponse<Object>> handleTransactionException(TransactionException e) {
        logger.error("Transaction exception: {} [{}]", e.getMessage(), e.getErrorCode());
        
        HttpStatus status = determineHttpStatus(e.getErrorCode());
        ApiResponse<Object> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    // Maneja errores de validacion de las anotaciones @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        logger.error("Validation exception: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        String errorMessage = "Validation failed: " + String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.validationError(errorMessage);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Maneja errores de validacion de las anotaciones @Validated
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("Constraint violation exception: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        
        String errorMessage = "Validation failed: " + String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.validationError(errorMessage);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Maneja excepciones de enlace (errores de enlace de datos de formulario)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e) {
        logger.error("Bind exception: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        String errorMessage = "Data binding failed: " + String.join(", ", errors);
        ApiResponse<Object> response = ApiResponse.validationError(errorMessage);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Maneja el desajuste de tipo de argumento del metodo (ej, se pasa una cadena donde se esperaba un numero)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        logger.error("Type mismatch exception: {}", e.getMessage());
        
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                                          e.getValue(), e.getName(), e.getRequiredType().getSimpleName());
        ApiResponse<Object> response = ApiResponse.badRequest(errorMessage);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // Maneja errores de analisis JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("Message not readable exception: {}", e.getMessage());
        
        String errorMessage = "Invalid JSON format or missing required fields";
        if (e.getMessage().contains("Required request body is missing")) {
            errorMessage = "Request body is required";
        }
        
        ApiResponse<Object> response = ApiResponse.badRequest(errorMessage);
        return ResponseEntity.badRequest().body(response);
    }
    
    // Maneja IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("Illegal argument exception: {}", e.getMessage());
        
        ApiResponse<Object> response = ApiResponse.badRequest(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    // Maneja todas las demas excepciones
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        logger.error("Unexpected exception: ", e);
        
        ApiResponse<Object> response = ApiResponse.internalError("An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    // Determina el codigo de estado HTTP basado en el codigo de error
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "CLIENT_NOT_FOUND", "ACCOUNT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INSUFFICIENT_FUNDS", "ACCOUNT_CREATION_ERROR" -> HttpStatus.CONFLICT;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "SYSTEM_ERROR", "TRANSACTION_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}