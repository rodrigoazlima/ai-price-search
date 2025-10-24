ProductController Endpoints Documentation
POST /api/v1/products

Description: Registers a new product to monitor its price.
Request Body (JSON):{
"url": "string",
"title": "string",
"desired_price": number,
"currency": "string"
}


Headers:
Authorization: JWT Bearer token (required).


Example:POST /api/v1/products
Authorization: Bearer <jwt_token>
{
"url": "https://example.com/product/123",
"title": "Smartphone XYZ",
"desired_price": 99.99,
"currency": "USD"
}


Response:
201: { "product_id": "prod123" }
400: Invalid input.
401: Unauthorized.
429: Rate limit exceeded.
500: Server error.


Logic: Authenticate user, validate/normalize inputs, save to MySQL, publish product.registered to Kafka, cache URL hash in Redis.

GET /api/v1/products

Description: Lists active products for the authenticated user.
Query Parameters:
limit (int, optional): Max results (default: 100).
offset (int, optional): Pagination offset (default: 0).


Headers:
Authorization: JWT Bearer token (required).


Example:GET /api/v1/products?limit=50&offset=0
Authorization: Bearer <jwt_token>


Response:
200:{
"products": [
{
"product_id": "prod123",
"url": "https://example.com/product/123",
"title": "Smartphone XYZ",
"desired_price": 99.99,
"currency": "USD",
"active_flag": true
}
]
}


401: Unauthorized.
429: Rate limit exceeded.
500: Server error.


Logic: Authenticate user, query MySQL for active products by user_id, return paginated results.

PUT /api/v1/products/{product_id}

Description: Updates product settings (desired price, active status).
Path Parameters:
product_id (string, required): Product identifier.


Request Body (JSON):{
"desired_price": number,
"active_flag": boolean
}


Headers:
Authorization: JWT Bearer token (required).


Example:PUT /api/v1/products/prod123
Authorization: Bearer <jwt_token>
{
"desired_price": 89.99,
"active_flag": true
}


Response:
200: { "product_id": "prod123" }
400: Invalid input.
401: Unauthorized.
403: Forbidden.
404: Product not found.
429: Rate limit exceeded.
500: Server error.


Logic: Authenticate user, verify product ownership, update MySQL, publish product.updated to Kafka.

DELETE /api/v1/products/{product_id}

Description: Deactivates a product (soft delete).
Path Parameters:
product_id (string, required): Product identifier.


Headers:
Authorization: JWT Bearer token (required).


Example:DELETE /api/v1/products/prod123
Authorization: Bearer <jwt_token>


Response:
204: No content.
401: Unauthorized.
403: Forbidden.
404: Product not found.
429: Rate limit exceeded.
500: Server error.


Logic: Authenticate user, verify product ownership, set active_flag=false in MySQL, publish product.deactivated to Kafka.
