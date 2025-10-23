# Алгоритм расчета заявки Investment Trading Service

## 📋 Обзор

Данный документ описывает алгоритм расчета цены и количества инструментов в заявке для Investment Trading Service. Алгоритм обеспечивает корректное формирование заявок с учетом требований биржи и защиты от превышения бюджета.

## 🎯 Цели алгоритма

1. **Безопасность**: Пользователь никогда не потратит больше запланированной суммы
2. **Соответствие биржевым требованиям**: Цены кратны минимальному шагу цены
3. **Учет лотности инструментов**: Корректное количество лотов с учетом размера лота инструмента
4. **Консервативный подход**: Округление в меньшую сторону для защиты от превышения бюджета

## 📊 Структура данных

### Входные параметры
- `amount` - общая сумма для инвестирования
- `levelsCount` - количество уровней
- `direction` - направление торговли (BUY/SELL)
- `percentage` - процент отклонения цены для каждого уровня
- `instrumentId` - идентификатор инструмента (FIGI)

### Параметры инструмента
- `instrumentPrice` - текущая цена инструмента
- `minPriceIncrement` - минимальный шаг цены
- `lot` - размер лота инструмента (количество единиц в одном лоте)

## 🔢 Алгоритм расчета

### Шаг 1: Расчет базовой суммы за уровень

```
priceForLevel = amount / levelsCount
```

**Пример:**
- `amount = 100,000 RUB`
- `levelsCount = 5`
- `priceForLevel = 100,000 / 5 = 20,000 RUB`

### Шаг 2: Расчет цены с учетом направления и процента

```
decimalPercentage = percentage / 100
priceChange = instrumentPrice × decimalPercentage

Для покупки (BUY):
basePrice = instrumentPrice - priceChange

Для продажи (SELL):
basePrice = instrumentPrice + priceChange
```

**Пример:**
- `instrumentPrice = 250 RUB`
- `percentage = 2%`
- `direction = BUY`
- `decimalPercentage = 2 / 100 = 0.02`
- `priceChange = 250 × 0.02 = 5 RUB`
- `basePrice = 250 - 5 = 245 RUB`

### Шаг 3: Применение лимитов биржи

```
limitDown = getLimitDown(instrumentId)
limitUp = getLimitUp(instrumentId)

adjustedPrice = max(min(basePrice, limitUp), limitDown)
```

**Пример:**
- `limitDown = 240 RUB`
- `limitUp = 260 RUB`
- `basePrice = 245 RUB`
- `adjustedPrice = max(min(245, 260), 240) = 245 RUB`

### Шаг 4: Округление до шага цены

```
minPriceIncrement = getMinPriceIncrement(instrumentId)
steps = floor(adjustedPrice / minPriceIncrement)
finalPrice = steps × minPriceIncrement
```

**Пример:**
- `minPriceIncrement = 0.01 RUB`
- `adjustedPrice = 245.00 RUB`
- `steps = floor(245.00 / 0.01) = 24500`
- `finalPrice = 24500 × 0.01 = 245.00 RUB`

### Шаг 5: Расчет лотности с учетом размера лота инструмента

```
baseLotSize = floor(priceForLevel / finalPrice)
instrumentLot = getLot(instrumentId)
finalLotSize = floor(baseLotSize / instrumentLot)
```

**Пример:**
- `priceForLevel = 20,000 RUB`
- `finalPrice = 245 RUB`
- `instrumentLot = 10`
- `baseLotSize = floor(20,000 / 245) = 81`
- `finalLotSize = floor(81 / 10) = 8`

### Шаг 6: Проверка итоговой суммы

```
totalAmount = finalLotSize × instrumentLot × finalPrice
remainder = priceForLevel - totalAmount
```

**Пример:**
- `finalLotSize = 8`
- `instrumentLot = 10`
- `finalPrice = 245 RUB`
- `totalAmount = 8 × 10 × 245 = 19,600 RUB`
- `remainder = 20,000 - 19,600 = 400 RUB`

## 📈 Полный пример расчета

### Исходные данные
```
amount = 100,000 RUB
levelsCount = 5
direction = BUY
percentage = 2%
instrumentId = "BBG004730N88" (SBER)
```

### Параметры инструмента
```
instrumentPrice = 250 RUB
minPriceIncrement = 0.01 RUB
lot = 10
```

### Расчет для одного уровня

| Шаг | Формула | Результат |
|-----|---------|-----------|
| 1. Базовая сумма | `100,000 / 5` | `20,000 RUB` |
| 2. Изменение цены | `250 × 0.02` | `5 RUB` |
| 3. Базовая цена | `250 - 5` | `245 RUB` |
| 4. Итоговая цена | `245.00` | `245.00 RUB` |
| 5. Базовая лотность | `floor(20,000 / 245)` | `81` |
| 6. Итоговая лотность | `floor(81 / 10)` | `8` |
| 7. Итоговая сумма | `8 × 10 × 245` | `19,600 RUB` |
| 8. Остаток | `20,000 - 19,600` | `400 RUB` |

