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
 * Servicio de ambito de aplicacion CDI para realizar llamadas REST a la API del backend
 */
@ApplicationScoped
public class TransactionRestClient {

    private static final Logger LOGGER = Logger.getLogger(TransactionRestClient.class.getName());

    String baseUrl;
    Client restClient;

    java.net.http.HttpClient httpClient;

    @PostConstruct
    public void init() {
        baseUrl = System.getProperty("backend.api.url",
                  System.getenv().getOrDefault("BACKEND_API_URL", "http://localhost:8080"));

        if ("http://backend:8080".equals(baseUrl)) {
            baseUrl = "http://host.docker.internal:8080";
        }

        restClient = ClientBuilder.newBuilder()
                .build();
    }

    @PreDestroy
    public void cleanup() {
        if (restClient != null) {
            restClient.close();
        }
    }

    /**
     * Envia una transaccion a la API del backend
     *
     * @param request la solicitud de transaccion
     * @return respuesta de la API que contiene el resultado de la transaccion
     */
    public ApiResponse<TransactionResponse> submitTransaction(TransactionRequest request) {
        try {
            Response response = restClient
                    .target(baseUrl)
                    .path("/api/transactions")
                    .request(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request));

            if (response.getStatus() == 202) {
                return response.readEntity(
                        new GenericType<ApiResponse<TransactionResponse>>() {});

            } else if (response.getStatus() >= 400 && response.getStatus() < 500) {
                return response.readEntity(
                        new GenericType<ApiResponse<TransactionResponse>>() {});

            } else {
                return createErrorResponse("SERVER_ERROR", "Error del servidor: " + response.getStatus() + " - " + response.getStatusInfo());
            }

        } catch (Exception e) {
            return createErrorResponse("CONNECTION_ERROR", "Error llamando API backend: " + e.getMessage());
        }
    }

    /**
     * Obtiene el saldo de un cliente y cuenta especificos
     *
     * @param clientIdentification la identificacion del cliente
     * @param accountNumber el numero de cuenta
     * @return respuesta de la API que contiene la informacion del saldo
     */
    public ApiResponse<Double> getBalance(String clientIdentification, String accountNumber) {
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
                return response.readEntity(
                        new GenericType<ApiResponse<Double>>() {});

            } else if (response.getStatus() == 404) {
                return createErrorResponse("NOT_FOUND", "Account not found");

            } else {
                return createErrorResponse("SERVER_ERROR", "Error del servidor: " + response.getStatus() + " - " + response.getStatusInfo());
            }

        } catch (Exception e) {
            return createErrorResponse("CONNECTION_ERROR", "Error llamando API backend: " + e.getMessage());
        }
    }

    /**
     * Verifica si la API del backend esta saludable
     *
     * @return verdadero si el backend responde, falso en caso contrario
     */
    public boolean isBackendHealthy() {
        try {
            Response response = restClient
                    .target(baseUrl)
                    .path("/api/transactions/health")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            return response.getStatus() == 200;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Metodo auxiliar para crear respuestas de error
     */
    private <T> ApiResponse<T> createErrorResponse(String code, String message) {
        return ApiResponse.error(message, code);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
