package com.isa.transaction.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.*;

/**
 * Prueba simple y directa de conexión a base de datos H2 en memoria
 * 
 * Esta prueba demuestra que:
 * - Se puede establecer una conexión directa a H2
 * - La base de datos está disponible y funcional
 * - Se pueden ejecutar consultas básicas
 */
@DisplayName("Conexión Directa a Base de Datos H2")
class SimpleH2ConnectionTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleH2ConnectionTest.class);

    @Test
    @DisplayName("Debe establecer conexión exitosa directa con H2")
    void shouldEstablishDirectH2Connection() throws Exception {
        log.info("=== INICIANDO PRUEBA DE CONEXION DIRECTA A H2 ===");
        
        // Configuración de conexión H2 en memoria
        String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
        String username = "sa";
        String password = "";
        
        // When - Establecer conexión directa
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            
            // Then - Verificar que la conexión es válida
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue(); // Timeout de 5 segundos
            
            log.info("✅ CONEXION DIRECTA ESTABLECIDA EXITOSAMENTE");
            
            // And - Obtener metadatos de la base de datos
            DatabaseMetaData metaData = connection.getMetaData();
            
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            String dbUrl = metaData.getURL();
            String userName = metaData.getUserName();
            
            // Verificar que es H2
            assertThat(databaseProductName).isEqualToIgnoringCase("H2");
            assertThat(dbUrl).contains("h2:mem:testdb");
            
            // Logging de información de conexión
            log.info("📋 DETALLES DE LA CONEXION H2:");
            log.info("   • Base de datos: {}", databaseProductName);
            log.info("   • Versión: {}", databaseProductVersion);
            log.info("   • URL: {}", dbUrl);
            log.info("   • Usuario: {}", userName);
            
            // And - Ejecutar una consulta básica de prueba
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1 as test_value")) {
                
                assertThat(rs.next()).isTrue();
                int testValue = rs.getInt("test_value");
                assertThat(testValue).isEqualTo(1);
                
                log.info("✅ CONSULTA DE PRUEBA EJECUTADA: SELECT 1 = {}", testValue);
            }
            
            // And - Crear una tabla de prueba para verificar operaciones DDL
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(50))");
                log.info("✅ TABLA DE PRUEBA CREADA EXITOSAMENTE");
                
                // Insertar datos de prueba
                stmt.execute("INSERT INTO test_table (id, name) VALUES (1, 'Test Record')");
                log.info("✅ REGISTRO INSERTADO EN TABLA DE PRUEBA");
                
                // Consultar datos insertados
                try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM test_table WHERE id = 1")) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt("id")).isEqualTo(1);
                    assertThat(rs.getString("name")).isEqualTo("Test Record");
                    
                    log.info("✅ CONSULTA VERIFICADA: ID={}, NAME='{}'", 
                            rs.getInt("id"), rs.getString("name"));
                }
                
                // Limpiar - Eliminar tabla de prueba
                stmt.execute("DROP TABLE test_table");
                log.info("✅ TABLA DE PRUEBA ELIMINADA");
            }
            
            log.info("=== PRUEBA DE CONEXION H2 COMPLETADA EXITOSAMENTE ===");
        }
    }

    @Test 
    @DisplayName("Debe verificar características específicas de H2")
    void shouldVerifyH2SpecificFeatures() throws Exception {
        log.info("=== VERIFICANDO CARACTERISTICAS DE H2 ===");
        
        String url = "jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
        String username = "sa";
        String password = "";
        
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Verificar características específicas de H2
            log.info("📋 CARACTERISTICAS DE H2:");
            log.info("   • Soporta transacciones: {}", metaData.supportsTransactions());
            log.info("   • Soporta batch updates: {}", metaData.supportsBatchUpdates());
            log.info("   • Soporta result set holdability: {}", metaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
            
            // Verificar que podemos usar funciones específicas de H2
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT H2VERSION() as version")) {
                
                if (rs.next()) {
                    String h2Version = rs.getString("version");
                    log.info("   • Versión H2: {}", h2Version);
                    assertThat(h2Version).isNotNull().isNotEmpty();
                }
            }
            
            // Verificar modo compatibilidad (debe ser H2 por defecto)
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SETTING_VALUE FROM INFORMATION_SCHEMA.SETTINGS WHERE SETTING_NAME = 'MODE'")) {
                
                if (rs.next()) {
                    String mode = rs.getString("SETTING_VALUE");
                    log.info("   • Modo de compatibilidad: {}", mode);
                }
            }
            
            log.info("✅ VERIFICACION DE CARACTERISTICAS H2 COMPLETADA");
        }
    }
}