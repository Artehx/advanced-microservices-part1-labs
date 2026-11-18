# Guía de bienvenida y trabajo — Advanced Orders Platform

## Bienvenido al curso
Arquitectura de Microservicios Avanzada, Parte 1: Diseño y Patrones Avanzados. Trabajaremos sobre una aplicación de comercio electrónico simulada y evolucionaremos su arquitectura a través de ejercicios de diseño, implementación y comprobación. No necesitas haber realizado la Parte 2. No hay datos de clientes, pagos, envíos ni notificaciones reales.

El instructor facilita el enlace del repositorio de alumnos y los checkpoints en el orden del curso. Este documento explica cómo preparar tu entorno y conservar tu trabajo; cada práctica tiene su propio enunciado. No necesitas instalar Java, Maven, Docker o Kubernetes en tu ordenador.

## 1. Antes de empezar
Necesitas una cuenta de GitHub con acceso al repositorio indicado, un navegador y acceso a GitHub Codespaces según las políticas de tu organización y tu cuenta. Comprueba que puedes iniciar sesión; si tu empresa restringe forks o Codespaces, avisa al instructor antes de crear recursos alternativos. No introduzcas credenciales corporativas en el código, archivos, capturas o commits.

En GitHub, abre el repositorio de alumnos compartido por el instructor. Crea un fork con el botón **Fork**, seleccionando tu espacio de trabajo autorizado. Si no puedes hacer fork, no cambies la visibilidad ni solicites permisos adicionales sin consultar: el instructor te indicará el procedimiento alternativo. El repositorio del curso se irá actualizando por etapas.

En tu fork, selecciona la rama main y elige **Code → Codespaces → Create codespace on main**. Espera a que VS Code en el navegador termine de preparar el entorno. Comprueba que estás en tu fork, no en el repositorio compartido. La configuración solicita 4 CPU, 8 GB RAM y 32 GB almacenamiento; utiliza la máquina autorizada por el instructor. El entorno puede consumir cuota de Codespaces de tu cuenta/organización: no crees varios sin necesidad.

## 2. Orientación en el entorno
En el explorador de VS Code encontrarás:
- `services/`: código Java, POM de Maven y Dockerfile de cada microservicio.
- `labs/`: enunciados, ayudas, plantillas de evidencias y extensiones disponibles.
- `scripts/`: comprobaciones y experimentos.
- `.devcontainer/`: entorno reproducible de Codespaces.
- `pom.xml`, `docker-compose.yml` y `Makefile`: construcción, servicios y comandos del proyecto.

Abre **Terminal → New Terminal**. Los comandos se ejecutan dentro del Codespace, en la raíz del repositorio. No se ejecutan en PowerShell ni en tu ordenador local. Si dudas:
```bash
pwd
git status --short --branch
git rev-parse --short HEAD
git remote -v
```
Comprueba que origin apunta a tu fork. Si aparece otro repositorio, consulta al instructor. No compartas URLs que incluyan tokens o credenciales.

### Preparación inicial
```bash
make preflight
make test
```
El preflight comprueba Java 21, Maven, Docker/Compose, Python, OpenSSL, curl, Git y Make. Espera el mensaje Preflight OK. Maven ejecuta las pruebas de regresión y debe terminar con BUILD SUCCESS. Las pruebas de aceptación de un ejercicio pueden fallar intencionadamente hasta que lo resuelvas; las pruebas de regresión no.

Continúa con [P00 — Primer contacto con la plataforma](p00-getting-started/README.md). Arrancarás los tres servicios base y comprobarás una petición real. El siguiente ejercicio es [P01 — Estimación de pedido](p01-architecture/README.md).

## 3. Trabaja como desarrollador
Antes de modificar el código, crea una rama propia desde el checkpoint indicado. Por ejemplo, para P01 (si acabas de crear el Codespace desde main):
```bash
git switch -c trabajo/p01
git status --short --branch
```
Lee el contexto, el contrato, los archivos a modificar y los resultados esperados. Diseña antes de programar; implementa los TODOs; ejecuta pruebas unitarias e integración; guarda evidencias. No copies una solución ni cambies un test para que pase.

Cuando completes una tarea:
```bash
git status --short
git diff
git add services/orders/src
# Añade de forma explícita los demás archivos que hayas modificado y revisado.
git diff --cached
git commit -m "Implementa estimacion de pedido"
git push -u origin trabajo/p01
```
El ejemplo corresponde a P01; adapta rutas y mensaje a tu cambio. No incluyas credenciales, archivos .env con secretos, datos personales, outputs masivos, target/ ni archivos temporales. Usa git status y git diff para comprobarlo. Si no puedes hacer push, conserva el commit local y pide ayuda antes de eliminar el Codespace.

