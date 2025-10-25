package dev.rodrigoazlima.poc.ai.pricenotifier.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Endpoints to manage tracked products")
public class ProductController {

    @PostMapping
    @Operation(summary = "Register a new product", description = "Registers a new product to monitor its price")
    public ResponseEntity<Map<String, String>> createProduct(@RequestBody Map<String, Object> body) {
        String productId = UUID.randomUUID().toString();
        Map<String, String> response = Map.of("product_id", productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List products", description = "Lists active products for the authenticated user")
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(name = "limit", required = false, defaultValue = "100")
            @Parameter(description = "Max results") int limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0")
            @Parameter(description = "Pagination offset") int offset) {
        Map<String, Object> response = new HashMap<>();
        response.put("products", Collections.emptyList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{product_id}")
    @Operation(summary = "Update product", description = "Updates product settings (desired price, active status)")
    public ResponseEntity<Map<String, String>> updateProduct(
            @PathVariable("product_id") String productId,
            @RequestBody Map<String, Object> body) {
        Map<String, String> response = Map.of("product_id", productId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{product_id}")
    @Operation(summary = "Deactivate product", description = "Deactivates a product (soft delete)")
    public ResponseEntity<Void> deleteProduct(@PathVariable("product_id") String productId) {
        return ResponseEntity.noContent().build();
    }
}