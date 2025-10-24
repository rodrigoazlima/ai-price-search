**MVP Architecture — AI Price Notifier**

**Overview**: Simplified distributed system to monitor product URLs, collect hourly prices, store in MySQL, apply basic rules for "buy" alerts, and notify via email. Uses Kafka for async communication, containerized in Kubernetes.

**Core Components**:
1. **API Gateway**:
   - Endpoints: Register product, view history, set thresholds.
   - JWT auth, basic rate-limiting.

2. **Product Service** (MySQL):
   - CRUD: product (id, url, title, desired_price, active_flag).
   - Basic URL normalization (remove UTM).

3. **Scheduler**:
   - Hourly cronjob, fetches active products, publishes `scrape.request` to Kafka.

4. **Scraper Workers**:
   - Consume `scrape.request`, use HTML parser (fallback to Playwright).
   - Extract price, availability; publish `price.event` to Kafka.

5. **Price History Writer**:
   - Consume `price.event`, store in MySQL (product_id, timestamp, price_cents).

6. **Alert Manager**:
   - Consume `price.event`, trigger if price ≤ desired_price.
   - Basic debounce (min interval between alerts).
   - Publish `notification.queue`.

7. **Notification Service**:
   - Email alerts with price, product link.

8. **Dashboard**:
   - Basic UI: product list, price history chart.

**Infra**:
- Kubernetes (basic setup, no HPA).
- Kafka (single topic: `scrape.request`, `price.event`, `notification.queue`).
- MySQL (single instance).
- Redis (rate-limiting, dedupe cache).
- Basic logging (ELK) + metrics (Prometheus).

**Data Models**:
- **product**: id, url, title, desired_price, currency, active_flag.
- **price_history**: product_id, timestamp, price_cents, currency.
- **alert_setting**: product_id, user_id, desired_price, min_interval_hours.

**Deduplication**:
- Basic: URL normalization, hash(url) for dedupe in Redis.

**MVP Checklist**:
- API for product CRUD, history.
- Hourly scheduler + simple scraper (HTML).
- Kafka for `scrape.request`, `price.event`, `notification.queue`.
- MySQL for price history.
- Basic alert rule (price ≤ desired_price) + email.
- Dashboard with price chart.
- Minimal observability (logs, basic metrics).

**Operational Notes**:
- Normalize URLs on registration.
- Simple retry for scraper failures.
- Test rate-limiting per domain.
- Basic security: HTTPS, JWT.

**Roadmap to Full Design**:
- Add normalizer/deduplicator service.
- Implement ML analytics.
- Scale scraper with proxies, headless browsers.
- Enhance observability, add traces.

**SLA**: Best-effort scraping, 99% API uptime.