## 4. Cómo avanzar al siguiente checkpoint
El instructor anunciará cada actualización de main en el repositorio de alumnos. Los checkpoints posteriores contienen el código de recuperación de las prácticas anteriores para que todos puedan avanzar, incluso si alguna implementación quedó incompleta. Tu trabajo sigue en tu rama: no se pierde si lo guardas antes de cambiar.

1. Guarda evidencias y el diff de tu ejercicio. Detén el overlay actual con el comando de cierre de su enunciado **antes de cambiar de rama**. No uses down -v salvo que el instructor confirme un reset de tu sandbox.
2. Haz commit y push de tu rama de trabajo. Comprueba git status.
3. En GitHub, abre tu fork y selecciona **Sync fork → Update branch** cuando se anuncie el siguiente checkpoint. Si hay conflicto, no elijas descartar cambios ni fuerces la actualización: consulta al instructor. Tu main debe mantenerse como punto de partida del curso.
4. En la terminal del mismo Codespace, comprueba que no quedan cambios sin guardar y trae la nueva versión:
```bash
git fetch origin
git switch main
git pull --ff-only origin main
git log -1 --oneline
git status --short --branch
```
5. Comprueba que el README corresponde al checkpoint anunciado. Crea una rama nueva; por ejemplo:
```bash
git switch -c trabajo/p02
make preflight
```
6. Sigue el enunciado del nuevo ejercicio. No mezcles ramas ni apliques cherry-pick de soluciones antiguas para resolverlo. Si el devcontainer cambia (por ejemplo, P06), utiliza **Dev Containers: Rebuild Container** solo tras guardar y subir tus cambios y siguiendo las instrucciones del instructor. Comprueba de nuevo preflight y herramientas. No necesitas crear un Codespace adicional para cada ejercicio.

Si tu fork utiliza otra rama por defecto o el instructor entrega un checkpoint con otro procedimiento, sigue sus indicaciones. No hagas git reset --hard, git clean -fd, force push ni borrado de ramas para corregir un conflicto.

## 5. Cómo utilizar los laboratorios
Cada guía incluye contexto, arquitectura, objetivos, mapa de código, tareas progresivas, comprobaciones y criterios de entrega. Los archivos SUPPORT proporcionan pistas; EVIDENCE ayuda a registrar resultados; STRETCH contiene extensiones opcionales. Si una salida no coincide, anota el resultado real y consulta la ayuda antes de continuar.

| Práctica | Resultado de aprendizaje |
|---|---|
| P00 | Entorno funcional y recorrido de una petición |
| P01 | Composición REST entre microservicios |
| P02 | Timeout, Retry, Circuit Breaker y Bulkhead |
| P03 | Publicación, consumo Kafka e idempotencia |
| P04 | Event Sourcing, CQRS y Saga |
| P05 | Webhooks, Protobuf y gRPC |
| P06 | Balanceo Kubernetes y políticas de Service Mesh |
| P07 | Diagnóstico y corrección de un incidente |

El código se entrega por etapas. Las guías disponibles en tu checkpoint son las que debes utilizar; no necesitas las ramas privadas del instructor. P06 requiere un namespace, credenciales e imágenes de laboratorio facilitadas por el instructor; utiliza exclusivamente esos recursos y no instales software en un cluster compartido. P07 es independiente de P06.

## 6. Si algo falla
Primero identifica si es un problema de entorno, compilación, arranque, red, prueba de aceptación o lógica de negocio. No cambies pruebas para ocultarlo. En el entorno base:
```bash
git status --short --branch
git rev-parse --short HEAD
make preflight
docker compose ps
make logs
```
En P03/P04/P05 utiliza el comando Compose con los overlays de su guía; no arranques el entorno base sobre otro overlay. Si el Codespace se detiene por inactividad, vuelve a abrirlo en GitHub Codespaces, comprueba rama y contenedores y recupera con el comando de arranque de tu práctica. No presupongas que los servicios siguen activos. Si falta Python u OpenSSL tras la preparación declarativa, informa al instructor; no instales herramientas en el ordenador local.

Para comunicar una incidencia, indica práctica, rama y SHA, comando exacto, primera línea de error relevante, salida esperada frente a observada y cambios que hiciste. Adjunta logs pertinentes, no todo el historial. Oculta secretos, tokens, cabeceras de autenticación y credenciales. No compartas datos personales ni información de otros alumnos.

## 7. Guarda el trabajo y cierra
Entrega el diff/commit y las evidencias indicadas en la guía. Detén el overlay con su comando de cierre; `make down` para la aplicación base. Los volúmenes se conservan salvo que se borren explícitamente. En P06 sigue la limpieza específica de tu namespace y kind. No borres recursos ajenos.

Detén el Codespace desde tu lista de Codespaces cuando no lo utilices. Cerrar la pestaña del navegador no equivale a detener el entorno. Eliminar el Codespace puede perder cambios no subidos; guarda el trabajo antes y no lo elimines hasta que el instructor lo indique. Los puertos deben permanecer privados.

**Empieza aquí:** [P00](p00-getting-started/README.md).
