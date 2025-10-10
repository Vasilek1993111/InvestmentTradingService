# Глобальная обработка ошибок - Руководство для тестировщиков

## Обзор

Система глобальной обработки ошибок предоставляет стандартизированные JSON-ответы с детальной информацией об ошибках, что помогает тестировщикам быстро понимать и исправлять проблемы.

## Структура ответов

### Успешные ответы (200 OK)

```json
{
  "message": "Акции успешно получены из кэша",
  "data": [...],
  "totalCount": 150,
  "dataType": "shares",
  "status": 200,
  "timestamp": "2024-01-15T12:00:00",
  "metadata": {
    "cacheStatus": "active",
    "lastUpdate": "2024-01-15T11:30:00"
  },
  "success": true
}
```

### Ответы об ошибках

```json
{
  "message": "Инструмент с FIGI 'INVALID_FIGI' не найден в системе",
  "errorCode": "INSTRUMENT_NOT_FOUND",
  "status": 404,
  "path": "/api/cache/by-figi/INVALID_FIGI",
  "timestamp": "2024-01-15T12:00:00",
  "operationContext": "Поиск инструмента по FIGI",
  "suggestion": "Проверьте корректность FIGI. Используйте GET /api/cache/instruments для получения списка доступных инструментов",
  "examples": ["BBG000B9XRY4", "BBG004730N88", "BBG004730ZJ9"],
  "documentationUrl": "/api/docs/instruments",
  "details": {
    "figi": "INVALID_FIGI",
    "searchScope": "all_instruments",
    "availableEndpoints": ["GET /api/cache/instruments", "GET /api/cache/shares", "GET /api/cache/futures"]
  }
}
```

## Типы ошибок и их коды

### 1. Ошибки валидации (400 Bad Request)

**Код ошибки:** `VALIDATION_ERROR`

**Примеры:**
- Пустой FIGI
- Неправильный формат FIGI
- Неправильный формат UUID для orderId
- Неподдерживаемый тип инструментов

```json
{
  "message": "FIGI должен содержать ровно 12 символов (буквы и цифры)",
  "errorCode": "VALIDATION_ERROR",
  "status": 400,
  "operationContext": "Поиск инструмента по FIGI",
  "suggestion": "Исправьте поле 'figi' согласно требованиям: 12-символьный код (буквы и цифры)",
  "examples": ["BBG000B9XRY4", "BBG004730N88", "BBG004730ZJ9"],
  "details": {
    "field": "figi",
    "rejectedValue": "INVALID",
    "expectedFormat": "12-символьный код (буквы и цифры)",
    "validationRules": {
      "pattern": "^[A-Z0-9]{12}$",
      "minLength": 12,
      "maxLength": 12,
      "description": "FIGI должен содержать 12 символов (буквы и цифры)"
    }
  }
}
```

### 2. Инструмент не найден (404 Not Found)

**Код ошибки:** `INSTRUMENT_NOT_FOUND`

**Когда возникает:**
- Запрос конкретного инструмента по FIGI
- Инструмент не существует в системе

```json
{
  "message": "Инструмент с FIGI 'BBG000B9XRY4' не найден в системе",
  "errorCode": "INSTRUMENT_NOT_FOUND",
  "status": 404,
  "operationContext": "Поиск инструмента по FIGI",
  "suggestion": "Проверьте корректность FIGI. Используйте GET /api/cache/instruments для получения списка доступных инструментов",
  "examples": ["BBG000B9XRY4", "BBG004730N88", "BBG004730ZJ9"],
  "details": {
    "figi": "BBG000B9XRY4",
    "searchScope": "all_instruments"
  }
}
```

### 3. Пустой список инструментов (200 OK)

**Код ошибки:** `EMPTY_INSTRUMENTS_LIST`

**Важно:** Это НЕ ошибка! Возвращается статус 200 с пустым списком.

**Когда возникает:**
- Запрос всех инструментов, но кэш пуст
- Запрос конкретного типа инструментов, но они не найдены

