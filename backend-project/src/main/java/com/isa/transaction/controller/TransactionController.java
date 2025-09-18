package com.isa.transaction.controller;

import com.isa.transaction.dto.ApiResponse;
import com.isa.transaction.dto.TransactionRequest;
import com.isa.transaction.dto.TransactionResponse;
import com.isa.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Transaction Management", description = "APIs for processing financial transactions")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    @Autowired
    private TransactionService transactionService;
    
    @PostMapping("/transactions")
    @Operation(
        summary = "Process a financial transaction",
        description = "Processes a credit or debit transaction for a client account. " +
                     "If the account doesn't exist, it will be created automatically for the client. " +
                     "Returns immediately with transaction ID and processes asynchronously."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "Transaction accepted and is being processed",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad request - validation errors",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Client not found",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Business rule violation (e.g., insufficient funds)",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransaction(
            @Parameter(description = "Transaction request details", required = true)
            @Valid @RequestBody TransactionRequest request) {
        
        logger.info("Received transaction request for client {} account {} amount {}", 
                   request.getClientIdentification(), request.getAccountNumber(), request.getAmount());
        
        try {
            TransactionResponse response = transactionService.processTransaction(request);
            
            ApiResponse<TransactionResponse> apiResponse = ApiResponse.accepted(response, 
                "Transaction has been accepted and is being processed");
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
            
        } catch (Exception e) {
            // Exception handling is delegated to the GlobalExceptionHandler
            throw e;
        }
    }
    
    @GetMapping("/transactions/health")
    @Operation(
        summary = "Health check for transaction service",
        description = "Returns the health status of the transaction processing service"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Service is healthy",
        content = @Content(mediaType = "application/json", 
                          schema = @Schema(implementation = ApiResponse.class))
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Transaction service is healthy"));
    }
    
    @GetMapping("/transactions/balance/{clientIdentification}/{accountNumber}")
    @Operation(
        summary = "Get current balance for an account",
        description = "Retrieves the current balance for a specific client account using client identification"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Balance retrieved successfully",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Account not found",
            content = @Content(mediaType = "application/json", 
                              schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getBalance(
            @Parameter(description = "Client identification", required = true, example = "12345678")
            @PathVariable String clientIdentification,
            @Parameter(description = "Account number", required = true, example = "ACC-123456")
            @PathVariable String accountNumber) {
        
        logger.debug("Getting balance for client {} account {}", clientIdentification, accountNumber);
        
        var balance = transactionService.getBalance(clientIdentification, accountNumber);
        
        if (balance != null) {
            return ResponseEntity.ok(ApiResponse.success(balance.getCurrentBalance(), "Balance retrieved successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Account not found for client " + clientIdentification + " and account " + accountNumber));
        }
    }
}