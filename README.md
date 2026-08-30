# Similar Products API

API REST que, dado un producto, devuelve el detalle de sus productos similares.
El servicio orquesta dos APIs existentes (mockeadas): una que devuelve los IDs
similares de un producto y otra que devuelve el detalle de un producto por ID.

Contrato de la API: [`similarProducts.yaml`](./similarProducts.yaml).
APIs existentes (upstream): [`existingApis.yaml`](./existingApis.yaml).

## Requisitos previos

- **JDK 21** (probado con Eclipse Temurin 21).
- **Docker** y **Docker Compose** (para los mocks y el test de rendimiento).

No hace falta instalar Maven: el proyecto incluye el **Maven Wrapper** (`mvnw` /
`mvnw.cmd`), que descarga la versión correcta automáticamente.

## Arquitectura

Arquitectura **hexagonal** (ports & adapters). El núcleo de negocio no conoce ni
HTTP ni las APIs externas; todo lo técnico vive en los adaptadores.

```
com.similarproducts
├─ domain            Núcleo: modelo (ProductId, ProductDetail) y puertos (interfaces)
│  ├─ model
│  └─ port           SimilarProductIdsPort, ProductDetailPort (puertos de salida)
├─ application       Casos de uso: GetSimilarProductsUseCase + su implementación
└─ infrastructure    Adaptadores técnicos
   ├─ http/in        Adaptador de entrada: controller REST (WebFlux)
   ├─ http/out       Adaptadores de salida: clientes HTTP hacia las APIs externas
   └─ config         Configuración (WebClient, timeouts, resiliencia, caché)
```

## Puesta en marcha

### 1. Levantar los mocks e infraestructura

Desde la raíz del proyecto:

```bash
docker compose up -d simulado influxdb grafana
```

Comprueba que los mocks responden:

```bash
curl http://localhost:3001/product/1/similarids
```

### 2. Arrancar la aplicación (puerto 5000)

En Linux / macOS / Git Bash:

```bash
./mvnw spring-boot:run
```

En Windows (PowerShell o CMD):

```bash
mvnw.cmd spring-boot:run
```

### 3. Probar el endpoint

```bash
curl http://localhost:5000/product/1/similar
```

### 4. Ejecutar los tests

```bash
./mvnw test
```

### 5. Test de rendimiento (k6)

Con la aplicación arrancada en el 5000 y los mocks levantados:

```bash
docker compose run --rm k6 run scripts/test.js
```

Resultados en Grafana:
[http://localhost:3000/d/Le2Ku9NMk/k6-performance-test](http://localhost:3000/d/Le2Ku9NMk/k6-performance-test)

## API

`GET /product/{productId}/similar`

- **200**: array de `ProductDetail` (`id`, `name`, `price`, `availability`),
  ordenado por similitud.
- **404**: el producto indicado no existe.

## Pruebas de la aplicación

Resultados de probar la API contra los mocks, con la app en el puerto 5000. Cada caso
está diseñado para ejercitar un comportamiento distinto (camino normal, timeout,
degradación ante fallo parcial y 404):

| Petición | Similares en el mock | Código | Tiempo | Respuesta | Qué valida |
|---|---|---|---|---|---|
| `GET /product/1/similar` | `[2,3,4]` (válidos) | 200 | 8 ms | `[2, 3, 4]` | Camino normal |
| `GET /product/2/similar` | `[3,100,1000]` (1000 tarda 5s) | 200 | 2.02 s | `[3, 100]` | Timeout: descarta la dependencia lenta |
| `GET /product/4/similar` | `[1,2,5]` (detalle de 5 → 404) | 200 | 16 ms | `[1, 2]` | Degradación: omite el similar no encontrado |
| `GET /product/5/similar` | `[1,2,6]` (detalle de 6 → 500) | 200 | 8 ms | `[1, 2]` | Degradación: omite el similar con error |
| `GET /product/6/similar` | similarids → 404 | 404 | 8 ms | — | Producto base inexistente |
| `GET /product/999/similar` | no existe | 404 | 6 ms | — | Producto base inexistente |

Salida real:

```text
GET /product/1/similar
[{"id":"2","name":"Dress","price":19.99,"availability":true},{"id":"3","name":"Blazer","price":29.99,"availability":false},{"id":"4","name":"Boots","price":39.99,"availability":true}]
HTTP 200 | 0.007902s

GET /product/2/similar
[{"id":"3","name":"Blazer","price":29.99,"availability":false},{"id":"100","name":"Trousers","price":49.99,"availability":false}]
HTTP 200 | 2.024390s

GET /product/4/similar
[{"id":"1","name":"Shirt","price":9.99,"availability":true},{"id":"2","name":"Dress","price":19.99,"availability":true}]
HTTP 200 | 0.016337s

GET /product/5/similar
[{"id":"1","name":"Shirt","price":9.99,"availability":true},{"id":"2","name":"Dress","price":19.99,"availability":true}]
HTTP 200 | 0.007934s

GET /product/6/similar
HTTP 404 | 0.008519s

GET /product/999/similar
HTTP 404 | 0.006122s
```

El resultado del test de carga (k6) se resume en el apartado
[Resultado del test de carga (k6)](#resultado-del-test-de-carga-k6).

## Decisiones técnicas

- **Java 21 + Spring Boot WebFlux (reactivo):** no bloqueante, escala mejor cuando el
  servicio pasa el tiempo esperando a APIs externas.
- **Arquitectura hexagonal + DDD:** el dominio y los casos de uso no dependen de Spring;
  el exterior entra por puertos (interfaces), lo que permite testear la lógica con
  dobles. `ProductId` es un value object y la regla de "devolver los similares que se
  pudieron obtener" vive en la aplicación.
- **Resiliencia:** timeout por petición (2s), circuit breaker tolerante (solo salta ante
  una caída generalizada del upstream, no ante productos lentos puntuales), degradación
  (se omite el similar que falla) y 404 si el producto base no existe.
- **Rendimiento:** caché de detalles con Caffeine (decorador sobre el puerto; los fallos
  no se cachean) y detalles resueltos en paralelo preservando el orden.
- **Sin reintentos (deliberado):** los fallos del mock son deterministas, así que
  reintentar solo añadiría carga y latencia.

### Resultado del test de carga (k6)

200 usuarios concurrentes: **100% de respuestas 2xx** y latencia acotada por el timeout
(p95 ≈ 2s), sin peticiones colgadas pese a upstreams de 5s/50s.

## Nota sobre el enunciado

Los componentes de _Test_ y _Mocks_ vienen dados por el repositorio base; solo se
implementa la aplicación. Es obligatorio ejecutar el test de rendimiento (k6) antes de
la entrega y verificar que los resultados son satisfactorios.
