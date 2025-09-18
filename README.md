# Sistema de Gestión de Transacciones

Un sistema completo de gestión de transacciones bancarias construido con Spring Boot (backend), JSF con PrimeFaces (frontend), PostgreSQL (base de datos) y Apache Kafka (mensajería).

##  Arquitectura del Sistema

El sistema está compuesto por los siguientes componentes:

- **Backend API**: Spring Boot con REST API para procesamiento de transacciones
- **Frontend Web**: JSF con PrimeFaces para interfaz de usuario
- **Base de Datos**: PostgreSQL con esquema TESTHTC
- **Mensajería**: Apache Kafka para eventos de transacciones
- **Orquestación**: Docker Compose para deployment

##  Prerrequisitos

- **Docker** (versión 20.10 o superior)
- **Docker Compose** (versión 2.0 o superior)
- **Java 17** (para desarrollo local)
- **Maven 3.8+** (para compilación local)

##  Inicio Rápido

### 1. Clonar y Preparar el Proyecto

```bash
# Clonar el repositorio
git clone <repository-url>
cd isa_test

# Verificar estructura de directorios
ls -la
# Deberías ver: backend-project/, frontend-project/, docker/, docker-compose.yml
```

### 2. Compilar los Proyectos

```bash
# Compilar backend
cd backend-project
mvn clean package -DskipTests
cd ..

# Compilar frontend
cd frontend-project
mvn clean package
cd ..
```

### 3. Levantar el Sistema Completo

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs de todos los servicios
docker-compose logs -f

# Ver el estado de los servicios
docker-compose ps
```

### 4. Verificar que Todo Esté Funcionando

Espera unos minutos para que todos los servicios se inicien completamente, luego verifica:

```bash
# Verificar salud del backend
curl http://localhost:8080/api/transactions/health

# Verificar frontend (abrir en navegador)
open http://localhost:8081/transaction-frontend/
```

##  URLs de Acceso

Una vez que el sistema esté ejecutándose:

- **Frontend Web**: http://localhost:8081/transaction-frontend/
- **Backend API**: http://localhost:8080/api/transactions
- **API Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8090 (opcional, con profile monitoring)
- **pgAdmin**: http://localhost:8091 (opcional, con profile monitoring)

##  Uso del Sistema

### Interfaz Web (Frontend)

1. Abrir http://localhost:8081/transaction-frontend/
2. Completar el formulario de transacción:
   - **ID del Cliente**: Número del 1-5 (clientes de ejemplo pre-cargados)
   - **Número de Cuenta**: Formato ABC-123456 (ej: ACC-123456, SAV-654321)
   - **Monto**: Positivo para crédito, negativo para débito (-10,000 a +10,000)
3. Hacer clic en "Procesar Transacción"
4. Ver el resultado de la transacción

### API REST (Backend)

```bash
# Ejemplo de transacción POST
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "accountNumber": "ACC-123456",
    "amount": 500.00
  }'

# Consultar saldo
curl http://localhost:8080/api/transactions/balance/1/ACC-123456

# Verificar salud del sistema
curl http://localhost:8080/api/transactions/health
```

##  Comandos Útiles de Docker

```bash
# Ver logs de un servicio específico
docker-compose logs -f backend
docker-compose logs -f frontend

# Reiniciar un servicio
docker-compose restart backend

# Parar todos los servicios
docker-compose down

# Parar y eliminar volúmenes (perderás los datos)
docker-compose down -v

# Reconstruir las imágenes
docker-compose build --no-cache

# Iniciar con servicios de monitoreo
docker-compose --profile monitoring up -d
```

##  Resolución de Problemas

### El Frontend No Se Conecta al Backend

1. Verificar que el backend esté saludable:
   ```bash
   curl http://localhost:8080/api/transactions/health
   ```

2. Verificar logs del frontend:
   ```bash
   docker-compose logs frontend
   ```

3. Verificar conectividad de red:
   ```bash
   docker-compose exec frontend ping backend
   ```

### Base de Datos No Se Inicializa

1. Verificar logs de PostgreSQL:
   ```bash
   docker-compose logs postgres
   ```

2. Conectarse manualmente a la DB:
   ```bash
   docker-compose exec postgres psql -U postgres -d transaction_db -c "\dt TESTHTC.*"
   ```

### Problemas de Kafka

1. Verificar que Zookeeper esté funcionando:
   ```bash
   docker-compose logs zookeeper
   ```

2. Verificar topics de Kafka:
   ```bash
   docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
   ```

### Reiniciar Completamente

Si tienes problemas persistentes:

```bash
# Parar todo y limpiar
docker-compose down -v
docker system prune -f

# Recompilar aplicaciones
cd backend-project && mvn clean package -DskipTests && cd ..
cd frontend-project && mvn clean package && cd ..

# Levantar de nuevo
docker-compose up -d --build
```

##  Monitoreo y Logging

### Ver Logs en Tiempo Real

```bash
# Todos los servicios
docker-compose logs -f

# Solo backend
docker-compose logs -f backend

# Solo frontend  
docker-compose logs -f frontend
```

### Monitoreo con Herramientas Opcionales

```bash
# Iniciar con herramientas de monitoreo
docker-compose --profile monitoring up -d

# Acceder a Kafka UI: http://localhost:8090
# Acceder a pgAdmin: http://localhost:8091
```

##  Desarrollo Local

### Ejecutar Backend Localmente

```bash
cd backend-project

# Con Docker Compose para dependencias
docker-compose up -d postgres kafka zookeeper

# Ejecutar backend con Maven
mvn spring-boot:run -Dspring.profiles.active=local
```

### Ejecutar Frontend Localmente

```bash
cd frontend-project

# Asegurarse de que el backend esté ejecutándose
export BACKEND_API_URL=http://localhost:8080

# Deploy en WildFly local
mvn wildfly:deploy
```

## 📝 Estructura del Proyecto

```
isa_test/
├── backend-project/          # Spring Boot API
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── Dockerfile
│   └── pom.xml
├── frontend-project/         # JSF Frontend
│   ├── src/main/java/
│   ├── src/main/webapp/
│   ├── Dockerfile
│   └── pom.xml
├── docker/
│   └── postgres/
│       └── init.sql         # Script de inicialización DB
├── docker-compose.yml       # Orquestación de servicios, se manejan mas archivos dentro de la carpeta docker
└── README.md
```
