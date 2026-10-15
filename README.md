# Nexo · Seller Hub

Aplicación full-stack para que los vendedores consulten sus pedidos y respondan preguntas de compradores, mientras Operaciones puede priorizar pendientes por SLA y revisar métricas del canal.

## Demo local

### Requisitos

- Java JDK 21
- Node.js 20 o superior
- npm 10 o superior

Usa persistencia en memoria, sin dependencias externas de base de datos.

### 1. Iniciar el backend

```bash
cd backend
gradle bootRun
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
- crear nuevas preguntas asociadas a un pedido, con notificación interna al vendedor;
- actualizar estados de pedido respetando transiciones válidas;
- filtrar pedidos por texto, estado y rango de fechas;
- ingresar al panel de Operaciones;
- revisar la cola ordenada por criticidad y antigüedad;
- consultar ventas, tasa y tiempo medio de respuesta;
- comparar pedidos y facturación de los últimos 7 días contra el período anterior;
- restaurar el estado inicial con **Reiniciar demo**.

La demo crea una sesión server-side con expiración de 8 horas y revocación explícita. El selector superior funciona como un falso login: también se conserva al entrar a Operaciones, por lo que cada vendedor sólo ve sus pedidos, preguntas, métricas y cola. El token se envía en `X-Demo-Session`; no es un mecanismo de autenticación productivo y debe reemplazarse por OAuth/OIDC en un entorno real.

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

## Ejecución con Docker

También podés iniciar backend y frontend con `docker compose up --build`. No requiere ningún servicio adicional.

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
- Cada ítem conserva `productId` como identidad estable de catálogo y `product` como snapshot visible del nombre al momento de la compra.
- `domain/port`: contratos de persistencia sin dependencias de framework.
- `application/service`: casos de uso y proyecciones para la UI.
- `application/mapper`: mapeos entidad/DTO generados por MapStruct; los servicios no construyen DTOs manualmente.
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
| `POST` | `/api/orders/{id}/status` | Cambiar estado con transición válida |
| `POST` | `/api/orders/{id}/questions` | Crear pregunta (`buyerName`, `text`) |
| `GET` | `/api/questions?sellerId=seller-01&status=PENDING` | Consultas filtradas |
| `POST` | `/api/questions/{id}/answer` | Responder (`{"answer":"..."}`) |
| `GET` | `/api/operations/questions` | Cola pendiente priorizada |
| `GET` | `/api/operations/metrics` | Métricas operativas |
| `POST` | `/api/demo/reset` | Restaurar datos demo |
| `POST` | `/api/demo/session` | Crear sesión demo (`SELLER` u `OPERATIONS`) |
| `DELETE` | `/api/demo/session` | Revocar sesión demo |

Todos los endpoints de negocio requieren `X-Demo-Session`. En esta demo los listados aceptan `limit` y lo acotan a un máximo de 1.000 elementos para poder explorar el dataset completo de cada vendedor. En producción convendría reemplazarlo por paginación o cursor.

## Verificación

```bash
# backend: pruebas unitarias y de integración
cd backend && gradle test

# frontend: tipos/build, lint y prueba del cliente HTTP
cd frontend
npm run build
npm run lint
npm test
```

## Supuestos y decisiones

- La autenticación productiva queda fuera del alcance; la demo sí aplica aislamiento por vendedor, expiración y revocación de sesiones.
- Los importes se expresan en ARS y la venta bruta no descuenta pedidos cancelados; en producción la definición debería acordarse con Finanzas.
- La persistencia en memoria favorece una demo reproducible y no requiere infraestructura externa.
- Las prioridades representan un SLA simple basado en tiempo. Una implementación productiva podría sumar valor del pedido, riesgo logístico y sentimiento.
- Responder una consulta es una operación de una sola vez. Las notificaciones están implementadas como puerto con adaptador de logging, listo para reemplazarse por email/webhook.
- Se usan colecciones concurrentes para soportar varias solicitudes en memoria; los datos se reinician al reiniciar la API.

## Stack

- Java 21, Spring Boot 3.5, Gradle, Lombok, MapStruct, JUnit 5
- React, TypeScript, Vite, Lucide, Vitest, ESLint
