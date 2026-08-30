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

## Decisiones técnicas

- **Java 21 + Spring Boot WebFlux (reactivo).** Un modelo no bloqueante escala mejor
  bajo carga cuando el servicio pasa la mayor parte del tiempo esperando respuestas de
  APIs externas.
- **Arquitectura hexagonal (ports & adapters).** El dominio y los casos de uso no
  dependen de Spring ni de detalles de transporte; se comunican con el exterior a
  través de puertos (interfaces), lo que facilita el testeo con dobles y aísla los
  cambios técnicos.
- **DDD.** `ProductId` es un value object con su invariante (no puede estar vacío);
  `ProductDetail` modela el detalle del producto.
- **Orden por similitud.** El caso de uso resuelve los detalles con
  `flatMapSequential`, que permite concurrencia preservando el orden de los IDs
  similares que exige el contrato.

> Resiliencia (timeouts, circuit breaker, degradación ante fallos parciales) y caché
> se documentan en detalle en los siguientes apartados a medida que se implementan.

## Nota sobre el enunciado

Los componentes de _Test_ y _Mocks_ vienen dados por el repositorio base; solo se
implementa la aplicación. Es obligatorio ejecutar el test de rendimiento (k6) antes de
la entrega y verificar que los resultados son satisfactorios.
