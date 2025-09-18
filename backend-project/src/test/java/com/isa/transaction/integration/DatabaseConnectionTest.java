package com.isa.transaction.integration;

import com.isa.transaction.entity.Client;
import com.isa.transaction.entity.Balance;
import com.isa.transaction.repository.ClientRepository;
import com.isa.transaction.repository.BalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Prueba de integracion para verificar la conexion a la base de datos
 * 
 * Esta prueba verifica:
 * - La conectividad con PostgreSQL
 * - Las operaciones basicas de repositorio (CRUD)
 * - La integridad de las entidades y relaciones
 * - Los constraints de base de datos
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Conexion a Base de Datos")
class DatabaseConnectionTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Debe verificar la conexion a la base de datos")
    void shouldVerifyDatabaseConnection() throws Exception {
        // When - Obtener conexion a la base de datos
        Connection connection = dataSource.getConnection();
        
        // Then - Verificar que la conexion es valida
        assertThat(connection).isNotNull();
        assertThat(connection.isValid(2)).isTrue();
        
        // And - Verificar metadatos de la base de datos
        DatabaseMetaData metaData = connection.getMetaData();
        assertThat(metaData.getDatabaseProductName()).isNotNull();
        
        connection.close();
    }

    @Test
    @DisplayName("Debe realizar operaciones CRUD basicas en la entidad Client")
    void shouldPerformBasicCrudOperationsOnClient() {
        // Given - Crear un cliente de prueba
        Client client = new Client();
        client.setName("Isaac");
        client.setLastname("ramirez");
        client.setClientIdentification("12345678");
        client.setEmail("Isaac.ramirez@test.com");
        client.setBirthday(LocalDate.of(1990, 5, 15));

        // When - Guardar el cliente
        Client savedClient = clientRepository.save(client);

        // Then - Verificar que se guardo correctamente
        assertThat(savedClient.getId()).isNotNull();
        assertThat(savedClient.getName()).isEqualTo("Isaac");
        assertThat(savedClient.getClientIdentification()).isEqualTo("12345678");

        // When - Buscar el cliente por identificacion
        Optional<Client> foundClient = clientRepository.findByClientIdentification("12345678");

        // Then - Verificar que se encontro
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getName()).isEqualTo("Isaac");
        assertThat(foundClient.get().getEmail()).isEqualTo("Isaac.ramirez@test.com");

        // When - Actualizar el cliente
        foundClient.get().setName("Isaac Ramirez");
        Client updatedClient = clientRepository.save(foundClient.get());

        // Then - Verificar actualizacion
        assertThat(updatedClient.getName()).isEqualTo("Isaac Ramirez");

        // When - Eliminar el cliente
        clientRepository.delete(updatedClient);

        // Then - Verificar eliminacion
        Optional<Client> deletedClient = clientRepository.findById(updatedClient.getId());
        assertThat(deletedClient).isEmpty();
    }

    @Test
    @DisplayName("Debe realizar operaciones CRUD basicas en la entidad Balance")
    void shouldPerformBasicCrudOperationsOnBalance() {
        // Given - Crear cliente y balance de prueba
        Client client = new Client();
        client.setName("Maria");
        client.setLastname("Garcia");
        client.setClientIdentification("87654321");
        client.setEmail("maria.garcia@test.com");
        client.setBirthday(LocalDate.of(1985, 8, 20));
        client = clientRepository.save(client);

        Balance balance = new Balance("ACC-123456", new BigDecimal("1000.00"), client);

        // When - Guardar el balance
        Balance savedBalance = balanceRepository.save(balance);

        // Then - Verificar que se guardo correctamente
        assertThat(savedBalance.getId()).isNotNull();
        assertThat(savedBalance.getAccountNumber()).isEqualTo("ACC-123456");
        assertThat(savedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(savedBalance.getClient().getId()).isEqualTo(client.getId());

        // When - Buscar balance por cliente y numero de cuenta
        Optional<Balance> foundBalance = balanceRepository.findByClientAndAccountNumber(client, "ACC-123456");

        // Then - Verificar que se encontro
        assertThat(foundBalance).isPresent();
        assertThat(foundBalance.get().getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));

        // When - Actualizar el balance
        foundBalance.get().setCurrentBalance(new BigDecimal("1500.00"));
        Balance updatedBalance = balanceRepository.save(foundBalance.get());

        // Then - Verificar actualizacion
        assertThat(updatedBalance.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Debe validar constraints de integridad referencial")
    void shouldValidateReferentialIntegrityConstraints() {
        // Given - Crear cliente
        Client client = new Client();
        client.setName("Ramirez");
        client.setLastname("Martinez");
        client.setClientIdentification("11223344");
        client.setEmail("Ramirez.martinez@test.com");
        client.setBirthday(LocalDate.of(1992, 3, 10));
        client = clientRepository.save(client);

        // When - Crear dos balances para el mismo cliente con diferente numero de cuenta
        Balance balance1 = new Balance("ACC-111111", new BigDecimal("500.00"), client);
        Balance balance2 = new Balance("ACC-222222", new BigDecimal("800.00"), client);

        Balance savedBalance1 = balanceRepository.save(balance1);
        Balance savedBalance2 = balanceRepository.save(balance2);

        // Then - Ambos balances deben guardarse correctamente
        assertThat(savedBalance1.getId()).isNotNull();
        assertThat(savedBalance2.getId()).isNotNull();
        assertThat(savedBalance1.getClient().getId()).isEqualTo(client.getId());
        assertThat(savedBalance2.getClient().getId()).isEqualTo(client.getId());

        // When - Intentar crear un balance duplicado (mismo cliente + mismo numero de cuenta)
        Balance duplicateBalance = new Balance("ACC-111111", new BigDecimal("300.00"), client);

        // Then - Debe lanzar excepcion por violacion de constraint
        assertThatThrownBy(() -> {
            balanceRepository.save(duplicateBalance);
        }).satisfiesAnyOf(
                throwable -> assertThat(throwable).hasMessageContaining("constraint"),
                throwable -> assertThat(throwable).hasMessageContaining("unique")
            );
    }

    @Test
    @DisplayName("Debe verificar que las entidades tienen timestamps automaticos")
    void shouldVerifyEntitiesHaveAutomaticTimestamps() {
        // Given - Crear cliente
        Client client = new Client();
        client.setName("Ana");
        client.setLastname("Rodriguez");
        client.setClientIdentification("99887766");
        client.setEmail("ana.rodriguez@test.com");
        client.setBirthday(LocalDate.of(1995, 12, 5));

        // When - Guardar el cliente
        Client savedClient = clientRepository.save(client);

        // Then - Verificar que el cliente se guardo correctamente
        // Nota: La entidad Client no tiene timestamps automaticos en esta implementacion
        assertThat(savedClient.getId()).isNotNull();
        assertThat(savedClient.getName()).isEqualTo("Ana");

        // When - Crear balance asociado
        Balance balance = new Balance("ACC-999999", new BigDecimal("2000.00"), savedClient);
        Balance savedBalance = balanceRepository.save(balance);

        // Then - Verificar timestamps del balance
        assertThat(savedBalance.getCreatedAt()).isNotNull();
        assertThat(savedBalance.getUpdatedAt()).isNotNull();
        assertThat(savedBalance.getCreatedAt()).isEqualTo(savedBalance.getUpdatedAt());
    }

}