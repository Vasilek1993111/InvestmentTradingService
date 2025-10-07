# Тестирование новых методов "только из кэша"

## Быстрый старт

### 1. Запуск сервиса
```bash
mvn spring-boot:run
```

### 2. Проверка доступности
```bash
curl -X GET "http://localhost:8080/api/cache/statistics"
```

### 3. Прогрев кэша
```bash
curl -X POST "http://localhost:8080/api/cache/warm"
```

### 4. Тестирование новых эндпоинтов
```bash
# Все инструменты
curl -X GET "http://localhost:8080/api/cache/instruments"

# Только акции
curl -X GET "http://localhost:8080/api/cache/shares"

# Только фьючерсы
curl -X GET "http://localhost:8080/api/cache/futures"

# Только индикативы
curl -X GET "http://localhost:8080/api/cache/indicatives"
```

## Сценарии тестирования

### Сценарий 1: Пустой кэш
```bash
# 1. Очистка кэша
curl -X POST "http://localhost:8080/api/cache/clear"

# 2. Проверка пустого ответа
curl -X GET "http://localhost:8080/api/cache/instruments"
# Ожидаемый результат: пустые списки, total_instruments: 0

# 3. Проверка отдельных типов
curl -X GET "http://localhost:8080/api/cache/shares"
curl -X GET "http://localhost:8080/api/cache/futures"
curl -X GET "http://localhost:8080/api/cache/indicatives"
# Ожидаемый результат: пустые списки для всех
```

### Сценарий 2: Заполненный кэш
```bash
# 1. Прогрев кэша
curl -X POST "http://localhost:8080/api/cache/warm"

# 2. Проверка заполненного ответа
curl -X GET "http://localhost:8080/api/cache/instruments"
# Ожидаемый результат: списки с данными, total_instruments > 0

# 3. Проверка отдельных типов
curl -X GET "http://localhost:8080/api/cache/shares"
curl -X GET "http://localhost:8080/api/cache/futures"
curl -X GET "http://localhost:8080/api/cache/indicatives"
# Ожидаемый результат: списки с данными для всех
```

### Сценарий 3: Производительность
```bash
# Измерение времени отклика
time curl -X GET "http://localhost:8080/api/cache/instruments"
# Ожидаемый результат: < 100ms

time curl -X GET "http://localhost:8080/api/cache/shares"
# Ожидаемый результат: < 50ms
```

## Ожидаемые результаты

### Успешный ответ с данными
```json
{
  "success": true,
  "status": "success",
  "message": "Инструменты успешно получены из кэша",
  "shares_size": 169,
  "futures_size": 365,
  "indicatives_size": 57,
  "total_instruments": 591,
  "shares": [...],
  "futures": [...],
  "indicatives": [...],
  "timestamp": "2024-01-15T12:00:00"
}
```

### Успешный ответ с пустым кэшем
```json
{
  "success": true,
  "status": "success",
  "message": "Инструменты успешно получены из кэша",
  "shares_size": 0,
  "futures_size": 0,
  "indicatives_size": 0,
  "total_instruments": 0,
  "shares": [],
  "futures": [],
  "indicatives": [],
  "timestamp": "2024-01-15T12:00:00"
}
```

## Проверка логов

### Логи успешного получения
```
INFO  - Получен запрос на инструменты только из кэша
INFO  - Получены инструменты только из кэша: 169 акций, 365 фьючерсов, 57 индикативов
INFO  - Инструменты успешно получены из кэша: 169 акций, 365 фьючерсов, 57 индикативов
```

### Логи пустого кэша
```
INFO  - Получен запрос на инструменты только из кэша
INFO  - Кэш акций пуст, возвращаем пустой список
INFO  - Кэш фьючерсов пуст, возвращаем пустой список
INFO  - Кэш индикативов пуст, возвращаем пустой список
INFO  - Получены инструменты только из кэша: 0 акций, 0 фьючерсов, 0 индикативов
```

## Автоматизированное тестирование

### Использование Postman
1. Импортируйте коллекцию `Investment-Trading-Service.postman_collection.json`
2. Импортируйте переменные `Investment-Trading-Service-Test.postman_environment.json`
3. Запустите коллекцию `Investment-Trading-Service-Examples.postman_collection.json`

### Использование curl в скрипте
```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "Тестирование новых методов только из кэша..."

# Очистка кэша
echo "1. Очистка кэша..."
curl -X POST "$BASE_URL/api/cache/clear"

# Проверка пустого кэша
echo "2. Проверка пустого кэша..."
curl -X GET "$BASE_URL/api/cache/instruments" | jq '.total_instruments'

# Прогрев кэша
echo "3. Прогрев кэша..."
curl -X POST "$BASE_URL/api/cache/warm"

# Проверка заполненного кэша
echo "4. Проверка заполненного кэша..."
curl -X GET "$BASE_URL/api/cache/instruments" | jq '.total_instruments'

# Тестирование отдельных типов
echo "5. Тестирование отдельных типов..."
curl -X GET "$BASE_URL/api/cache/shares" | jq '.shares_size'
curl -X GET "$BASE_URL/api/cache/futures" | jq '.futures_size'
curl -X GET "$BASE_URL/api/cache/indicatives" | jq '.indicatives_size'

echo "Тестирование завершено!"
```

## Устранение неполадок

### Проблема: Сервис не отвечает
**Решение**: Проверьте, что сервис запущен на порту 8080

### Проблема: Пустой кэш после прогрева
**Решение**: Проверьте подключение к БД и наличие данных

### Проблема: Медленный отклик
**Решение**: Проверьте статистику кэша и производительность БД

### Проблема: Ошибки в логах
**Решение**: Проверьте конфигурацию кэша и права доступа к БД
