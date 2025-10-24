# LM Studio OpenAI‑Compatible API Guide

This document summarizes how to call LM Studio’s OpenAI‑compatible HTTP endpoints from curl, Python, and Java (OpenFeign). It is based on LM Studio’s official documentation and tailored to this project’s usage.

- Base URL: http://localhost:1234/v1
- API Key: Not strictly required by LM Studio by default, but many SDKs expect a token. Use any non‑empty value (e.g., "lm-studio"). In this project we configure it via application.yml.

Tip: Keep a terminal running with `lms log stream` to inspect prompts sent to the model.

## Supported endpoints

| Endpoint              | Method | Description          |
|----------------------|--------|----------------------|
| /v1/models           | GET    | List available models|
| /v1/responses        | POST   | Responses (stateful, streaming, tools/MCP)
| /v1/chat/completions | POST   | Chat Completions     |
| /v1/embeddings       | POST   | Embeddings           |
| /v1/completions      | POST   | Legacy Completions   |

---

## 1) List Models
List all visible models. When Just‑In‑Time loading is enabled, the server may return models not yet loaded into memory.

Method: GET

curl
```
curl http://localhost:1234/v1/models
```

Example response (abbreviated):
```
{
  "data": [
    { "id": "qwen2.5:7b-instruct-q4_k_m", "object": "model" },
    { "id": "llama-3.1-8b-instruct", "object": "model" }
  ],
  "object": "list"
}
```

---

## 2) Responses
Create responses with support for streaming, reasoning, and prior response state. Optionally invoke Remote MCP tools if enabled in LM Studio (Developer → Settings).

OpenAI docs reference: https://platform.openai.com/docs/api-reference/responses

Method: POST

Basic (non‑streaming)
```
curl http://localhost:1234/v1/responses \
  -H "Content-Type: application/json" \
  -d '{
    "model": "openai/gpt-oss-20b",
    "input": "Provide a prime number less than 50",
    "reasoning": { "effort": "low" }
  }'
```

Stateful follow‑up (use the id from a previous response)
```
curl http://localhost:1234/v1/responses \
  -H "Content-Type: application/json" \
  -d '{
    "model": "openai/gpt-oss-20b",
    "input": "Multiply it by 2",
    "previous_response_id": "resp_123"
  }'
```

Streaming (Server‑Sent Events)
```
# Note the -N flag to disable curl buffering
curl -N http://localhost:1234/v1/responses \
  -H "Content-Type: application/json" \
  -d '{
    "model": "openai/gpt-oss-20b",
    "input": "Hello",
    "stream": true
  }'
```
You will receive SSE events such as `response.created`, `response.output_text.delta`, and `response.completed`.

Tools and Remote MCP (opt‑in)
```
curl http://localhost:1234/v1/responses \
  -H "Content-Type: application/json" \
  -d '{
    "model": "openai/gpt-oss-20b",
    "tools": [{
      "type": "mcp",
      "server_label": "tiktoken",
      "server_url": "https://gitmcp.io/openai/tiktoken",
      "allowed_tools": ["fetch_tiktoken_documentation"]
    }],
    "input": "What is the first sentence of the tiktoken documentation?"
  }'
```

---

## 3) Chat Completions
Send a chat history and get the assistant’s reply. LM Studio applies a prompt template automatically for chat‑tuned models.

OpenAI docs reference: https://platform.openai.com/docs/api-reference/chat

Method: POST

Python (OpenAI SDK)
```
from openai import OpenAI
client = OpenAI(base_url="http://localhost:1234/v1", api_key="lm-studio")

completion = client.chat.completions.create(
  model="model-identifier",
  messages=[
    {"role": "system", "content": "Always answer in rhymes."},
    {"role": "user", "content": "Introduce yourself."}
  ],
  temperature=0.7,
)

print(completion.choices[0].message)
```