### Результат
- **Количество лотов в заявке**: 8
- **Итоговая сумма**: 19,600 RUB
- **Остаток**: 400 RUB (не потрачен)

## 🔍 Особые случаи

### Случай 1: Недостаточная сумма для покупки лота

**Исходные данные:**
```
priceForLevel = 1,000 RUB
finalPrice = 300 RUB
instrumentLot = 10
```

**Расчет:**
```
baseLotSize = floor(1,000 / 300) = 3
finalLotSize = floor(3 / 10) = 0
```

**Результат:** Заявка не создается (недостаточно средств)

### Случай 2: Цена не кратна шагу

**Исходные данные:**
```
adjustedPrice = 245.67 RUB
minPriceIncrement = 0.01 RUB
```

**Расчет:**
```
steps = floor(245.67 / 0.01) = 24567
finalPrice = 24567 × 0.01 = 245.67 RUB
```

**Результат:** Цена округляется до 245.67 RUB (кратна шагу)

### Случай 3: Превышение лимитов биржи

**Исходные данные:**
```
basePrice = 270 RUB
limitUp = 260 RUB
```

**Расчет:**
```
adjustedPrice = min(270, 260) = 260 RUB
```

**Результат:** Цена ограничена верхним лимитом

## ⚠️ Важные особенности

### 1. Консервативное округление
- Используется `floor()` для лотности - гарантирует, что сумма не превысит бюджет
- Используется `floor()` для округления цены до шага - обеспечивает соответствие биржевым требованиям

### 2. Защита от превышения бюджета
- Остаток средств остается неиспользованным
- Это гарантирует, что пользователь не потратит больше запланированного

### 3. Соответствие биржевым требованиям
- Цена всегда кратна минимальному шагу цены
- Количество лотов учитывает размер лота инструмента

### 4. Обработка ошибок
- Если размер лота инструмента не найден, используется базовая лотность
- Если минимальный шаг цены не найден, используется исходная цена
- Если лимиты биржи недоступны, используются рассчитанные цены

## 🧮 Математические формулы

### Основные формулы
```
priceForLevel = amount / levelsCount
priceChange = instrumentPrice × (percentage / 100)
basePrice = instrumentPrice ± priceChange
adjustedPrice = max(min(basePrice, limitUp), limitDown)
finalPrice = floor(adjustedPrice / minPriceIncrement) × minPriceIncrement
baseLotSize = floor(priceForLevel / finalPrice)
finalLotSize = floor(baseLotSize / instrumentLot)
totalAmount = finalLotSize × instrumentLot × finalPrice
```

### Формулы округления
```
steps = floor(price / minPriceIncrement)
roundedPrice = steps × minPriceIncrement

lotRatio = floor(amount / price)
instrumentLots = floor(lotRatio / instrumentLot)
```

## 📝 Логирование

Алгоритм включает подробное логирование на каждом этапе:

```
DEBUG: Расчет для уровня 1: instrumentPrice=250.00, levelPercentage=2.00, direction=BUY, adjustedPrice=245.00
INFO: Цена округлена до шага: 245.00 -> 245.00 (шаг: 0.01, инструмент: BBG004730N88)
INFO: Расчет лотности: baseLotSize=81, instrumentLot=10, finalLotSize=8, инструмент: BBG004730N88
DEBUG: Создана заявка: инструмент=BBG004730N88, направление=BUY, уровень=1, цена=245.00, лотность=8
```

## 🔧 Техническая реализация

### Основные методы
- `calculatePriceWithDirection()` - расчет цены с учетом направления
- `applyLimitsToPrice()` - применение лимитов и округления
- `calculateLotSize()` - расчет лотности с учетом размера лота
- `calculateAjustedPriceWithMinPriceIncrement()` - округление до шага цены

### Зависимости
- `InstrumentServiceFacade` - получение параметров инструмента
- `TInvestApiService` - получение лимитов биржи
- `PriceRoundingUtil` - утилиты округления (если используется)

## 📚 Заключение

Данный алгоритм обеспечивает:
- ✅ Безопасность торговли (не превышение бюджета)
- ✅ Соответствие биржевым требованиям
- ✅ Корректный учет лотности инструментов
- ✅ Подробное логирование для отладки
- ✅ Обработку ошибок и исключительных ситуаций

Алгоритм протестирован на различных сценариях и готов к использованию в продакшене.

---

**Версия документа:** 1.0.0  
**Дата обновления:** 2025-10-17  
**Автор:** Investment Trading Service Team
