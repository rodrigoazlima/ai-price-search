Below are the essential REST endpoints for the MVP of the AI Price Notifier, structured for an MVC (Model-View-Controller) architecture. These focus on core functionality: product management, price history, and alert settings, aligned with the simplified MVP design.

**Endpoints** (REST, JSON):

1. **POST /api/v1/products**
    - **Purpose**: Register a new product to monitor.
    - **Controller**: ProductController
    - **Request Body**: `{ "url": "string", "title": "string", "desired_price": number, "currency": "string" }`
    - **Response**: `201 { "product_id": "string" }`
    - **Logic**: Validate input, normalize URL, save to MySQL, publish `product.registered` to Kafka.

2. **GET /api/v1/products**
    - **Purpose**: List all active products for the authenticated user.
    - **Controller**: ProductController
    - **Query Params**: `?limit=int&offset=int`
    - **Response**: `200 { "products": [{ "product_id": "string", "url": "string", "title": "string", "desired_price": number, "currency": "string", "active_flag": boolean }] }`
    - **Logic**: Fetch from MySQL, filter by user_id (from JWT).

3. **GET /api/v1/products/{product_id}/history**
    - **Purpose**: Retrieve price history for a product.
    - **Controller**: PriceHistoryController
    - **Query Params**: `?from=timestamp&to=timestamp`
    - **Response**: `200 { "product_id": "string", "history": [{ "timestamp": "string", "price_cents": number, "currency": "string" }] }`
    - **Logic**: Query MySQL price_history, filter by product_id and time range.

4. **PUT /api/v1/products/{product_id}**
    - **Purpose**: Update product settings (e.g., desired price, active status).
    - **Controller**: ProductController
    - **Request Body**: `{ "desired_price": number, "active_flag": boolean }`
    - **Response**: `200 { "product_id": "string" }`
    - **Logic**: Update MySQL, publish update event to Kafka.

5. **DELETE /api/v1/products/{product_id}**
    - **Purpose**: Deactivate or delete a product.
    - **Controller**: ProductController
    - **Response**: `204 No Content`
    - **Logic**: Soft delete (set active_flag=false) in MySQL.

6. **POST /api/v1/alerts**
    - **Purpose**: Configure alert settings for a product.
    - **Controller**: AlertController
    - **Request Body**: `{ "product_id": "string", "desired_price": number, "min_interval_hours": number }`
    - **Response**: `201 { "alert_id": "string" }`
    - **Logic**: Save to MySQL alert_setting, link to user_id (from JWT).

7. **GET /api/v1/alerts**
    - **Purpose**: List alert settings for the authenticated user.
    - **Controller**: AlertController
    - **Query Params**: `?product_id=string` (optional)
    - **Response**: `200 { "alerts": [{ "alert_id": "string", "product_id": "string", "desired_price": number, "min_interval_hours": number }] }`
    - **Logic**: Query MySQL alert_setting, filter by user_id.

**MVC Structure**:
- **Model**:
    - `Product`: Maps to product table (id, url, title, desired_price, currency, active_flag).
    - `PriceHistory`: Maps to price_history table (product_id, timestamp, price_cents, currency).
    - `AlertSetting`: Maps to alert_setting table (id, product_id, user_id, desired_price, min_interval_hours).
- **View**: JSON responses (no UI rendering for API; Dashboard handled separately).
- **Controller**:
    - `ProductController`: Handles CRUD for products.
    - `PriceHistoryController`: Manages price history queries.
    - `AlertController`: Manages alert settings.

**Notes**:
- **Auth**: JWT middleware for all endpoints, extracting user_id.
- **Rate Limiting**: Apply via API Gateway (Redis-backed).
- **Error Handling**: Standard responses (400 for invalid input, 401 for auth errors, 404 for not found).
- **Validation**: URL format, positive numbers for prices, valid currency codes.
- **Kafka Integration**: ProductController and AlertController publish events (`product.registered`, `alert.updated`) for async processing.

This covers the minimal API surface for the MVP, focusing on core functionality while keeping the MVC pattern clean and extensible.