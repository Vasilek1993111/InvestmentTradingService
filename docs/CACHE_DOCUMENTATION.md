# Документация по работе с кэшем

## Обзор

Система кэширования в Investment Trading Service обеспечивает быстрый доступ к данным инструментов, лимитам торговли и рыночным данным через T-Invest API.

## Архитектура кэша

### Компоненты системы кэширования

1. **CacheService** - основной сервис кэширования
2. **OrderCacheService** - кэш для заявок
3. **InstrumentServiceFacade** - кэш для инструментов и рыночных данных
4. **TInvestApiService** - интеграция с T-Invest API для получения лимитов

### Типы кэша

- **In-Memory Cache** - быстрый доступ к часто используемым данным
- **Redis Cache** - распределенное кэширование (опционально)
- **Database Cache** - персистентное хранение данных

## API для работы с кэшем

### 1. Получение всех заявок из кэша

**GET** `/api/orders/cache`

Возвращает все заявки, находящиеся в кэше.

#### Response

```json
[
  {
    "orderId": "123e4567-e89b-12d3-a456-426614174000",
    "instrumentId": "BBG004730ZJ9",
    "quantity": 10,
    "price": {
      "units": 2500,
      "nano": 500000000
    },
    "direction": "ORDER_DIRECTION_BUY",
    "accountId": "account123",
    "startTime": "14:30:00",
    "status": "PENDING"
  }
]
```

### 2. Получение заявки по ID из кэша

**GET** `/api/orders/cache/{orderId}`

Возвращает конкретную заявку из кэша.

#### Path Parameters

| Параметр | Тип | Описание |
|----------|-----|----------|
| `orderId` | UUID | Идентификатор заявки |

#### Response

```json
{
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "instrumentId": "BBG004730ZJ9",
  "quantity": 10,
  "price": {
    "units": 2500,
    "nano": 500000000
  },
  "direction": "ORDER_DIRECTION_BUY",
  "accountId": "account123",
  "startTime": "14:30:00",
  "status": "PENDING"
}
```

### 3. Очистка кэша заявок

**DELETE** `/api/orders/cache`

Очищает весь кэш заявок.

#### Response

```json
{
  "message": "Кэш заявок очищен",
  "clearedCount": 150,
  "timestamp": "2025-10-21T10:30:00"
}
```

### 4. Удаление заявки из кэша

**DELETE** `/api/orders/cache/{orderId}`

Удаляет конкретную заявку из кэша.

#### Path Parameters

| Параметр | Тип | Описание |
|----------|-----|----------|
| `orderId` | UUID | Идентификатор заявки |

#### Response

```json
{
  "message": "Заявка удалена из кэша",
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "timestamp": "2025-10-21T10:30:00"
}
```

### 5. Получение статистики кэша

**GET** `/api/orders/cache/statistics`

Возвращает статистику по кэшу заявок.

#### Response

```json
{
  "totalOrders": 150,
  "pendingOrders": 45,
  "sentOrders": 100,
  "errorOrders": 5,
  "cacheSize": "2.5 MB",
  "lastUpdated": "2025-10-21T10:30:00"
}
```

## Работа с лимитами инструментов

### Получение лимитов для инструмента

**GET** `/api/orders/limits/{instrumentId}`

Возвращает лимиты торговли для указанного инструмента.

#### Path Parameters

| Параметр | Тип | Описание |
|----------|-----|----------|
| `instrumentId` | String | FIGI инструмента |

#### Response

```json
{
  "instrumentId": "BBG004730ZJ9",
  "limitUp": 2550.75,
  "limitDown": 2450.25,
  "minPriceIncrement": 0.01,
  "lotSize": 1,
  "lastUpdated": "2025-10-21T10:30:00"
}
```

### Обновление лимитов

**POST** `/api/orders/limits/refresh`

Принудительно обновляет лимиты для всех инструментов из T-Invest API.

#### Response

```json
{
  "message": "Лимиты обновлены",
  "updatedInstruments": 25,
  "timestamp": "2025-10-21T10:30:00"
}
```

## Конфигурация кэша

### Настройки кэша в application.properties

```properties
# Настройки кэша заявок
cache.orders.enabled=true
cache.orders.max-size=10000
cache.orders.ttl=3600

# Настройки кэша инструментов
cache.instruments.enabled=true
cache.instruments.max-size=5000
cache.instruments.ttl=1800

# Настройки кэша лимитов
cache.limits.enabled=true
cache.limits.max-size=1000
cache.limits.ttl=300

# Redis настройки (опционально)
cache.redis.enabled=false
cache.redis.host=localhost
cache.redis.port=6379
cache.redis.password=
```

### Программная конфигурация

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS));
        return cacheManager;
    }
}
```

## Методы работы с кэшем

### CacheService

Основной сервис для работы с кэшем.

#### Методы

- `put(String key, Object value)` - сохранение в кэш
- `get(String key)` - получение из кэша
- `remove(String key)` - удаление из кэша
- `clear()` - очистка всего кэша
- `getAll()` - получение всех элементов
- `size()` - размер кэша
- `containsKey(String key)` - проверка наличия ключа

#### Примеры использования

```java
// Сохранение в кэш
cacheService.put("order_123", orderDTO);

// Получение из кэша
OrderDTO order = cacheService.get("order_123");

// Удаление из кэша
cacheService.remove("order_123");