curl
```
curl http://localhost:1234/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "model-identifier",
    "messages": [
      {"role": "system", "content": "Always answer in rhymes."},
      {"role": "user", "content": "Introduce yourself."}
    ],
    "temperature": 0.7
  }'
```

Supported payload parameters (common ones)
```
model
messages
max_tokens
temperature
top_p
top_k
stream
stop
presence_penalty
frequency_penalty
logit_bias
repeat_penalty
seed
```

Tip: Keep `lms log stream` open to inspect the final prompt after the template is applied for chat‑tuned models.

---

## 4) Embeddings
Generate embedding vectors from input text.

OpenAI docs reference: https://platform.openai.com/docs/api-reference/embeddings

Method: POST

Python
```
from openai import OpenAI
client = OpenAI(base_url="http://localhost:1234/v1", api_key="lm-studio")

def get_embedding(text, model="model-identifier"):
  text = text.replace("\n", " ")
  return client.embeddings.create(input=[text], model=model).data[0].embedding

print(get_embedding("Once upon a time, there was a cat."))
```

curl
```
curl http://localhost:1234/v1/embeddings \
  -H "Content-Type: application/json" \
  -d '{
    "model": "model-identifier",
    "input": ["Once upon a time, there was a cat."]
  }'
```

---

## 5) Completions (Legacy)
Text completion for base models (legacy OpenAI endpoint). Heads‑up: OpenAI no longer supports this endpoint, but LM Studio does. Using chat‑tuned models here may produce unexpected tokens. Prefer base models.

Method: POST

curl
```
curl http://localhost:1234/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "base-model-id",
    "prompt": "Write a short haiku about rain.",
    "max_tokens": 64,
    "temperature": 0.7
  }'
```

---

## Java (Spring OpenFeign) usage in this project
This project integrates with LM Studio via a Spring Cloud OpenFeign client (`LmStudioClient`) and DTOs under `dto.lmstudio`.

Configuration (application.yml)
```
lmstudio:
  api:
    url: http://localhost:1234
    api-key: ${LMSTUDIO_API_KEY:lm-studio}
```
Note: The Feign client typically points to the base server URL; for OpenAI‑compatible calls include `/v1` in the request mappings of the Feign interface.

Example request objects used by this project (abbreviated):
- ChatCompletionRequestDTO: holds `model`, `messages`, `temperature`, etc.
- EmbeddingRequestDTO: holds `model` and `input`.
- ResponseRequestDTO: holds `model`, `input`, optional `reasoning`, `stream`, and `previous_response_id`.

Service usage (pseudo‑example)
```
@Autowired
private LmStudioClient lmStudioClient;

var req = ChatCompletionRequestDTO.builder()
  .model("model-identifier")
  .message(new ChatMessageDTO("user", "Introduce yourself."))
  .temperature(0.7)
  .build();

var completion = lmStudioClient.createChatCompletion(req);
System.out.println(completion.getChoices().get(0).getMessage().getContent());
```

---

## Troubleshooting & Tips
- 404: Ensure you’re hitting `/v1/...` paths. The base server root is usually `http://localhost:1234`, while the OpenAI‑compatible API is under `/v1`.
- 401/No API key: LM Studio often accepts any token. Provide `api_key`/`Authorization: Bearer lm-studio` if your HTTP client requires it.
- Model not found: Confirm the model id from `/v1/models`. If using Just‑In‑Time loading, allow LM Studio to fetch and prepare the model.
- Streaming parsing: Use `-N` with curl or an SSE client for `stream: true`. Expect events like `response.output_text.delta` and a final `response.completed`.
- Prompt templates: For chat‑tuned models, LM Studio applies a template automatically for Chat Completions; base Completions do not apply templates.
- Performance: Control decoding with `temperature`, `top_p`, `top_k`, `repeat_penalty`, and `max_tokens` to balance quality and speed.

---

References
- LM Studio OpenAI‑compatible API concepts follow OpenAI’s endpoints: https://platform.openai.com/docs
- LM Studio app: Developer → Settings for tools/MCP and logging options.
