Базовый URL: `http://localhost:8080/api/tasks`

- `GET /tasks` - Получить список задач (пагинация: `?page=0&size=20`)
- `GET /tasks/{id}` - Получить задачу по ID
- `POST /tasks` - Создать задачу (body: `{"name": "string", "description": "string"}`)
- `POST /tasks/{id}/assign` - Назначить исполнителя (body: `{"userId": 1}`)
- `PATCH /tasks/{id}/status` - Изменить статус (body: `{"status": "IN_PROGRESS"}`)

**Статусы задач:** `NEW`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

**Формат задачи:**
```json
{
    "id": 1,
    "name": "Название",
    "description": "Описание",
    "status": "NEW",
    "executor": {
        "id": 1,
        "name": "Имя",
        "email": "email@example.com"
    }
}