// Проверка наличия
if (cacheService.containsKey("order_123")) {
    // обработка
}
```

### OrderCacheService

Специализированный сервис для кэширования заявок.

#### Методы

- `put(OrderDTO order)` - сохранение заявки
- `get(String orderId)` - получение заявки по ID
- `getAll()` - получение всех заявок
- `remove(String orderId)` - удаление заявки
- `putAll(List<OrderDTO> orders)` - массовое сохранение
- `clear()` - очистка кэша заявок
- `getByStatus(OrderStatus status)` - получение заявок по статусу
- `getReadyOrders(LocalTime time)` - получение готовых к отправке заявок

#### Примеры использования

```java
// Сохранение заявки
orderCacheService.put(orderDTO);

// Получение заявки
OrderDTO order = orderCacheService.get("order_123");

// Получение заявок по статусу
List<OrderDTO> pendingOrders = orderCacheService.getByStatus(OrderStatus.PENDING);

// Получение готовых к отправке заявок
List<OrderDTO> readyOrders = orderCacheService.getReadyOrders(LocalTime.now());
```

### InstrumentServiceFacade

Сервис для работы с кэшем инструментов и рыночных данных.

#### Методы

- `getInstrumentInfo(String instrumentId)` - получение информации об инструменте
- `getPriceData(String instrumentId)` - получение рыночных данных
- `getPriceDataByType(String instrumentId, String priceType)` - получение данных по типу
- `getMinPriceIncrement(String instrumentId)` - получение минимального шага цены
- `getLotSize(String instrumentId)` - получение размера лота
- `extractPriceFromData(Object data)` - извлечение цены из данных
- `refreshInstrumentData(String instrumentId)` - обновление данных инструмента

#### Примеры использования

```java
// Получение информации об инструменте
InstrumentInfo info = instrumentServiceFacade.getInstrumentInfo("BBG004730ZJ9");

// Получение рыночных данных
List<Object> priceData = instrumentServiceFacade.getPriceData("BBG004730ZJ9");

// Получение минимального шага цены
BigDecimal minIncrement = instrumentServiceFacade.getMinPriceIncrement("BBG004730ZJ9");

// Получение размера лота
Integer lotSize = instrumentServiceFacade.getLotSize("BBG004730ZJ9");
```

## Логирование кэша

### Настройки логирования

```xml
<!-- logback-spring.xml -->
<logger name="com.example.investmenttradingservice.cache" level="DEBUG" additivity="false">
    <appender-ref ref="CACHE_FILE"/>
</logger>

<appender name="CACHE_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/cache-operations.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/cache-operations.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>10MB</maxFileSize>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

### Типы логов

- **DEBUG** - детальная информация о операциях кэша
- **INFO** - основные операции (сохранение, получение, удаление)
- **WARN** - предупреждения (переполнение, истечение TTL)
- **ERROR** - ошибки кэширования

### Примеры логов

```
2025-10-21 10:30:00.123 [http-nio-8089-exec-1] DEBUG c.e.i.service.CacheService - Сохранение в кэш: key=order_123, value=OrderDTO{...}
2025-10-21 10:30:00.124 [http-nio-8089-exec-1] INFO  c.e.i.service.CacheService - Заявка order_123 сохранена в кэш
2025-10-21 10:30:00.125 [http-nio-8089-exec-1] DEBUG c.e.i.service.CacheService - Получение из кэша: key=order_123
2025-10-21 10:30:00.126 [http-nio-8089-exec-1] INFO  c.e.i.service.CacheService - Заявка order_123 найдена в кэше
```

## Мониторинг кэша

### Метрики кэша

- **Cache Hit Rate** - процент попаданий в кэш
- **Cache Miss Rate** - процент промахов кэша
- **Cache Size** - размер кэша в памяти
- **Cache Evictions** - количество вытеснений
- **Cache TTL** - время жизни элементов

### Health Check

**GET** `/actuator/health/cache`

Проверка состояния кэша.

#### Response

```json
{
  "status": "UP",
  "details": {
    "cache": {
      "status": "UP",
      "details": {
        "size": 150,
        "hitRate": 0.95,
        "missRate": 0.05
      }
    }
  }
}
```

## Лучшие практики

### 1. Управление памятью

- Устанавливайте разумные лимиты размера кэша
- Используйте TTL для автоматической очистки
- Мониторьте использование памяти

### 2. Производительность

- Используйте асинхронные операции для больших объемов данных
- Кэшируйте только часто используемые данные
- Избегайте кэширования больших объектов

### 3. Надежность

- Реализуйте fallback при недоступности кэша
- Логируйте все операции кэширования
- Настройте мониторинг состояния кэша

### 4. Безопасность

- Не кэшируйте чувствительные данные
- Используйте шифрование для персистентного кэша
- Реализуйте контроль доступа

## Устранение неполадок

### Частые проблемы

1. **Переполнение кэша**
   - Увеличьте максимальный размер
   - Настройте политику вытеснения
   - Проверьте TTL настройки

2. **Медленная работа кэша**
   - Проверьте настройки памяти
   - Оптимизируйте размер объектов
   - Используйте профилирование

3. **Потеря данных**
   - Проверьте настройки TTL
   - Убедитесь в правильности ключей
   - Проверьте логи ошибок

### Диагностика

```bash
# Проверка состояния кэша
curl http://localhost:8089/actuator/health/cache

# Получение статистики
curl http://localhost:8089/api/orders/cache/statistics

# Очистка кэша при проблемах
curl -X DELETE http://localhost:8089/api/orders/cache
```

## Версия

**Версия документации:** 1.0.0  
**Дата обновления:** 2025-10-21  
**Совместимость:** Investment Trading Service 1.1.0+
