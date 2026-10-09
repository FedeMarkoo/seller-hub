# Nexo · Seller Hub

Aplicación full-stack para que los vendedores consulten sus pedidos y respondan preguntas de compradores, mientras Operaciones puede priorizar pendientes por SLA y revisar métricas del canal.

## Demo local

### Requisitos

- Java JDK 21
- Node.js 20 o superior
- npm 10 o superior

No requiere una base de datos ni servicios externos.

### 1. Iniciar el backend

```bash
cd backend
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Podés verificarla en `http://localhost:8080/actuator/health`.

### 2. Iniciar el frontend

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

Abrí `http://localhost:5173`. Vite redirige las llamadas `/api` al backend.

La demo permite:

- cambiar entre tres vendedores y filtrar sus pedidos;
- ver consultas pendientes y respondidas;
- responder una consulta con validación;
- ingresar al panel de Operaciones;
- revisar la cola ordenada por criticidad y antigüedad;
- consultar ventas, tasa y tiempo medio de respuesta;
- restaurar el estado inicial con **Reiniciar demo**.

## Datos de ejemplo

Los datos están en [`backend/src/main/resources/demo-data.json`](backend/src/main/resources/demo-data.json). Se cargan automáticamente al iniciar la API. Las edades de las preguntas son relativas al momento de carga para que las prioridades siempre resulten útiles.

Hay dos maneras claras e idempotentes de recargarlos:

```bash
# desde la interfaz
Botón "Reiniciar demo"

# desde la API
curl -X POST http://localhost:8080/api/demo/reset
```

Para cambiar el conjunto demo, editá el JSON y reiniciá el backend. El campo `ageHours` expresa cuántas horas atrás se creó una pregunta; `responseHours` indica cuánto tardó una respuesta histórica.

## Arquitectura

El backend sigue arquitectura hexagonal:

```text
HTTP / JSON
    │
    ▼
infrastructure/web ──► application/service ──► domain/port
                                                   ▲
                                                   │
                                     infrastructure/persistence
                                           (memoria concurrente)
```

- `domain/model`: entidades, estados y reglas (respuesta única, prioridad por antigüedad).
- `domain/port`: contratos de persistencia sin dependencias de framework.
- `application/service`: casos de uso y proyecciones para la UI.
- `infrastructure/web`: controladores REST y manejo uniforme de errores.
- `infrastructure/persistence`: adaptadores en memoria reemplazables.
- `config`: beans y carga de datos demo.

Spring se mantiene en las capas externas. Se usa un enfoque annotations-first con `@SpringBootApplication`, `@Configuration`, `@Bean`, `@Component`, `@Service`, `@Repository`, `@RestController` y validaciones Jakarta. Lombok reduce código repetitivo mediante `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@NoArgsConstructor` y `@AllArgsConstructor`.

El frontend es React + TypeScript + Vite. Mantiene el acceso HTTP en un cliente separado, componentes de presentación reutilizables y tipado compartido dentro del cliente.

## Priorización y métricas

Una pregunta pendiente se clasifica según su antigüedad:

| Antigüedad | Prioridad |
| --- | --- |
| 48 h o más | Crítica |
| 24–47 h | Alta |
| 8–23 h | Media |
| Menos de 8 h | Baja |

La cola de Operaciones muestra primero la mayor prioridad y, dentro de ella, la pregunta más antigua. Las métricas se calculan en tiempo real a partir del estado actual: pendientes, críticas, tasa de respuesta, tiempo medio de primera respuesta, pedidos y venta bruta.

## API principal

| Método | Endpoint | Uso |
| --- | --- | --- |
| `GET` | `/api/orders?sellerId=seller-01` | Pedidos por vendedor |
| `GET` | `/api/orders/{id}` | Detalle de pedido |
| `GET` | `/api/questions?sellerId=seller-01&status=PENDING` | Consultas filtradas |
| `POST` | `/api/questions/{id}/answer` | Responder (`{"answer":"..."}`) |
| `GET` | `/api/operations/questions` | Cola pendiente priorizada |
| `GET` | `/api/operations/metrics` | Métricas operativas |
| `POST` | `/api/demo/reset` | Restaurar datos demo |

## Verificación

```bash
# backend: 2 pruebas unitarias + 2 de integración
cd backend && ./mvnw test

# frontend: tipos/build, lint y prueba del cliente HTTP
cd frontend
npm run build
npm run lint
npm test
```

## Supuestos y decisiones

- La autenticación y autorización quedan fuera del alcance; el selector simula la identidad del vendedor y la navegación simula el rol de Operaciones.
- Los importes se expresan en ARS y la venta bruta no descuenta pedidos cancelados; en producción la definición debería acordarse con Finanzas.
- La persistencia en memoria favorece una demo reproducible. Los puertos permiten incorporar JPA, MongoDB u otro adaptador sin cambiar los casos de uso.
- Las prioridades representan un SLA simple basado en tiempo. Una implementación productiva podría sumar valor del pedido, riesgo logístico y sentimiento.
- Responder una consulta es una operación de una sola vez. La edición, auditoría, notificaciones y paginación se consideran extensiones productivas.
- Se usan colecciones concurrentes para soportar varias solicitudes, pero no se ofrece durabilidad entre reinicios.

## Stack

- Java 21, Spring Boot 3.5, Maven Wrapper, Lombok, JUnit 5
- React, TypeScript, Vite, Lucide, Vitest, ESLint
