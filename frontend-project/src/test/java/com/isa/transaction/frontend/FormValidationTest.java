package com.isa.transaction.frontend;

import com.isa.transaction.frontend.bean.TransactionBean;
import com.isa.transaction.frontend.validator.AccountNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.faces.validator.ValidatorException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Prueba de validación de formularios y binding de datos en Managed Beans
 * 
 * Esta prueba verifica:
 * - La validación correcta de campos del formulario
 * - El binding bidireccional de datos en el Managed Bean
 * - La gestión del estado del formulario
 */
@DisplayName("Validacion de Formularios y Binding de Datos")
class FormValidationTest {

    private TransactionBean transactionBean;
    private AccountNumberValidator validator;

    @BeforeEach
    void setUp() {
        transactionBean = new TransactionBean();
        transactionBean.init();
        validator = new AccountNumberValidator();
    }

    @Test
    @DisplayName("Debe validar correctamente los campos del formulario")
    void shouldValidateFormFields() {
        // Given - Datos vlidos
        String validClientId = "12345678";
        String validAccountNumber = "ACC-123456";
        BigDecimal validAmount = new BigDecimal("100.50");

        // When - Asignar datos al bean 
        transactionBean.setClientIdentification(validClientId);
        transactionBean.setAccountNumber(validAccountNumber);
        transactionBean.setAmount(validAmount);

        // Then - Verificar binding correcto
        assertThat(transactionBean.getClientIdentification()).isEqualTo(validClientId);
        assertThat(transactionBean.getAccountNumber()).isEqualTo(validAccountNumber);
        assertThat(transactionBean.getAmount()).isEqualTo(validAmount);

        // And - Verificar tipo de transacción calculado correctamente
        assertThat(transactionBean.getTransactionType()).isEqualTo("DEPOSITO");
        assertThat(transactionBean.getTransactionTypeText()).isEqualTo("Crédito (Depósito)");
    }

    @Test
    @DisplayName("Debe validar numero de cuenta usando el validator")
    void shouldValidateAccountNumber() {
        // Test 1: Número de cuenta válido
        String validAccount = "ACC-123456";
        assertThatNoException().isThrownBy(() -> {
            validator.validate(null, null, validAccount);
        });

        // Test 2: Número de cuenta muy corto (inválido)
        String shortAccount = "123";
        assertThatExceptionOfType(ValidatorException.class)
            .isThrownBy(() -> {
                validator.validate(null, null, shortAccount);
            });

        // Test 3: Número de cuenta muy largo (inválido)
        String longAccount = "ACCOUNT-123456789012345678901234567890";
        assertThatExceptionOfType(ValidatorException.class)
            .isThrownBy(() -> {
                validator.validate(null, null, longAccount);
            });
        
        // Test 4: Número de cuenta null (permitido por el validator)
        assertThatNoException().isThrownBy(() -> {
            validator.validate(null, null, null);
        });
        
        // Test 5: Número de cuenta vacío (permitido por el validator)
        assertThatNoException().isThrownBy(() -> {
            validator.validate(null, null, "");
        });
    }

    @Test
    @DisplayName("Debe manejar estados del formulario correctamente")
    void shouldHandleFormStates() {
        // Given - Configurar datos iniciales
        transactionBean.setClientIdentification("CLIENT-001");
        transactionBean.setAccountNumber("ACC-001");
        transactionBean.setAmount(new BigDecimal("200.00"));
        transactionBean.setProcessing(true);
        transactionBean.setShowResult(true);
        transactionBean.setTransactionSuccess(true);

        // When - Limpiar formulario
        transactionBean.clearForm();

        // Then - Verificar limpieza selectiva (los datos del cliente se mantienen)
        assertThat(transactionBean.getClientIdentification()).isEqualTo("CLIENT-001"); // Se mantiene
        assertThat(transactionBean.getAccountNumber()).isEqualTo("ACC-001"); // Se mantiene
        assertThat(transactionBean.getAmount()).isNull(); // Se limpia
        assertThat(transactionBean.isProcessing()).isFalse(); // Se resetea
        assertThat(transactionBean.isShowResult()).isFalse(); // Se resetea
        assertThat(transactionBean.isTransactionSuccess()).isFalse(); // Se resetea
    }

    @Test
    @DisplayName("Debe calcular tipos de transaccion segun el monto")
    void shouldCalculateTransactionTypes() {
        // Test 1: Depósito (monto positivo)
        transactionBean.setAmount(new BigDecimal("100.00"));
        assertThat(transactionBean.getTransactionType()).isEqualTo("DEPOSITO");
        assertThat(transactionBean.getTransactionTypeText()).isEqualTo("Crédito (Depósito)");

        // Test 2: Retiro (monto negativo)
        transactionBean.setAmount(new BigDecimal("-50.00"));
        assertThat(transactionBean.getTransactionType()).isEqualTo("RETIRO");
        assertThat(transactionBean.getTransactionTypeText()).isEqualTo("Débito (Retiro)");

        // Test 3: Monto cero
        transactionBean.setAmount(BigDecimal.ZERO);
        assertThat(transactionBean.getTransactionType()).isEqualTo("INVALIDO");
        assertThat(transactionBean.getTransactionTypeText()).isEqualTo("Monto inválido");

        // Test 4: Sin monto
        transactionBean.setAmount(null);
        assertThat(transactionBean.getTransactionType()).isEqualTo("INDEFINIDO");
        assertThat(transactionBean.getTransactionTypeText()).isEqualTo("Ninguna");
    }
}