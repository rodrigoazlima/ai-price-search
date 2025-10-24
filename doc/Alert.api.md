### AlertController Endpoints Documentation

Below is the detailed documentation for the REST endpoints managed by the `AlertController` for the AI Price Notifier MVP, following the MVC architecture. These endpoints handle the creation and retrieval of alert settings for products, enabling users to configure price-based notifications.

#### 1. POST /api/v1/alerts

- **Description**: Configures a new alert setting for a product, specifying conditions for price notifications.
- **Request Body** (JSON, required):
  ```json
  {
    "product_id": "string", // Unique identifier of the product
    "desired_price": number, // Target price for alert (e.g., 99.99)
    "min_interval_hours": number // Minimum interval between alerts in hours (e.g., 24)
  }
  ```
- **Headers**:
    - `Authorization` (string, required): JWT Bearer token for authentication.
- **Request Example**:
  ```http
  POST /api/v1/alerts
  Authorization: Bearer <jwt_token>
  Content-Type: application/json
  {
    "product_id": "prod123",
    "desired_price": 89.99,
    "min_interval_hours": 24
  }
  ```
- **Response**:
    - **Status Codes**:
        - 201 Created: Alert setting successfully created.
        - 400 Bad Request: Invalid input (e.g., invalid product_id, negative price).
        - 401 Unauthorized: Missing or invalid JWT.
        - 403 Forbidden: User lacks access to the product.
        - 404 Not Found: Product not found.
        - 429 Too Many Requests: Rate limit exceeded.
        - 500 Internal Server Error: Unexpected server issue.
    - **Response Body** (JSON):
      ```json
      {
        "alert_id": "alert456"
      }
      ```
- **Logic**:
    - Authenticate user via JWT, extract `user_id`.
    - Validate input: `product_id` (exists in `product` table and belongs to `user_id`), `desired_price` (positive number), `min_interval_hours` (non-negative integer, e.g., 0-168).
    - Check if product exists and is active in MySQL `product` table.
    - Insert into MySQL `alert_setting` table: `(id, product_id, user_id, desired_price, min_interval_hours, created_at)`.
    - Publish `alert.created` event to Kafka with `{ alert_id, product_id, user_id, desired_price, min_interval_hours }`.
- **Error Handling**:
    - Return descriptive errors (e.g., `{"error": "Product not found"}`).
    - Log validation or database errors for debugging.
- **Security**:
    - Rate-limited (e.g., 50 requests/hour per user).
    - Ensure `product_id` belongs to `user_id` to prevent unauthorized access.
    - Sanitize inputs to prevent injection.
    - HTTPS enforced for data in transit.
- **Performance Notes**:
    - Index `product_id` and `user_id` in `alert_setting` table for fast inserts and lookups.
    - Cache product existence check in Redis to reduce MySQL queries.

#### 2. GET /api/v1/alerts

- **Description**: Lists alert settings for the authenticated user, optionally filtered by product.
- **Query Parameters**:
    - `product_id` (string, optional): Filter alerts by specific product ID.
    - `limit` (integer, optional): Max number of alerts to return (e.g., 50). Defaults to 100.
    - `offset` (integer, optional): Pagination offset (e.g., 0). Defaults to 0.
- **Headers**:
    - `Authorization` (string, required): JWT Bearer token.
- **Request Example**:
  ```http
  GET /api/v1/alerts?product_id=prod123&limit=50&offset=0
  Authorization: Bearer <jwt_token>
  ```
- **Response**:
    - **Status Codes**:
        - 200 OK: Successful retrieval.
        - 401 Unauthorized: Missing or invalid JWT.
        - 400 Bad Request: Invalid query parameters (e.g., negative limit).
        - 429 Too Many Requests: Rate limit exceeded.
        - 500 Internal Server Error: Unexpected server issue.
    - **Response Body** (JSON):
      ```json
      {
        "alerts": [
          {
            "alert_id": "alert456",
            "product_id": "prod123",
            "desired_price": 89.99,
            "min_interval_hours": 24
          },
          {
            "alert_id": "alert789",
            "product_id": "prod124",
            "desired_price": 499.99,
            "min_interval_hours": 12
          }
        ]
      }
      ```
- **Logic**:
    - Authenticate user via JWT, extract `user_id`.
    - Query MySQL `alert_setting` table: `SELECT * WHERE user_id = ? [AND product_id = ?] LIMIT ? OFFSET ?`.
    - If `product_id` is provided, verify it exists and belongs to `user_id`.
    - Return paginated results.
