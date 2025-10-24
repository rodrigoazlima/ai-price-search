# Arquitetura técnica — AI Price Notifier (microservices, Kafka, MySQL)

## Visão geral

Sistema distribuído para monitorar URLs de produtos, coletar preços hora a hora, normalizar/identificar duplicatas, armazenar histórico em MySQL, processar séries temporais com análise/ML e emitir notificações quando for “melhor momento para comprar”. Comunicação assíncrona via Apache Kafka; serviços desacoplados, containerizados e orquestrados em Kubernetes.

## Componentes principais (serviços)

1. **API Gateway / Management API**

   * Endpoints: registrar produto, listar produtos, atualizar thresholds, ver histórico, desativar.
   * Autenticação: JWT + RBAC.
   * Rate-limit por usuário/cliente.

2. **Product Service** (MySQL primary)

   * CRUD de produtos, metadados, URL canonical, estado (active/paused).
   * Normalização de URLs, extração de domínio, dedup key.
   * Gera mensagens `product.registered` para Kafka.

3. **Scheduler / Orchestrator**

   * Orquestra checks hora a hora (pod baseado em cronjobs ou job queue).
   * Consome produto ativo da DB, agrupa por domínio e prioridade, publica `scrape.request` no Kafka.
   * Garante politicas de rate limit por domínio.

4. **Scraper Workers**

   * Escalam horizontalmente; consomem `scrape.request`.
   * Estratégia: headless browser (Playwright) + fallback HTML parser.
   * Implementa fingerprinting de DOM para detectar mudanças; extrai preço, disponibilidade, SKU, imagens.
   * Publica `price.event` (raw + normalized) em Kafka.
   * Retries idempotentes; detecta captive pages e bloqueios.

5. **Normalizer / Deduplicator Service**

   * Consome `price.event`.
   * Normaliza moeda, unidade, remove ruído, extrai atributos (sku, model, title).
   * Dedup: gera `product_signature` (hash de SKU+normalized_title+model).
   * Se assinatura existente → liga ao product_id; se novo → cria candidate e publica `product.duplicate_check`/`product.candidate`.
   * Atualiza MySQL mapping SKU ↔ product_id.

6. **Price History Writer**

   * Consome `price.event` já normalizado; grava `price_history` em MySQL (write-optimized).
   * Partitioning por product_id + time window, compressão, TTL policy.

7. **Analytics / AI Engine**

   * Consome stream de `price.event` e histórico via materialized store.
   * Executa: detecção de anomalias, moving averages, seasonal patterns, price elasticity, ML que prevê probabilidade de queda nos próximos N dias.
   * Calcula score “momento de compra” e sinais: `alert.candidate` em Kafka.

8. **Alert Manager / Rule Engine**

   * Consome `alert.candidate` e aplica regras do usuário (absolute price, % drop, hysteresis para evitar spam).
   * Debounce + dedupe: mantém última_notified_price e janela mínima entre alertas.
   * Emite `notification.queue` com payload por canal.

9. **Notification Service**

   * Envia via: e-mail, SMS, push, Telegram, Slack, Webhooks.
   * Template com snapshot: preço atual, histórico min/max, link, motivo do alerta.
   * Persistência de logs de envio + retries.

10. **Admin / Dashboard**

    * Visualização de produtos, gráficos (price over time), alertas, problemas de scraping, health metrics.
    * Ferramentas de correção manual para mapping de duplicatas.

11. **Observability**

    * Metrics: Prometheus + Grafana; traces: Jaeger; logs: ELK/EFK.
    * Alertas infra: scraper-fail-rate, queue-lag, consumer-lag, DB errors.

## Infra e deploy

* **Kubernetes** (GKE/EKS/AKS).
* **Container images** no registry (Docker).
* **Kafka** (cluster, replication factor 3) + Schema Registry (Avro/Protobuf).
* **MySQL** (primary-replica) ou Amazon Aurora; read replicas para analytics.
* **Redis** para cache de dedupe, rate-limit buckets, lock distribuído.
* **Object storage** (S3) para snapshots e imagens.
* **CI/CD**: pipelines (GitHub Actions/GitLab CI) com lint, tests, build, deploy.
* **Secrets**: Vault/Kubernetes secrets.

## Modelos de dados (essencial)

* **product**: id, canonical_url, domain, title, desired_price, threshold_pct, currency, active_flag, created_at, last_checked_at, duplicate_signature.
* **product_mapping**: product_id, sku, vendor_id, external_id, signature_hash.
* **price_history**: id, product_id, timestamp, price_cents, currency, availability, vendor, raw_payload_reference.
* **alert_setting**: id, user_id, product_id, channel, threshold_type, threshold_value, min_interval_hours.
* **alert_log**: id, product_id, user_id, timestamp, price_when_alerted, reason, notification_status.

## Kafka topics (exemplo)

* `product.registered` (avro)
* `scrape.request` (key=domain)
* `price.raw`
* `price.normalized`
* `product.candidate`
* `analytics.signals`
* `alert.candidate`
* `notification.queue`
* `audit.events`

