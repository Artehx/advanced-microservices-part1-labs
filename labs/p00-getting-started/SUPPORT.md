# P00 — Ayuda y recuperación
- **Rama incorrecta:** comprueba git status y SHA; no cambies a main ni reference. Consulta instructor antes de cambiar si tienes cambios.
- **Herramienta ausente / Docker no responde:** revisa que el Codespace terminó la configuración devcontainer; conserva salida de make preflight. No instalar herramientas en equipo local.
- **Maven falla:** busca primer error y módulo; distingue descarga, compilación y prueba. No omitir pruebas para ocultarlo. make p01-test es otro objetivo, intencionadamente rojo.
- **Contenedor no saludable:** docker compose ps; make logs; revisar mensaje del servicio. No hacer prune ni borrar volúmenes globales.
- **curl falla:** confirma puerto y ruta desde terminal del Codespace; no uses localhost de tu ordenador ni publiques el puerto.
- **Respuesta negativa de negocio:** canBeFulfilled=false con HTTP 200 es correcto cuando la cantidad supera el stock.
