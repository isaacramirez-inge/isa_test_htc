package com.isa.transaction.frontend.bean;

import com.isa.transaction.frontend.dto.ApiResponse;
import com.isa.transaction.frontend.dto.TransactionRequest;
import com.isa.transaction.frontend.dto.TransactionResponse;
import com.isa.transaction.frontend.service.TransactionRestClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Bean administrado de ambito de vista para manejar las operaciones del formulario de transacciones
 * Este bean gestiona el estado y las acciones del formulario de transacciones
 */
@Named("transactionBean")
@ViewScoped
public class TransactionBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TransactionBean.class.getName());
    
    @Inject
    TransactionRestClient restClient; // Paquete-privado para pruebas
    
    // Campos del formulario
    private String clientIdentification;
    private String accountNumber;
    private BigDecimal amount;
    
    // Gestion del estado de la UI
    private boolean processing = false;
    private boolean showResult = false;
    private boolean transactionSuccess = false;
    private String transactionId;
    private String transactionStatus;
    private String errorMessage;
    private TransactionResponse transactionResult;
    
    @PostConstruct
    public void init() {
        LOGGER.info("TransactionBean inicializado");
        clearForm();
    }
    
    /**
     * Procesa la transaccion - valida el formulario y llama a la API
     */
    public void processTransaction() {
        LOGGER.info("=== Procesamiento de transaccion iniciado ===");
        
        try {
            // Nota: No es necesario limpiar los mensajes manualmente - JSF se encarga de esto
            LOGGER.info("Iniciando procesamiento de formulario");
            
            // Valida los campos del formulario
            LOGGER.info("A punto de llamar validateForm()");
            boolean isValid = false;
            try {
                isValid = validateForm();
                LOGGER.info("validateForm() retorno: " + isValid);
            } catch (Exception validationEx) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error en validateForm: " + validationEx.getClass().getName() + " - " + validationEx.getMessage(), validationEx);
                throw new RuntimeException("Error en validación: " + validationEx.getMessage(), validationEx);
            }
            
            if (!isValid) {
                LOGGER.warning("Validacion de formulario fallo");
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                        "Validación", "Por favor corrija los errores en el formulario"));
                return;
            }
            
            LOGGER.info("Validacion de formulario exitosa, llamando submitTransaction");
            try {
                submitTransaction();
                LOGGER.info("submitTransaction() completado exitosamente");
            } catch (Exception submitEx) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error en submitTransaction: " + submitEx.getClass().getName() + " - " + submitEx.getMessage(), submitEx);
                throw new RuntimeException("Error en envío de transacción: " + submitEx.getMessage(), submitEx);
            }
            
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error en processTransaction: " + e.getClass().getName() + " - " + e.getMessage(), e);
            
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Error procesando la transacción: " + errorMsg));
        } finally {
            LOGGER.info("=== Procesamiento de transaccion completado ===");
        }
    }
    
    /**
     * Valida los campos del formulario y muestra errores en linea
     */
    private boolean validateForm() {
        LOGGER.info("Iniciando validacion de formulario");
        LOGGER.info("Client ID: " + clientIdentification);
        LOGGER.info("Account Number: " + accountNumber);
        LOGGER.info("Amount: " + amount);
        
        boolean isValid = true;
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Valida la identificacion del cliente
        if (clientIdentification == null || clientIdentification.trim().isEmpty()) {
            LOGGER.warning("Identificacion de cliente esta vacia");
            context.addMessage("transactionForm:clientId", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "La identificación del cliente es requerida"));
            isValid = false;
        }
        
        // Valida el numero de cuenta
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            LOGGER.warning("Numero de cuenta esta vacio");
            context.addMessage("transactionForm:accountNum", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "El número de cuenta es requerido"));
            isValid = false;
        } else if (accountNumber.trim().length() < 5 || accountNumber.trim().length() > 25) {
            LOGGER.warning("Longitud de numero de cuenta invalida: " + accountNumber.trim().length());
            context.addMessage("transactionForm:accountNum", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "El número de cuenta debe tener entre 5 y 25 caracteres"));
            isValid = false;
        }
        
        // Valida el monto
        if (amount == null) {
            LOGGER.warning("Monto es nulo");
            context.addMessage("transactionForm:amount", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "El monto es requerido"));
            isValid = false;
        } else if (amount.compareTo(BigDecimal.ZERO) == 0) {
            LOGGER.warning("Monto es cero");
            context.addMessage("transactionForm:amount", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "El monto no puede ser cero"));
            isValid = false;
        }
        
        LOGGER.info("Resultado de validacion de formulario: " + isValid);
        return isValid;
    }
    
    /**
     * Envia la transaccion a la API del backend
     */
    private void submitTransaction() {
        LOGGER.info("=== Envio de transaccion iniciado ===");
        LOGGER.info("Enviando transaccion para cliente " + clientIdentification + ", cuenta " + accountNumber + ", monto " + amount);
        
        try {
            processing = true;
            showResult = false;
            
            // Verifica si restClient esta inyectado
            if (restClient == null) {
                LOGGER.severe("RestClient es nulo - fallo la inyeccion CDI");
                throw new IllegalStateException("RestClient no está inicializado");
            }
            
            LOGGER.info("RestClient disponible, URL base: " + restClient.getBaseUrl());
            
            // Crea el DTO de solicitud
            TransactionRequest request = new TransactionRequest(clientIdentification, accountNumber, amount);
            LOGGER.info("DTO de solicitud creado: " + request);
            
            // Llama a la API del backend
            LOGGER.info("Llamando restClient.submitTransaction...");
            ApiResponse<TransactionResponse> response = restClient.submitTransaction(request);
            LOGGER.info("Respuesta recibida: " + response);
            
            if (response.isSuccess() && !response.isError()) {
                // Transaccion exitosa
                transactionSuccess = true;
                transactionId = response.getData().getTransactionId();
                transactionStatus = response.getData().getStatus();
                transactionResult = response.getData();
                
                // Muestra notificacion de exito
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Éxito", response.getMessage() != null ? response.getMessage() : "Transacción procesada exitosamente"));
                        
                LOGGER.info("Transaccion exitosa: " + transactionId);
                
            } else {
                // Transaccion fallida
                transactionSuccess = false;
                errorMessage = response.getMessage();
                
                // Muestra notificacion de error
                FacesMessage.Severity severity = 
                    response.getCode().equals("VALIDATION_ERROR") ? 
                    FacesMessage.SEVERITY_WARN : FacesMessage.SEVERITY_ERROR;
                    
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(severity, "Error", errorMessage));
                    
                LOGGER.warning("Transaccion fallo: " + errorMessage);
            }
            
            showResult = true;
            
        } catch (Exception e) {
            transactionSuccess = false;
            errorMessage = "Error inesperado: " + e.getMessage();
            showResult = true;
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Error inesperado procesando la transacción: " + e.getMessage()));
                    
            LOGGER.severe("Error inesperado enviando transaccion: " + e.getMessage());
            
        } finally {
            processing = false;
        }
    }
    
    /**
     * Limpia solo el campo de monto
     */
    public void clearForm() {
        amount = null;
        
        // Reinicia el estado de la transaccion
        processing = false;
        showResult = false;
        transactionSuccess = false;
        transactionId = null;
        transactionStatus = null;
        errorMessage = null;
        transactionResult = null;
        
        LOGGER.info("Campo monto limpiado");
    }
    
    /**
     * Obtiene el texto del tipo de transaccion basado en el monto actual
     */
    public String getTransactionTypeText() {
        if (amount == null) {
            return "Ninguna";
        } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "Crédito (Depósito)";
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "Débito (Retiro)";
        } else {
            return "Monto inválido";
        }
    }
    
    /**
     * Obtiene el saldo actual para el cliente y la cuenta ingresados
     * Este metodo puede ser llamado via AJAX cuando se ingresan el ID de cliente y el numero de cuenta
     */
    public void checkBalance() {
        if (clientIdentification != null && !clientIdentification.trim().isEmpty() && accountNumber != null && !accountNumber.trim().isEmpty()) {
            try {
                ApiResponse<Double> balanceResponse = restClient.getBalance(clientIdentification, accountNumber);
                
                if (balanceResponse.isSuccess()) {
                    Double currentBalance = balanceResponse.getData();
                    String balanceMessage = String.format("Saldo actual: $%.2f", currentBalance);
                    
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Información", balanceMessage));
                        
                } else if ("NOT_FOUND".equals(balanceResponse.getCode())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Información", 
                            "Cuenta nueva - se creará automáticamente con la primera transacción"));
                }
                
            } catch (Exception e) {
                LOGGER.warning("Error verificando saldo: " + e.getMessage());
                // No mostrar error al usuario ya que esta es informacion opcional
            }
        }
    }
    
    /**
     * Verifica si el backend esta saludable
     */
    public boolean isBackendHealthy() {
        return restClient.isBackendHealthy();
    }
    
    /**
     * Metodo de prueba para mostrar notificacion (para depuracion)
     */
    public void testNotification() {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Prueba", "Esta es una notificación de prueba para verificar que funciona correctamente"));
        LOGGER.info("Test notification sent");
    }
    
    /**
     * Metodo de depuracion - prueba la transaccion con datos fijos
     */
    public void testTransactionWithFixedData() {
        LOGGER.info("=== Testing transaction with fixed data ===");
        
        try {
            // Establece datos de prueba fijos que cumplen con la validacion del backend
            this.clientIdentification = "12345678";
            this.accountNumber = "ACC-123456"; // El backend espera el formato ACC-XXXXXX (10 caracteres)
            this.amount = new BigDecimal("100.50");
            
            LOGGER.info("Set test data - Client: " + clientIdentification + ", Account: " + accountNumber + ", Amount: " + amount);
            
            // Llama a processTransaction directamente
            processTransaction();
            
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error in testTransactionWithFixedData: " + e.getClass().getName() + " - " + e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Error en prueba con datos fijos: " + e.getClass().getSimpleName()));
        }
    }
    
    /**
     * Prueba la conectividad con el backend
     */
    public void testBackendConnection() {
        LOGGER.info("=== Testing backend connection ===");
        
        try {
            if (restClient == null) {
                LOGGER.severe("RestClient is null");
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "RestClient no está inicializado (CDI injection failed)"));
                return;
            }
            
            String baseUrl = restClient.getBaseUrl();
            LOGGER.info("RestClient base URL: " + baseUrl);
            
            boolean isHealthy = restClient.isBackendHealthy();
            LOGGER.info("Backend health check result: " + isHealthy);
            
            if (isHealthy) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Conectividad", "Backend conectado correctamente en " + baseUrl));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "No se puede conectar al backend en " + baseUrl));
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error testing backend connection: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Error probando conectividad: " + e.getMessage()));
        }
    }
    
    public String getClientIdentification() {
        return clientIdentification;
    }
    
    public void setClientIdentification(String clientIdentification) {
        this.clientIdentification = clientIdentification;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public boolean isProcessing() {
        return processing;
    }
    
    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
    
    public boolean isShowResult() {
        return showResult;
    }
    
    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }
    
    public boolean isTransactionSuccess() {
        return transactionSuccess;
    }
    
    public void setTransactionSuccess(boolean transactionSuccess) {
        this.transactionSuccess = transactionSuccess;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getTransactionStatus() {
        return transactionStatus;
    }
    
    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public TransactionResponse getTransactionResult() {
        return transactionResult;
    }
    
    public void setTransactionResult(TransactionResponse transactionResult) {
        this.transactionResult = transactionResult;
    }
    
    /**
     * Getter para restClient (para pruebas)
     */
    public TransactionRestClient getRestClient() {
        return restClient;
    }
    
    /**
     * Setter para restClient (para pruebas)
     */
    public void setRestClient(TransactionRestClient restClient) {
        this.restClient = restClient;
    }
    
    /**
     * Obtiene el tipo de transaccion basado en el monto
     */
    public String getTransactionType() {
        if (amount == null) {
            return "INDEFINIDO";
        } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "DEPOSITO";
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "RETIRO";
        } else {
            return "INVALIDO";
        }
    }
    
    /**
     * Metodo de depuracion - prueba la conectividad con el backend
     */
    public void testBackendConnectivity() {
        testBackendConnection();
    }
    
    /**
     * Metodo de depuracion - procesa con datos fijos
     */
    public void processWithFixedData() {
        LOGGER.info("=== Procesar con datos fijos ===");
        
        // Establece datos de prueba fijos
        this.clientIdentification = "DEBUG-CLIENT";
        this.accountNumber = "DEBUG-ACCOUNT";
        this.amount = new BigDecimal("999.99");
        
        // Procesa la transaccion
        processTransaction();
    }
    
    /**
     * Metodo de depuracion - prueba la notificacion de exito
     */
    public void testSuccessNotification() {
        this.transactionSuccess = true;
        this.showResult = true;
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Éxito", "Prueba de notificación exitosa"));
    }
    
    /**
     * Metodo de depuracion - prueba la notificacion de error
     */
    public void testErrorNotification() {
        this.transactionSuccess = false;
        this.showResult = true;
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error", "Prueba de notificación de error"));
    }
}