## Formato de mensagens (exemplo JSON/Avro)

* `scrape.request`: { request_id, product_id, url, domain, priority, attempt }
* `price.normalized`: { product_signature, product_id?, timestamp, price_cents, currency, availability, vendor, sku, title, url, raw_ref }
* `alert.candidate`: { product_id, score, reason, price_cents, timestamp, window_stats }

## Estratégia de deduplicação e matching

1. **Normalization pipeline**: lowercasing, strip punctuation, remove stopwords, map synonyms (GB vs UK), remove seller tags.
2. **Signature**: hash(normalized_title + normalized_sku + normalized_model).
3. **Fuzzy match**: Levenshtein + token set ratio para detectar variações; threshold ajustável.
4. **Manual override**: dashboard para unir/sep product_ids se falso positivo.
5. **URL dedupe**: canonicalização (remove utm, session ids); store canonical_url_hash and index.
6. **Idempotency**: each scraped event has `external_id` and scraper maintains seen-set in Redis for X hours to avoid duplicates.

## Agendamento e escala de checks

* **Granularidade**: ciclo base = 1 hora; produtos de alta prioridade podem ter check menor (ex.: 15min) com custo extra.
* **Batching por domínio**: scheduler agrupa requests por domínio para respeitar politicas.
* **Backoff**: se bloqueado, exponencial backoff para domain + rotate proxies.
* **Concurrency**: max concurrent per domain controlado pelo Scheduler.
* **Horizontal scale**: Kubernetes HPA baseado em lag do Kafka consumer/CPU.

## Regras para evitar alert spam

* Hysteresis: notificar apenas se: preço <= target **AND** variação >= configured_pct **AND** diferença vs last_alert >= min_change.
* Window consolidation: agrupar quedas dentro de N minutos e enviar uma notificação consolidada.
* Max alerts/day por produto por usuário.

## AI / Analytics (detalhe prático)

* **Features**: price history windows (7/30/90d), volatility, vendor frequency, time-of-day patterns, discount events.
* **Models**: LightGBM para probabilidade de queda; ARIMA/Prophet for seasonality; anomaly detector (isolation forest) para outliers.
* **Output**: probability_down_7d, suggested_action (buy/hold/watch), confidence.
* **Retraining**: batch diário com dataset rotulado por resultados (queda confirmada ou não).

## Resiliência e consistência

* **Exactly-once/At-least-once**: idempotent writers (price_history inserts guarded por unique(product_id, timestamp, source_id)).
* **Schema evolution**: use Schema Registry.
* **Data retention**: price_history hot (90 days), cold archive (S3) por 5 anos.
* **Backfill**: data pipeline para reprocessar eventos raw se precisar recalcular ML.

## Segurança e compliance

* CORS + rate limits na API.
* HTTPS everywhere.
* Proteção contra scraping abuse: rotate proxies, CAPTCHA handling manual fallback.
* GDPR/CCPA: opção para deletar dados por usuário, retenção mínima, logs de consentimento.

## Observability e operações

* Dashboards: scraper success rate, consumer lag, alert rate, false-alert rate.
* Health probes e auto-restart.
* Playbook: bloquear domínio → isolate consumers → notify ops → rotate proxies.

## Roadmap de implementação (tarefas / milestones)

1. Schema + repo + infra IaC (k8s manifests, helm).
2. Product Service + MySQL models + Management API.
3. Simple Scheduler (cron) + Scraper worker mínimo (HTML fetch + parser).
4. Price History Writer + basic Dashboard (price chart).
5. Kafka integration + Schema Registry.
6. Normalizer/Deduplicator + Redis cache.
7. Analytics basic rules (moving average, % drop).
8. Alert Manager + Notification Service (email).
9. Harden scraping (Playwright, proxies), rate limiting.
10. ML pipeline + retraining infra.
11. Monitoring, autoscale, security hardening, production rollout.

## Contratos operacionais e políticas

* **SLA**: 99.9% for API, best-effort for scraping.
* **Rate limits**: configurable per account.
* **Terms**: respeito a TOS dos sites monitorados; opção enterprise com vendor APIs (preferred).

## Exemplo de endpoint essencial (REST)

* `POST /v1/products` { url, desired_price, threshold_pct, check_interval } → returns product_id
* `GET /v1/products/{id}/history?from=&to=` → time series

## Checklist técnico mínimo para MVP

* API + Product CRUD, MySQL.
* Scheduler hourly, simple scraper (HTML).
* Kafka basic topics (`scrape.request`, `price.normalized`).
* Price history persistence.
* Basic alert rule (price <= desired_price) + email notifications.
* Dashboard com gráfico de histórico.
* Observability mínima (logs + metrics).

## Observações operacionais rápidas (diretas)

* Normalize e indexe URLs imediatamente ao registrar.
* Priorize dedupe via SKU/signature antes de confiar em NLP fuzzy match.
* Use Kafka partitions por domínio para garantir ordenação local.
* Implemente debounce/hysteresis no Alert Manager para evitar ruído.
* Teste bloqueios e rotas de fallback (proxy pool + user agents).

Fim.