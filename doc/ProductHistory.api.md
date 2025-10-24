PriceHistoryController Endpoints Documentation
GET /api/v1/products/{product_id}/history

Description: Retrieves the price history time series for a specific product, allowing users to view past price changes over a specified period.
Path Parameters:

product_id (string, required): Unique identifier of the product.


Query Parameters:

from (timestamp, optional): Start of the time range (e.g., "2023-01-01T00:00:00Z"). Defaults to 30 days ago.
to (timestamp, optional): End of the time range (e.g., "2023-12-31T23:59:59Z"). Defaults to current time.
limit (integer, optional): Maximum number of history entries to return (e.g., 100). Defaults to all within range.
sort (string, optional): Sort order ("asc" or "desc" by timestamp). Defaults to "asc".


Headers:

Authorization (string, required): JWT Bearer token for authentication.


Request Example:
textGET /api/v1/products/prod123/history?from=2023-01-01T00:00:00Z&to=2023-12-31T23:59:59Z&limit=50&sort=desc

Response:

Status Codes:

200 OK: Successful retrieval.
400 Bad Request: Invalid parameters (e.g., malformed timestamp).
401 Unauthorized: Missing or invalid JWT.
403 Forbidden: User lacks access to the product.
404 Not Found: Product not found.
500 Internal Server Error: Unexpected server issue.


Response Body (JSON):
text{
"product_id": "prod123",
"history": [
{
"timestamp": "2023-01-01T12:00:00Z",
"price_cents": 9999,
"currency": "USD"
},
{
"timestamp": "2023-01-02T12:00:00Z",
"price_cents": 9899,
"currency": "USD"
}
]
}



Logic:

Authenticate user via JWT and verify ownership/access to product_id.
Query MySQL price_history table: SELECT * WHERE product_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp [ASC/DESC] LIMIT ?.
Handle pagination if limit is set; convert prices to cents for consistency.