```json
{
  "message": "Акции не найдены в кэше. Возможно, кэш еще не заполнен или произошла ошибка при загрузке данных.",
  "errorCode": "EMPTY_INSTRUMENTS_LIST",
  "status": 200,
  "operationContext": "Получение списка акций",
  "suggestion": "Попробуйте изменить критерии поиска или проверьте доступность данных",
  "examples": [
    "Используйте GET /api/cache/instruments для получения всех инструментов",
    "Проверьте актуальность данных в кэше"
  ],
  "details": {
    "instrumentType": "shares",
    "suggestion": "Попробуйте более широкие критерии поиска"
  }
}
```

### 4. Ошибки внешнего API (502 Bad Gateway)

**Код ошибки:** `EXTERNAL_API_ERROR`

**Когда возникает:**
- Ошибка Tinkoff Invest API
- Таймаут внешнего API
- Недоступность внешнего сервиса

```json
{
  "message": "Ошибка внешнего API 'Tinkoff Invest API': TIMEOUT",
  "errorCode": "EXTERNAL_API_ERROR",
  "status": 502,
  "operationContext": "Получение данных от Tinkoff Invest API",
  "suggestion": "Попробуйте повторить запрос позже или обратитесь к администратору",
  "details": {
    "apiName": "Tinkoff Invest API",
    "externalErrorCode": "TIMEOUT",
    "retryRecommended": true
  }
}
```

### 5. Ошибки доступа (403 Forbidden)

**Код ошибки:** `ACCESS_DENIED`

```json
{
  "message": "Доступ к ресурсу 'orders' запрещен",
  "errorCode": "ACCESS_DENIED",
  "status": 403,
  "operationContext": "Управление заявками",
  "suggestion": "Обратитесь к администратору для получения разрешения: READ_WRITE",
  "details": {
    "resource": "orders",
    "requiredPermission": "READ_WRITE",
    "authenticationRequired": true
  }
}
```

## Специальные случаи

### Различие между пустым списком и "не найдено"

1. **GET /api/cache/shares** - если акций нет → **200 OK** с пустым списком
2. **GET /api/cache/by-figi/INVALID** - если FIGI не найден → **404 Not Found**

### Валидация параметров

Все параметры автоматически валидируются:

- **FIGI:** 12 символов, только буквы и цифры
- **UUID:** стандартный формат UUID
- **Время:** формат HH:mm:ss
- **Количество:** положительное целое число
- **Цена:** положительное число с точностью до 2 знаков

## Полезные поля для тестирования

### `operationContext`
Показывает, в какой операции произошла ошибка:
- "Получение списка всех инструментов"
- "Поиск инструмента по FIGI"
- "Управление заявками"

### `suggestion`
Конкретные рекомендации по исправлению ошибки

### `examples`
Примеры корректных значений для поля

### `details`
Дополнительная техническая информация

### `documentationUrl`
Ссылка на соответствующую документацию

## Тестовые сценарии

### 1. Тестирование валидации FIGI

```bash
# Неправильный формат
curl -X GET "http://localhost:8089/api/cache/by-figi/INVALID"

# Пустой FIGI
curl -X GET "http://localhost:8089/api/cache/by-figi/"

# Правильный формат (но несуществующий)
curl -X GET "http://localhost:8089/api/cache/by-figi/BBG000B9XRY4"
```

### 2. Тестирование пустых списков

```bash
# Если кэш пуст, должен вернуть 200 с пустым списком
curl -X GET "http://localhost:8089/api/cache/shares"
```

### 3. Тестирование валидации UUID

```bash
# Неправильный формат UUID
curl -X GET "http://localhost:8089/api/orders/invalid-uuid"

# Правильный формат UUID (но несуществующий)
curl -X GET "http://localhost:8089/api/orders/123e4567-e89b-12d3-a456-426614174000"
```

## Отладка в режиме разработки

В dev режиме (`spring.profiles.active=dev`) ответы содержат дополнительное поле `trace` с полной трассировкой стека исключения.

```json
{
  "message": "Внутренняя ошибка сервера",
  "errorCode": "INTERNAL_SERVER_ERROR",
  "status": 500,
  "trace": "java.lang.RuntimeException: ...\n\tat com.example..."
}
```
