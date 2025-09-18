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
@Tag(name = "Gestion de Transacciones", description = "APIs para procesar transacciones financieras")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transactions")
    @Operation(
        summary = "Procesar una transaccion financiera",
        description = "Procesa una transaccion de credito o debito para la cuenta de un cliente " +
                     "Si la cuenta no existe se creara automaticamente para el cliente " +
                     "Retorna inmediatamente con el ID de la transaccion y la procesa asincronamente"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "Transaccion aceptada y siendo procesada",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Solicitud incorrecta - errores de validacion",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Cliente no encontrado",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Violacion de reglas de negocio (ej fondos insuficientes)",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransaction(
            @Parameter(description = "Detalles de la solicitud de transaccion", required = true)
            @Valid @RequestBody TransactionRequest request) {

        logger.info("Received transaction request for client {} account {} amount {}",
                   request.getClientIdentification(), request.getAccountNumber(), request.getAmount());

        try {
            TransactionResponse response = transactionService.processTransaction(request);

            ApiResponse<TransactionResponse> apiResponse = ApiResponse.accepted(response,
                "La transaccion ha sido aceptada y esta siendo procesada");

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/transactions/health")
    @Operation(
        summary = "Verificacion de estado del servicio de transacciones",
        description = "Retorna el estado de salud del servicio de procesamiento de transacciones"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "El servicio esta saludable",
        content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = ApiResponse.class))
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("El servicio de transacciones esta saludable"));
    }

    @GetMapping("/transactions/balance/{clientIdentification}/{accountNumber}")
    @Operation(
        summary = "Obtener el saldo actual de una cuenta",
        description = "Recupera el saldo actual de una cuenta de cliente especifica usando la identificacion del cliente"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Saldo recuperado exitosamente",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Cuenta no encontrada",
            content = @Content(mediaType = "application/json",
                              schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getBalance(
            @Parameter(description = "Identificacion del cliente", required = true, example = "12345678")
            @PathVariable String clientIdentification,
            @Parameter(description = "Numero de cuenta", required = true, example = "ACC-123456")
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
