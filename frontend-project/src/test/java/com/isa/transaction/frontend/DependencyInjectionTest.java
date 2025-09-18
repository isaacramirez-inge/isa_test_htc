package com.isa.transaction.frontend;

import com.isa.transaction.frontend.bean.TransactionBean;
import com.isa.transaction.frontend.service.TransactionRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Prueba de inyección de dependencias simplificada
 * 
 * Esta prueba verifica:
 * - La correcta inyección manual de dependencias
 * - La disponibilidad de servicios en Managed Beans
 * - La comunicación entre beans y servicios
 */
@DisplayName("Inyeccion de Dependencias")
class DependencyInjectionTest {

    private TransactionBean transactionBean;
    private TransactionRestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = new TransactionRestClient();
        restClient.init();
        transactionBean = new TransactionBean();
        transactionBean.setRestClient(restClient);
        transactionBean.init();
    }

    @Test
    @DisplayName("Debe crear correctamente el TransactionBean")
    void shouldCreateTransactionBeanCorrectly() {
        // Then- Verificar creación del Managed Bean
        assertThat(transactionBean).isNotNull();
        assertThat(transactionBean.getClass().getName()).contains("TransactionBean");
        
        // And-Verificar estado inicial del bean
        assertThat(transactionBean.getClientIdentification()).isNull();
        assertThat(transactionBean.getAccountNumber()).isNull();
        assertThat(transactionBean.getAmount()).isNull();
        assertThat(transactionBean.isProcessing()).isFalse();
    }

    @Test
    @DisplayName("Debe crear correctamente el servicio TransactionRestClient")
    void shouldCreateRestClientServiceCorrectly() {
        // Then- Verificar creación del servicio
        assertThat(restClient).isNotNull();
        assertThat(restClient.getClass().getName()).contains("TransactionRestClient");
        
        // And- Verificar que el servicio está configurado
        assertThat(restClient.getBaseUrl()).isNotNull();
        assertThat(restClient.getBaseUrl()).contains("8080");
    }

    @Test
    @DisplayName("Debe inyectar el servicio dentro del Managed Bean")
    void shouldInjectServiceIntoManagedBean() {
        // Then  - Verificar que el servicio está inyectado en el bean
        assertThat(transactionBean.getRestClient()).isNotNull();
        assertThat(transactionBean.getRestClient()).isSameAs(restClient);
        
        // And - Verificar que pueden usar métodos del servicio
        String baseUrl = transactionBean.getRestClient().getBaseUrl();
        assertThat(baseUrl).isNotNull();
        assertThat(baseUrl).contains("8080");
    }

    @Test
    @DisplayName("Debe permitir intercambio de dependencias para testing")
    void shouldAllowDependencyReplacementForTesting() {
        // Given- Servicio original inyectado
        TransactionRestClient originalService = transactionBean.getRestClient();
        assertThat(originalService).isNotNull();
        
        // When - Crear servicio de prueba y reemplazar
        TransactionRestClient testService = new TransactionRestClient();
        testService.init();
        transactionBean.setRestClient(testService);
        
        // Then - Verificar que el intercambio funcionó
        assertThat(transactionBean.getRestClient()).isNotSameAs(originalService);
        assertThat(transactionBean.getRestClient()).isSameAs(testService);
        assertThat(transactionBean.getRestClient().getBaseUrl()).isNotNull();
        
        // Cleanup - Restaurar servicio original para no afectar otras pruebas
        transactionBean.setRestClient(originalService);
    }

    @Test
    @DisplayName("Debe funcionar la comunicacion entre bean y servicio")
    void shouldWorkCommunicationBetweenBeanAndService() {
        // Given - Bean con servicio inyectado
        assertThat(transactionBean.getRestClient()).isNotNull();
        
        // When - Usar método del bean que depende del servicio
        boolean isHealthy = transactionBean.isBackendHealthy();
        assertThat(isHealthy).isTrue();
        
        // Then - El método debe ser callable y funcionar correctamente
        // El resultado puede ser true o false dependiendo de si el backend está disponible
        // Lo importante es que no lance NullPointerException
        // Si es true significa que hay un backend corriendo en localhost:8080
        // Si es false significa que no hay backend disponible
        
        // And - Verificar que el servicio sigue disponible
        assertThat(transactionBean.getRestClient().getBaseUrl()).isNotNull();
    }

    @Test
    @DisplayName("Debe validar la disponibilidad de metodos inyectados")
    void shouldValidateInjectedMethodsAvailability() {
        // When - Acceder a métodos del servicio a través del bean
        String baseUrl = transactionBean.getRestClient().getBaseUrl();
        
        // Then - Los métodos deben estar disponibles
        assertThat(baseUrl).isNotNull();
        assertThat(baseUrl).contains("localhost");
        
        // When - Usar métodos del bean que dependen del servicio
        assertThat(transactionBean.getRestClient()).isNotNull();
        
        // Then - La dependencia debe funcionar correctamente
        assertThat(transactionBean.getRestClient().getBaseUrl()).isEqualTo(baseUrl);
    }
}