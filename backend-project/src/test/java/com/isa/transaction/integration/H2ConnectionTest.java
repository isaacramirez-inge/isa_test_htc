package com.isa.transaction.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.*;

/**
 * Prueba simple para verificar la conexión a la base de datos H2
 * 
 * Esta prueba demuestra que:
 * - Se puede establecer una conexión válida a H2
 * - La base de datos está disponible y funcional
 * - Se pueden obtener metadatos de la conexión
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Conexión a Base de Datos")
class H2ConnectionTest {

    private static final Logger log = LoggerFactory.getLogger(H2ConnectionTest.class);

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Debe establecer conexión exitosa con la base de datos")
    void shouldEstablishSuccessfulConnection() throws Exception {
        log.info("=== INICIANDO PRUEBA DE CONEXION A BASE DE DATOS ===");
        
        // When - Obtener conexión a la base de datos
        try (Connection connection = dataSource.getConnection()) {
            
            // Then - Verificar que la conexión es válida
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue(); // Timeout de 5 segundos
            
            log.info("✅ CONEXION ESTABLECIDA EXITOSAMENTE");
            
            // And - Obtener y verificar metadatos de la base de datos
            DatabaseMetaData metaData = connection.getMetaData();
            
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            String url = metaData.getURL();
            String userName = metaData.getUserName();
            
            // Verificar que es realmente H2
            assertThat(databaseProductName).isEqualToIgnoringCase("H2");
            assertThat(url).contains("h2:mem:testdb");
            
            // Logging de información de conexión
            log.info("📋 DETALLES DE LA CONEXION:");
            log.info("   • Base de datos: {}", databaseProductName);
            log.info("   • Versión: {}", databaseProductVersion);
            log.info("   • URL: {}", url);
            log.info("   • Usuario: {}", userName);
            
            // And - Verificar que podemos ejecutar una consulta básica
            try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT 1 as test_value")) {
                assertThat(resultSet.next()).isTrue();
                int testValue = resultSet.getInt("test_value");
                assertThat(testValue).isEqualTo(1);
                
                log.info("✅ CONSULTA DE PRUEBA EJECUTADA CORRECTAMENTE: SELECT 1 = {}", testValue);
            }
            
            log.info("=== PRUEBA DE CONEXION COMPLETADA EXITOSAMENTE ===");
        }
    }

}