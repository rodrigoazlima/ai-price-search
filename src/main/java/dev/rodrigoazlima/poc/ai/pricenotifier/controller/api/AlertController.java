package dev.rodrigoazlima.poc.ai.pricenotifier.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Endpoints to manage alert settings for products")
public class AlertController {

    @PostMapping
    @Operation(summary = "Create alert", description = "Creates a new alert setting for a product")
    public ResponseEntity<Map<String, String>> createAlert(@RequestBody Map<String, Object> body) {
        log.debug("Create alert request: {}", body);
        String productId = Optional.ofNullable(body.get("product_id")).map(Object::toString).orElse("").trim();
        if (productId.isEmpty()) {
            return badRequest("Invalid product_id: must be provided");
        }

        // desired_price must be a positive number
        Double desiredPrice = null;
        Object dp = body.get("desired_price");
        if (dp instanceof Number) {
            desiredPrice = ((Number) dp).doubleValue();
        } else if (dp != null) {
            try {
                desiredPrice = Double.parseDouble(dp.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        if (desiredPrice == null || desiredPrice <= 0) {
            return badRequest("Invalid desired_price: must be a positive number");
        }

        // min_interval_hours must be a non-negative integer
        Integer minInterval = null;
        Object mih = body.get("min_interval_hours");
        if (mih instanceof Number) {
            minInterval = ((Number) mih).intValue();
        } else if (mih != null) {
            try {
                minInterval = Integer.parseInt(mih.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        if (minInterval == null || minInterval < 0) {
            return badRequest("Invalid min_interval_hours: must be a non-negative integer");
        }

        Map<String, String> response = Map.of("alert_id", UUID.randomUUID().toString());
        log.info("Alert created for product {} with minInterval {} and desiredPrice {}", productId, minInterval, desiredPrice);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List alerts", description = "Lists alert settings for the authenticated user, optionally filtered by product")
    public ResponseEntity<Map<String, Object>> listAlerts(
            @RequestParam(name = "product_id", required = false)
            @Parameter(description = "Filter by product ID") String productId,
            @RequestParam(name = "limit", required = false, defaultValue = "100")
            @Parameter(description = "Max results (1-100)") int limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0")
            @Parameter(description = "Pagination offset (>= 0)") int offset
    ) {
        if (limit < 1 || limit > 100) {
            return badRequestObj("Invalid limit: must be between 1 and 100");
        }
        if (offset < 0) {
            return badRequestObj("Invalid offset: must be >= 0");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("alerts", Collections.emptyList());
        log.debug("List alerts productId={}, limit={}, offset={}", productId, limit, offset);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        log.warn("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
    }

    // Variant for endpoints that return Map<String, Object>
    private ResponseEntity<Map<String, Object>> badRequestObj(String message) {
        log.warn("Validation error: {}", message);
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
