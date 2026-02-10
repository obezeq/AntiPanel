# Justificación DWES

- Nombre: Ezequiel Ortega
- Curso: 2ºDAW
- Fecha: 10/02/2026

## a) Qué endpoint has creado y por qué.

He creado un endpoint GET `/api/v1/analytics` que devuelve estadísticas globales de toda la plataforma en formato JSON. El endpoint retorna un array de objetos `AnalyticResponse`, donde cada objeto contiene:
- `title`: Nombre de la métrica (String)
- `amount`: Valor numérico de la métrica (BigDecimal)

Las 3 metricas que he implementado en el analisis son 3, para permitirle:
1. **Money Spent**: Total de ingresos generados por todas las oredenes completadas
2. **Orders Made**: Número total de órdenes registradas en el sistema
3. **Users Registered**: Total de usuarios registrados en la plataforma

He hecho este endpoint permite visualizar de forma centralizada el rendimiento global de la plataforma, proporcionando métricas clave para analisis y toma de decisiones. Se ha implementado con acceso público para facilitar la visualización de estadísticas generales sin necesidad de autenticación.

**Implementacion tecnica**:
- `AnalyticsController.java`: Aqui el controlador REST que expone el endpoint con anotaciones Swagger/OpenAPI
- `AnalyticsService.java`: Realizo el servicio que calcula las métricas consultando los repositorios
- `AnalyticResponse.java`: Este es el DTO que estructura la respuesta JSON con validaciones
- Repositorios utilizados: `OrderRepository`, `UserRepository`, `TransactionRepository`

**Arquitectura del endpoint**:
El endpoint funciona de una forma muy sencilla.
- El cliente HTTP llama al controlador AnalyticsControler que delega la logica de negocio al servicio, aplicando un buen SOLID y separacion de respondabilidades... El AnalyticsService es el que consulta al repositorio jpa, y el repositorio llama a la base de datos para obtener la respuesta. Todo esto se hace de forma sencilla gracias a Spring Boot que facilita mucho la vida al desarrollador sobre todo en la parte de inyeccion de dependencias.
- Añado el DTO AnalyticResponse, que permite mapear solamente la respuesta que requiere el usuario especificamente. Esto es una buena practica de seguridad y de darle solamente al usuario lo que requiere.

El servicio utiliza:
- `orderRepository.getTotalRevenue()`: Query personalizada que suma el total de ingresos de órdenes completadas
- `orderRepository.count()`: Cuenta total de órdenes en el sistema
- `userRepository.count()`: Cuenta total de usuarios registrados

**Transaccionalidad**: El método `getGlobalAnalytics()` usa `@Transactional(readOnly = true)` para optimizar el rendimiento de consultas de solo lectura y garantizar consistencia de datos.

## b) Cómo has implementado la seguridad.

**Configuración de acceso público**: El endpoint `/api/v1/analytics` está configurado como público en `SecurityConfig.java` mediante `.requestMatchers("/api/v1/analytics").permitAll()` (línea 70). Esto permite el acceso sin autenticación JWT para visualizar estadísticas generales.

**Seguridad implementada en el proyecto**:
1. **Autenticación JWT Stateless**: Configuración completa con `JwtAuthenticationFilter` y `JwtAuthenticationEntryPoint`
2. **Rate Limiting**: Filtro `RateLimitingFilter` que previene abusos y ataques de fuerza bruta
3. **Security Headers** (Spring Security 7 best practices):
   - Content Security Policy (CSP)
   - Permissions Policy (restringe características del navegador)
4. **Control de acceso basado en roles**:
   - Endpoints de admin: requieren rol ADMIN
   - Endpoints de soporte: requieren rol ADMIN o SUPPORT
   - Otros endpoints: requieren autenticación

**Justificación de acceso público**: Se ha permitido el acceso público al endpoint de analytics porque las estadísticas globales no exponen información sensible de usuarios individuales y facilita la visualización de métricas generales de la plataforma sin barreras de autenticación.

## c) Capturas o comandos para probarlo.

No se incluyen capturas porque Docker no me funciono en el ordenador de clase. A continuación se proporcionan los comandos para probar el endpoint manualmente.

### Paso 1: Iniciar el backend Spring Boot

Desde el directorio raíz del proyecto:

```bash
cd backend
```

Si usas Maven Wrapper (incluido en el proyecto):
```bash
./mvnw spring-boot:run
```

Si tienes Maven instalado globalmente:
```bash
mvn spring-boot:run
```

El servidor se iniciará en `http://localhost:8080`

### Paso 2: Probar el endpoint con cURL

Una vez el backend esté corriendo, se ejecutaria un curl GET paara obtener los analisis al endpoint:

```bash
curl -X GET http://localhost:8080/api/v1/analytics
```

### Respuesta que se espera para todos los usuarios de la aplicacion que se ejecuta al hacer el get:

```json
[
  {
    "title": "Money Spent",
    "amount": 15750.50
  },
  {
    "title": "Orders Made",
    "amount": 42
  },
  {
    "title": "Users Registered",
    "amount": 128
  }
]
```

### Paso 3: Probar con navegador

También puedes abrir directamente en el navegador:
```
http://localhost:8080/api/v1/analytics
```

### Paso 4: Ver documentación Swagger

Para ver la documentación interactiva del endpoint tenemos que ir a la url de Swagger en el puerto 8080 cuando este desplegado el backend:
```
http://localhost:8080/swagger-ui.html
```

Busca el controlador "Analytics" y prueba el endpoint `GET /api/v1/analytics` directamente desde Swagger UI.

### Verificación de integración con Frontend

Si el frontend Angular está corriendo en `http://localhost:4200`:

1. Navega a `http://localhost:4200/analysis`
2. El componente Analysis cargará automáticamente los datos del backend mediante `AnalyticsService`
3. Las 3 tarjetas mostrarán las métricas globales obtenidas del endpoint

### Casos de prueba esperados

Como comento no he podido probar los endpoints pero me he cubrido diferentes escenarios con CORS habilidatos, un rate limiting para seguridad y la respuesta correcta como espera en el frontend {title: string, amount: number}

### Integración con frontend

El frontend Angular consume este endpoint mediante:
- **Servicio**: `AnalysisService.getGlobalAnalytics()` en `analysis.service.ts`
- **Componente**: `AnalysisContentSection` muestra las 3 métricas en tarjetas
- **Ruta**: Accesible públicamente en `/analysis` sin necesidad de login
- **Retry logic**: Implementa reintentos automáticos con backoff exponencial para errores 5xx
- **Type safety**: TypeScript interfaces aseguran el tipado correcto de la respuesta

**Prueba de integración completa**:
1. Iniciar backend (puerto 8080)
2. Iniciar frontend (puerto 4200)
3. Navegar a `http://localhost:4200/analysis`
4. Verificar que las 3 tarjetas muestren datos del backend
5. Abrir DevTools Network tab y confirmar petición GET a `/api/v1/analytics`
