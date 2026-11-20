# Enunciados de todas las prácticas — Parte 1

Este índice permite **consultar desde el primer día el enunciado completo de P00 a P07**, las ayudas, los ejercicios opcionales y las plantillas de evidencias. Los materiales son para alumnos: no contienen soluciones privadas ni ramas internas.

**Importante:** tener un enunciado disponible **no significa que el código de esa práctica ya esté publicado**. El código se libera progresivamente en el directorio `labs/` y en `services/` del [repositorio de alumnos](../../README.md). Al llegar a cada práctica, utiliza el enunciado del checkpoint activo (en `labs/`) como fuente operativa. Las versiones aquí publicadas permiten lectura previa.

| Práctica | Enunciado | Materiales de acompañamiento |
|---|---|---|
| P00 · Entorno y arquitectura inicial | [P00](../../labs/p00-getting-started/README.md) | En el checkpoint inicial |
| P01 · Estimación HTTP y API Gateway | [P01](../../labs/p01-architecture/README.md) | En el checkpoint inicial |
| P02 · Timeout, Retry, Circuit Breaker y Bulkhead | [P02](p02-resilience/README.md) | [Ayudas](p02-resilience/SUPPORT.md) · [Evidencias](p02-resilience/EVIDENCE.md) · [Extensión](p02-resilience/STRETCH.md) |
| P03 · Kafka, eventos e idempotencia | [P03](p03-event-driven/README.md) | [Ayudas](p03-event-driven/SUPPORT.md) · [Evidencias](p03-event-driven/EVIDENCE.md) · [Extensión](p03-event-driven/STRETCH.md) |
| P04 · Event Sourcing, CQRS y Saga | [P04](p04-state/README.md) | [Ayudas](p04-state/SUPPORT.md) · [Evidencias](p04-state/EVIDENCE.md) · [Extensión](p04-state/STRETCH.md) |
| P05 · Webhooks, gRPC y Protobuf | [P05](p05-integrations/README.md) | [Ayudas](p05-integrations/SUPPORT.md) · [Evidencias](p05-integrations/EVIDENCE.md) · [Extensión](p05-integrations/STRETCH.md) |
| P06 · Kubernetes, Istio y Linkerd | [P06](p06-mesh/README.md) | [Ayudas](p06-mesh/SUPPORT.md) · [Evidencias](p06-mesh/EVIDENCE.md) · [Extensión](p06-mesh/STRETCH.md) |
| P07 · Incidente de consistencia | [P07](p07-incident/README.md) | [Ayudas](p07-incident/SUPPORT.md) · [Evidencias](p07-incident/EVIDENCE.md) · [Extensión](p07-incident/STRETCH.md) · [ADR](p07-incident/ADR-template.md) |

## Código y recuperación de sesiones

- **P01–P05:** sus soluciones se incorporan al checkpoint siguiente junto con el starter nuevo. Conserva tu trabajo en una rama propia y sigue la guía general del alumno antes de actualizar tu fork.
- Si no asististe a una sesión, consulta el enunciado y recupera el checkpoint siguiente cuando se publique; podrás examinar la implementación de la etapa anterior en `services/`. El código futuro no se adelanta.
- **P06 es independiente de P07:** el checkpoint P07 no contiene automáticamente la solución de Istio/Linkerd. El instructor publicará la referencia de P06 por separado una vez terminada esa práctica.
- **P07:** su solución también se publicará expresamente al finalizar, porque no existe un P08 que la incorpore.
- No utilices soluciones de ejercicios posteriores antes del trabajo en clase. Los enlaces a rutas de código todavía inexistentes pasan a estar disponibles cuando se publica su checkpoint.

Para empezar, consulta la [guía general de trabajo](../../labs/GUIA-ALUMNO.md).
