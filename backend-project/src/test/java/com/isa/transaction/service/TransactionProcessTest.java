package com.isa.transaction.service;

import com.isa.transaction.dto.TransactionRequest;
import com.isa.transaction.dto.TransactionResponse;
import com.isa.transaction.entity.Balance;
import com.isa.transaction.entity.BalanceTransaction;
import com.isa.transaction.entity.Client;
import com.isa.transaction.exception.TransactionException;
import com.isa.transaction.repository.BalanceRepository;
import com.isa.transaction.repository.BalanceTransactionRepository;
import com.isa.transaction.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Prueba unitaria para el proceso completo de acreditacion y debito de transacciones
 * 
 * Esta prueba verifica:
 * - El proceso completo de acreditacion (depositos)
 * - El proceso completo de debito (retiros) 
 * - La validacion de fondos suficientes
 * - La creacion automatica de clientes y cuentas
 * - El manejo de errores y excepciones
 * - La integridad de los calculos de balance
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Proceso de Acreditacion y Debito de Transacciones")
class TransactionProcessTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private BalanceTransactionRepository balanceTransactionRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private TransactionService transactionService;

    private Client existingClient;
    private Balance existingBalance;
    private TransactionRequest creditRequest;
    private TransactionRequest debitRequest;

    @BeforeEach
    void setUp() {
        // Cliente existente
        existingClient = new Client();
        existingClient.setId(1L);
        existingClient.setName("Isaac");
        existingClient.setLastname("Ramirez");
        existingClient.setClientIdentification("12345678");
        existingClient.setEmail("isaac@test.com");
        existingClient.setBirthday(LocalDate.of(1990, 1, 1));

        // Balance existente con $1000
        existingBalance = new Balance("ACC-123456", new BigDecimal("1000.00"), existingClient);
        existingBalance.setId(1L);

        // Solicitud de credito (deposito) por $500
        creditRequest = new TransactionRequest("12345678", "ACC-123456", new BigDecimal("500.00"));

        // Solicitud de debito (retiro) por $300
        debitRequest = new TransactionRequest("12345678", "ACC-123456", new BigDecimal("-300.00"));
    }

    @Test
    @DisplayName("Debe procesar correctamente una acreditacion (deposito)")
    void shouldProcessCreditTransactionCorrectly() {
        // Given - Cliente y balance existentes
        when(clientRepository.findByClientIdentification("12345678")).thenReturn(Optional.of(existingClient));
        when(balanceRepository.findByClientAndAccountNumber(existingClient, "ACC-123456"))
            .thenReturn(Optional.of(existingBalance));
        when(balanceRepository.save(any(Balance.class))).thenReturn(existingBalance);
        when(balanceTransactionRepository.save(any(BalanceTransaction.class)))
            .thenReturn(new BalanceTransaction());

        // When - Procesar transaccion de credito
        TransactionResponse response = transactionService.processTransaction(creditRequest);

        // Then - Verificar respuesta exitosa
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getTransactionId()).isNotNull();
        assertThat(response.getTransactionId()).startsWith("txn_");

        // And - Verificar que se actualizo el balance correctamente
        ArgumentCaptor<Balance> balanceCaptor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(balanceCaptor.capture());
        
        Balance savedBalance = balanceCaptor.getValue();
        assertThat(savedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00")); // 1000 + 500

        // And - Verificar que se guardo el registro de transaccion
        ArgumentCaptor<BalanceTransaction> transactionCaptor = ArgumentCaptor.forClass(BalanceTransaction.class);
        verify(balanceTransactionRepository).save(transactionCaptor.capture());
        
        BalanceTransaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(savedTransaction.getBalanceBefore()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(savedTransaction.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(savedTransaction.getClientId()).isEqualTo(1L);
        assertThat(savedTransaction.getAccountNumber()).isEqualTo("ACC-123456");
    }

    @Test
    @DisplayName("Debe procesar correctamente un debito (retiro) con fondos suficientes")
    void shouldProcessDebitTransactionWithSufficientFunds() {
        // Given - Cliente y balance existentes con fondos suficientes
        when(clientRepository.findByClientIdentification("12345678")).thenReturn(Optional.of(existingClient));
        when(balanceRepository.findByClientAndAccountNumber(existingClient, "ACC-123456"))
            .thenReturn(Optional.of(existingBalance));
        when(balanceRepository.save(any(Balance.class))).thenReturn(existingBalance);
        when(balanceTransactionRepository.save(any(BalanceTransaction.class)))
            .thenReturn(new BalanceTransaction());

        // When - Procesar transaccion de debito
        TransactionResponse response = transactionService.processTransaction(debitRequest);

        // Then - Verificar respuesta exitosa
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getTransactionId()).isNotNull();

        // And - Verificar que se actualizo el balance correctamente
        ArgumentCaptor<Balance> balanceCaptor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository).save(balanceCaptor.capture());
        
        Balance savedBalance = balanceCaptor.getValue();
        assertThat(savedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("700.00")); // 1000 - 300

        // And - Verificar registro de transaccion
        ArgumentCaptor<BalanceTransaction> transactionCaptor = ArgumentCaptor.forClass(BalanceTransaction.class);
        verify(balanceTransactionRepository).save(transactionCaptor.capture());
        
        BalanceTransaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("-300.00"));
        assertThat(savedTransaction.getBalanceBefore()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(savedTransaction.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("Debe rechazar un debito por fondos insuficientes")
    void shouldRejectDebitTransactionWithInsufficientFunds() {
        // Given - Balance existente con solo $1000 pero retiro solicitado de $1500
        TransactionRequest largeDebitRequest = new TransactionRequest("12345678", "ACC-123456", new BigDecimal("-1500.00"));
        
        when(clientRepository.findByClientIdentification("12345678")).thenReturn(Optional.of(existingClient));
        when(balanceRepository.findByClientAndAccountNumber(existingClient, "ACC-123456"))
            .thenReturn(Optional.of(existingBalance));

        // When & Then - Procesar transaccion y verificar excepcion
        assertThatThrownBy(() -> transactionService.processTransaction(largeDebitRequest))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("Insufficient funds")
            .hasMessageContaining("ACC-123456")
            .hasMessageContaining("1500")
            .hasMessageContaining("1000.00");

        // And - Verificar que no se modifica el balance
        verify(balanceRepository, never()).save(any(Balance.class));
        verify(balanceTransactionRepository, never()).save(any(BalanceTransaction.class));
    }

    @Test
    @DisplayName("Debe crear automaticamente un cliente nuevo si no existe")
    void shouldAutomaticallyCreateNewClientIfNotExists() {
        // Given - Cliente que no existe en la base de datos
        String newClientId = "99999999";
        TransactionRequest newClientRequest = new TransactionRequest(newClientId, "ACC-999999", new BigDecimal("1000.00"));
        
        Client newClient = new Client();
        newClient.setId(2L);
        newClient.setName("N/A");
        newClient.setLastname("N/A");
        newClient.setClientIdentification(newClientId);
        
        Balance newBalance = new Balance("ACC-999999", new BigDecimal("1000.00"), newClient);
        newBalance.setId(2L);

        when(clientRepository.findByClientIdentification(newClientId)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(newClient);
        when(balanceRepository.findByClientAndAccountNumber(any(Client.class), eq("ACC-999999")))
            .thenReturn(Optional.empty());
        when(balanceRepository.save(any(Balance.class))).thenReturn(newBalance);
        when(balanceTransactionRepository.save(any(BalanceTransaction.class)))
            .thenReturn(new BalanceTransaction());

        // When - Procesar transaccion para cliente nuevo
        TransactionResponse response = transactionService.processTransaction(newClientRequest);

        // Then - Verificar respuesta exitosa
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");

        // And - Verificar que se creo el cliente
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        
        Client savedClient = clientCaptor.getValue();
        assertThat(savedClient.getClientIdentification()).isEqualTo(newClientId);
        assertThat(savedClient.getName()).isEqualTo("N/A");
        assertThat(savedClient.getLastname()).isEqualTo("N/A");

        // And - Verificar que se creo el balance
        ArgumentCaptor<Balance> balanceCaptor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository, times(2)).save(balanceCaptor.capture()); // Una para crear, otra para actualizar
        
        Balance firstSavedBalance = balanceCaptor.getAllValues().get(0);
        assertThat(firstSavedBalance.getAccountNumber()).isEqualTo("ACC-999999");
        assertThat(firstSavedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Debe crear automaticamente una cuenta nueva para cliente existente")
    void shouldAutomaticallyCreateNewAccountForExistingClient() {
        // Given - Cliente existente pero con cuenta nueva
        String newAccountNumber = "ACC-555555";
        TransactionRequest newAccountRequest = new TransactionRequest("12345678", newAccountNumber, new BigDecimal("750.00"));
        
        Balance newBalance = new Balance(newAccountNumber, new BigDecimal("750.00"), existingClient);
        newBalance.setId(3L);

        when(clientRepository.findByClientIdentification("12345678")).thenReturn(Optional.of(existingClient));
        when(balanceRepository.findByClientAndAccountNumber(existingClient, newAccountNumber))
            .thenReturn(Optional.empty());
        when(balanceRepository.save(any(Balance.class))).thenReturn(newBalance);
        when(balanceTransactionRepository.save(any(BalanceTransaction.class)))
            .thenReturn(new BalanceTransaction());

        // When - Procesar transaccion para cuenta nueva
        TransactionResponse response = transactionService.processTransaction(newAccountRequest);

        // Then - Verificar respuesta exitosa
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");

        // And - Verificar que NO se creo un cliente nuevo
        verify(clientRepository, never()).save(any(Client.class));

        // And - Verificar que se creo la cuenta nueva
        ArgumentCaptor<Balance> balanceCaptor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository, times(2)).save(balanceCaptor.capture()); // Una para crear, otra para actualizar
        
        Balance firstSavedBalance = balanceCaptor.getAllValues().get(0);
        assertThat(firstSavedBalance.getAccountNumber()).isEqualTo(newAccountNumber);
        assertThat(firstSavedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("750.00"));
        assertThat(firstSavedBalance.getClient().getId()).isEqualTo(existingClient.getId());
    }

    @Test
    @DisplayName("Debe calcular correctamente el balance en transacciones consecutivas")
    void shouldCalculateBalanceCorrectlyInConsecutiveTransactions() {
        // Given - Configurar mocks para transacciones consecutivas
        Balance mutableBalance = new Balance("ACC-123456", new BigDecimal("1000.00"), existingClient);
        mutableBalance.setId(1L);
        
        when(clientRepository.findByClientIdentification("12345678")).thenReturn(Optional.of(existingClient));
        when(balanceRepository.findByClientAndAccountNumber(existingClient, "ACC-123456"))
            .thenReturn(Optional.of(mutableBalance));
        
        // Simular el comportamiento de guardado actualizando el balance
        when(balanceRepository.save(any(Balance.class))).thenAnswer(invocation -> {
            Balance balance = invocation.getArgument(0);
            mutableBalance.setCurrentBalance(balance.getCurrentBalance());
            return mutableBalance;
        });
        
        when(balanceTransactionRepository.save(any(BalanceTransaction.class)))
            .thenReturn(new BalanceTransaction());

        // When - Procesar primera transaccion (credito +200)
        TransactionRequest firstCredit = new TransactionRequest("12345678", "ACC-123456", new BigDecimal("200.00"));
        TransactionResponse response1 = transactionService.processTransaction(firstCredit);
        
        // Then - Verificar primera transaccion
        assertThat(response1.getStatus()).isEqualTo("ACCEPTED");
        assertThat(mutableBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1200.00"));

        // When - Procesar segunda transaccion (debito -150)
        TransactionRequest firstDebit = new TransactionRequest("12345678", "ACC-123456", new BigDecimal("-150.00"));
        TransactionResponse response2 = transactionService.processTransaction(firstDebit);
        
        // Then - Verificar segunda transaccion
        assertThat(response2.getStatus()).isEqualTo("ACCEPTED");
        assertThat(mutableBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1050.00"));

        // When - Procesar tercera transaccion (credito +350)
        TransactionRequest secondCredit = new TransactionRequest("12345678", "ACC-123456", new BigDecimal("350.00"));
        TransactionResponse response3 = transactionService.processTransaction(secondCredit);
        
        // Then - Verificar tercera transaccion
        assertThat(response3.getStatus()).isEqualTo("ACCEPTED");
        assertThat(mutableBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1400.00"));

        // And - Verificar que se realizaron todas las operaciones
        verify(balanceRepository, times(3)).save(any(Balance.class));
        verify(balanceTransactionRepository, times(3)).save(any(BalanceTransaction.class));
    }

    @Test
    @DisplayName("Debe manejar errores del sistema y lanzar TransactionException")
    void shouldHandleSystemErrorsAndThrowTransactionException() {
        // Given - Simular error del sistema (RuntimeException)
        when(clientRepository.findByClientIdentification("12345678"))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - Procesar transaccion y verificar excepcion
        assertThatThrownBy(() -> transactionService.processTransaction(creditRequest))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("System error processing transaction")
            .hasCauseInstanceOf(RuntimeException.class);

        // And - Verificar que no se realizaron operaciones de guardado
        verify(balanceRepository, never()).save(any(Balance.class));
        verify(balanceTransactionRepository, never()).save(any(BalanceTransaction.class));
    }
}