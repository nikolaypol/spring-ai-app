# Spring AI ChatGPT App

Spring Boot-приложение на Spring AI для работы с OpenAI-совместимыми чат-моделями.

## Требования

- Java 21+
- Maven 3.9+
- API-ключ OpenAI Platform или OpenRouter

## Настройка провайдера

Для OpenAI заполнить `.env` так:

```properties
AI_BASE_URL=https://api.openai.com
AI_API_KEY=your-openai-api-key
AI_MODEL=gpt-4o-mini
```

Для OpenRouter OpenAI-совместимый endpoint:

```properties
AI_BASE_URL=https://openrouter.ai/api
AI_API_KEY=your-openrouter-api-key
AI_MODEL=openai/gpt-4o-mini
```

Файл `.env` игнорируется git. Приложение импортирует его через
[application.yml](src/main/resources/application.yml):

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
  ai:
    openai:
      base-url: ${AI_BASE_URL:https://api.openai.com}
      api-key: ${AI_API_KEY:}
      chat:
        completions-path: /v1/chat/completions
        options:
          model: ${AI_MODEL:gpt-4o-mini}
```

Можно не использовать `.env`, а задать переменные окружения напрямую:

```bash
export AI_BASE_URL="https://api.openai.com"
export AI_API_KEY="your-openai-api-key"
export AI_MODEL="gpt-4o-mini"
```

## Как запустить
Запустить приложение:
```bash
mvn spring-boot:run
```

Отправить чат-запрос:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"Привет! Объясни Spring AI в двух предложениях."}'
```

Пример формата ответа:

```json
{
  "answer": "..."
}
```

Запустить тесты:
```bash
mvn test
```
