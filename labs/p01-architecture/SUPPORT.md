# SUPPORT P01
1. Lee InventoryClient.getInventory como modelo de llamada RestClient. Usa uri con builder y queryParam, retrieve y body(ShippingQuote.class). RestClientException se captura y envuelve; no devolver null.
2. El service usa constructor injection ya preparada. Primero validación, luego inventory.getInventory. El record InventorySnapshot tiene available(); ShippingQuote tiene priceCents(). Devuelve OrderEstimateResponse con los datos correctos.
3. @GetMapping("/estimate") y @RequestParam explícito para cada argumento; mira OrderPreviewController. No colocar lógica de red en controller.
4. make p01-test ejecuta tests de ejercicio (rojos hasta completar); make test ejecuta regresión normal. Si falla un test, lee aserción, no lo cambies para ocultar problema.
5. Shipping está en perfil extended. make p01-up activa solo los cuatro servicios necesarios. No instalar localmente ni tocar Azure.
6. Gateway: ejemplo de rutas existentes en application.yml. SetPath=/api/inventory/{sku} con Path=/stock/{sku}. Conserva rutas anteriores.
