# Sock Inventory API
## Описание
API для управления инвентарем носок, включая регистрацию прихода и отпуска носок, получение носок по фильтрам, обновление данных носка и загрузку партий носок через файл. Реализовано с использованием Spring Boot и включает работу с базой данных через Spring Data JPA.

## Описание API
### 1. Register Sock Income
Регистрирует приход новой партии носок в инвентаре.

- Метод: POST
- URL: /api/socks/income
- Тело запроса: JSON, содержащий информацию о носке:

```json
{
  "color": "Red",
  "cottonContent": 80,
  "quantity": 100
}
```
- Ответ:
  - Код 200: "Income registered successfully"
  - Код 400: "Invalid request"
  - Код 409: "Conflict while searching for socks"
    
### 2. Register Sock Outcome
Регистрирует отпуск носок из инвентаря.
- Метод: POST
- URL: /api/socks/outcome
- Тело запроса: JSON, содержащий информацию о носке:
```json
{
  "color": "Red",
  "cottonContent": 80,
  "quantity": 50
}
```
- Ответ:
  - Код 200: "Outcome registered successfully"
  - Код 400: "Invalid request"
  - Код 409: "Not enough socks in stock"
    
### 3. Get Socks by Filters
Получить список носок с возможностью фильтрации по цвету, содержанию хлопка и сортировке.
- Метод: GET
- URL: /api/socks
- Параметры запроса:
  - color (необязательный) — цвет носка
  - operation (необязательный) — операция с содержанием хлопка: MORE_THAN, LESS_THAN, EQUAL
  - cottonContent (необязательный) — минимальное содержание хлопка
  - maxCottonContent (необязательный) — максимальное содержание хлопка
  - sortBy (необязательный) — сортировка (по умолчанию сортировка по содержанию хлопка)
- Ответ:
  - Код 200: JSON список носок.
  
### 4. Update Sock Data
Обновляет данные носка по его ID.
- Метод: PUT
- URL: /api/socks/{id}
- Параметры пути:
  - id — уникальный идентификатор носка
  - Тело запроса: JSON с обновленными данными:
```json
Копировать код
{
  "color": "Red",
  "cottonContent": 80,
  "quantity": 120
}
```
- Ответ:
  - Код 200: "Socks updated successfully"
  - Код 400: "Invalid request"
  - Код 409: "Sock not found"

### 5. Upload Socks Batch
Загружает партию носок через файл Excel.
- Метод: POST
- URL: /api/socks/batch
- Параметры запроса:
  - file — файл Excel, содержащий информацию о носках в формате:

| Color         |Cotton Part(%) | Quantity |
| ------------- | ------------- | -------- |
| Red           | 80            | 100      |
| Blue          | 75            | 100      |

- Ответ:
  - Код 200: "Batch uploaded successfully"
  - Код 413: "Payload Too Large"
    
## Технологии
- Java 17+
- Spring Boot
- Spring Data JPA
- Apache POI (для работы с Excel)
- Swagger (для документации API)
- JUnit 5 (для тестирования)
- Установка и запуск