- **Error Handling**:
    - Validate `limit` (1-100), `offset` (non-negative), `product_id` (valid format).
    - Return `{"error": "Invalid pagination parameters"}` or `{"error": "Product not found"}`.
    - Log query errors for debugging.
- **Security**:
    - Filter by `user_id` to prevent unauthorized access.
    - Rate-limited (e.g., 100 requests/hour per user).
    - HTTPS enforced.
- **Performance Notes**:
    - Index `user_id` and `product_id` in `alert_setting` table for efficient filtering.
    - Cache frequent queries (e.g., user’s alert list) in Redis for read-heavy workloads.

**MVC Structure**:
- **Model**:
    - `AlertSetting`: Maps to `alert_setting` table (id, product_id, user_id, desired_price, min_interval_hours, created_at).
- **View**: JSON responses for API, no UI rendering (Dashboard handled separately).
- **Controller**:
    - `AlertController`: Manages creation and listing of alert settings, validates inputs, interacts with MySQL, publishes Kafka events.

**Additional Notes**:
- **Authentication**: JWT middleware extracts `user_id` and validates token for all endpoints.
- **Rate Limiting**: Enforced via Redis-backed middleware at the API Gateway (e.g., 100 requests/hour for GET, 50 for POST).
- **Validation**:
    - `product_id`: Must exist in `product` table and belong to `user_id`.
    - `desired_price`: Positive number, 2 decimal places.
    - `min_interval_hours`: Non-negative integer (e.g., 0-168).
- **Kafka Integration**: Publishes `alert.created` event for async processing by Alert Manager.
- **Error Handling**: Standardized JSON error responses (e.g., `{"error": "message"}`), logged for debugging.
- **Security**:
    - HTTPS enforced.
    - Input sanitization to prevent SQL injection or XSS.
    - User-level isolation (filter by `user_id`).
- **Performance**:
    - Use MySQL indexes on `user_id`, `product_id` for fast queries.
    - Cache product validation results in Redis to avoid repeated MySQL checks.
    - Optimize pagination for large alert lists.

<xaiArtifact artifact_id="16306462-5cce-480f-bc90-4ba94a3f0bf9" artifact_version_id="8683db80-c1ef-459d-83a4-cb0939ba9ea5" title="AlertController_Documentation.md" contentType="text/markdown">

# AlertController Endpoints Documentation

## POST /api/v1/alerts

- **Description**: Configures a new alert setting for a product.
- **Request Body** (JSON):
  ```json
  {
    "product_id": "string",
    "desired_price": number,
    "min_interval_hours": number
  }
  ```
- **Headers**:
    - `Authorization`: JWT Bearer token (required).
- **Example**:
  ```http
  POST /api/v1/alerts
  Authorization: Bearer <jwt_token>
  {
    "product_id": "prod123",
    "desired_price": 89.99,
    "min_interval_hours": 24
  }
  ```
- **Response**:
    - **201**: `{ "alert_id": "alert456" }`
    - **400**: Invalid input.
    - **401**: Unauthorized.
    - **403**: Forbidden.
    - **404**: Product not found.
    - **429**: Rate limit exceeded.
    - **500**: Server error.
- **Logic**: Authenticate user, validate inputs, verify product ownership, save to MySQL, publish `alert.created` to Kafka.

## GET /api/v1/alerts

- **Description**: Lists alert settings for the authenticated user, optionally filtered by product.
- **Query Parameters**:
    - `product_id` (string, optional): Filter by product ID.
    - `limit` (int, optional): Max results (default: 100).
    - `offset` (int, optional): Pagination offset (default: 0).
- **Headers**:
    - `Authorization`: JWT Bearer token (required).
- **Example**:
  ```http
  GET /api/v1/alerts?product_id=prod123&limit=50&offset=0
  Authorization: Bearer <jwt_token>
  ```
- **Response**:
    - **200**:
      ```json
      {
        "alerts": [
          {
            "alert_id": "alert456",
            "product_id": "prod123",
            "desired_price": 89.99,
            "min_interval_hours": 24
          }
        ]
      }
      ```
    - **400**: Invalid query parameters.
    - **401**: Unauthorized.
    - **429**: Rate limit exceeded.
    - **500**: Server error.
- **Logic**: Authenticate user, query MySQL for alert settings by user_id and optional product_id, return paginated results.

</xaiArtifact>