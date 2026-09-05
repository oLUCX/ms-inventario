# ms-inventario

Microservicio de **Catálogo y Existencias Físicas** del caso BodegaNube (asignatura JVY0101).

## Responsabilidad (bounded context)
Administrar el catálogo de productos, validar existencias en tiempo real y realizar
reservas/descuentos lógicos de stock. Ver sección 4.1 del informe de arquitectura.

## Requerimientos que cubre
- **RF-04**: reserva/descuento de stock.
- **RF-07**: gestión centralizada del catálogo de productos e inventario.

## Stack
Java 17, Spring Boot 3.2, Spring Data JPA, PostgreSQL.

## Endpoints
| Método | Endpoint                         | Descripción                                          |
|--------|-----------------------------------|-------------------------------------------------------|
| GET    | `/api/productos`                 | Lista el catálogo completo.                            |
| POST   | `/api/productos/{sku}/reservar`  | Reserva stock si hay disponibilidad suficiente.        |
| POST   | `/api/productos/{sku}/descontar` | Descuenta stock reservado (al confirmar el despacho).  |

## Cómo correrlo localmente
1. Crear una base PostgreSQL llamada `bodeganube_inventario`.
2. `mvn spring-boot:run`

## Próximos pasos (fuera del alcance de este esqueleto)
- Exponer el endpoint de reserva protegido con `@CircuitBreaker` (Resilience4j) del lado de
  `ms-ordenes`, que es quien lo invoca.
- Endpoint de administración de catálogo (alta/baja de productos).
