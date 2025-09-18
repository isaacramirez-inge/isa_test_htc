package com.isa.transaction.frontend.service;

import com.isa.transaction.frontend.dto.ApiResponse;
import com.isa.transaction.frontend.dto.TransactionRequest;
import com.isa.transaction.frontend.dto.TransactionResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CDI Application-scoped service for making REST calls to the backend API
 */
@ApplicationScoped
public class TransactionRestClient {
    
    private static final Logger LOGGER = Logger.getLogger(TransactionRestClient.class.getName());
    
    // Backend API base URL - can be configured via system property or environment variable
    String baseUrl; // Package-private for testing
    Client restClient; // Package-private for testing
    
    // For end-to-end testing with HttpClient mock
    java.net.http.HttpClient httpClient;
    
    @PostConstruct
    public void init() {
        // Configure base URL - default to localhost for development
        baseUrl = System.getProperty("backend.api.url", 
                  System.getenv().getOrDefault("BACKEND_API_URL", "http://localhost:8080"));
        
        // Handle Docker environment URLs
        if ("http://backend:8080".equals(baseUrl)) {
            baseUrl = "http://host.docker.internal:8080";
            LOGGER.info("Convirtiendo URL Docker backend a host.docker.internal");
        } else if (baseUrl.contains("host.docker.internal")) {
            LOGGER.info("Usando URL Docker host.docker.internal: " + baseUrl);
        }
        
        // Initialize JAX-RS client
        restClient = ClientBuilder.newBuilder()
                .build();
        
        LOGGER.info("TransactionRestClient inicializado con URL base: " + baseUrl);
    }
    
    @PreDestroy
    public void cleanup() {
        if (restClient != null) {
            restClient.close();
        }
    }
    
    /**
     * Submit a transaction to the backend API
     * 
     * @param request the transaction request
     * @return API response containing transaction result
     */
    public ApiResponse<TransactionResponse> submitTransaction(TransactionRequest request) {
        LOGGER.info("Enviando transaccion: " + request);
        
        try {
            Response response = restClient
                    .target(baseUrl)
                    .path("/api/transactions")
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));
            
            // Check response status
            if (response.getStatus() == 202) { // Accepted
                ApiResponse<TransactionResponse> apiResponse = response.readEntity(
                        new GenericType<ApiResponse<TransactionResponse>>() {});
                LOGGER.info("Transaccion enviada exitosamente: " + apiResponse);
                return apiResponse;
                
            } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
                // Client error - validation or business logic error
                ApiResponse<TransactionResponse> errorResponse = response.readEntity(
                        new GenericType<ApiResponse<TransactionResponse>>() {});
                LOGGER.warning("Error de validacion de transaccion: " + errorResponse.getMessage());
                return errorResponse;
                
            } else {
                // Server error or other unexpected status
                String errorMessage = "Error del servidor: " + response.getStatus() + " - " + response.getStatusInfo();
                LOGGER.severe(errorMessage);
                return createErrorResponse("SERVER_ERROR", errorMessage);
            }
            
        } catch (Exception e) {
            String errorMessage = "Error llamando API backend: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            return createErrorResponse("CONNECTION_ERROR", errorMessage);
        }
    }
    
    /**
     * Get balance for a specific client and account
     * 
     * @param clientIdentification the client identification
     * @param accountNumber the account number
     * @return API response containing balance information
     */
    public ApiResponse<Double> getBalance(String clientIdentification, String accountNumber) {
        //logger.info("Getting balance for client " + clientIdentification + " account " + accountNumber);
        LOGGER.info("Obteniendo saldo para cliente " + clientIdentification + " cuenta " + accountNumber);
        
        try {
            Response response = restClient
                    .target(baseUrl)
                    .path("/api/transactions/balance/{clientIdentification}/{accountNumber}")
                    .resolveTemplate("clientIdentification", clientIdentification)
                    .resolveTemplate("accountNumber", accountNumber)
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == 200) {
                ApiResponse<Double> apiResponse = response.readEntity(
                        new GenericType<ApiResponse<Double>>() {});
                LOGGER.info("Saldo obtenido exitosamente: " + apiResponse);
                return apiResponse;
                
            } else if (response.getStatus() == 404) {
                return createErrorResponse("NOT_FOUND", "Account not found");
                
            } else {
                String errorMessage = "Error del servidor: " + response.getStatus() + " - " + response.getStatusInfo();
                LOGGER.severe(errorMessage);
                return createErrorResponse("SERVER_ERROR", errorMessage);
            }
            
        } catch (Exception e) {
            String errorMessage = "Error llamando API backend: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            return createErrorResponse("CONNECTION_ERROR", errorMessage);
        }
    }
    
    /**
     * Check if the backend API is healthy
     * 
     * @return true if backend is responsive, false otherwise
     */
    public boolean isBackendHealthy() {
        try {
            LOGGER.info("Verificando salud del backend en: " + baseUrl + "/api/transactions/health");
            
            Response response = restClient
                    .target(baseUrl)
                    .path("/api/transactions/health")
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            boolean isHealthy = response.getStatus() == 200;
            LOGGER.info("Respuesta de verificacion de salud del backend: " + response.getStatus() + " - " + (isHealthy ? "OK" : "FALLO"));
            
            return isHealthy;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Verificacion de salud del backend fallo: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Helper method to create error responses
     */
    private <T> ApiResponse<T> createErrorResponse(String code, String message) {
        return ApiResponse.error(message, code);
    }
    
    // Getter for base URL (for testing/debugging)
    public String getBaseUrl() {
        return baseUrl;
    }
}