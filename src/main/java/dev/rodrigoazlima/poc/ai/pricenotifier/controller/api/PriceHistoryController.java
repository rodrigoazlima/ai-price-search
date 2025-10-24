package dev.rodrigoazlima.poc.ai.pricenotifier.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/products/{product_id}/history")
@Tag(name = "Price History", description = "Endpoints to retrieve product price history")
public class PriceHistoryController {

    @GetMapping
    @Operation(summary = "Get product price history", description = "Retrieves the price history time series for a specific product")
    public ResponseEntity<?> getPriceHistory(
            @PathVariable("product_id") String productId,
            @RequestParam(name = "from", required = false)
            @Parameter(description = "Start of the time range in ISO-8601, e.g., 2023-01-01T00:00:00Z") String from,
            @RequestParam(name = "to", required = false)
            @Parameter(description = "End of the time range in ISO-8601, e.g., 2023-12-31T23:59:59Z") String to,
            @RequestParam(name = "limit", required = false)
            @Parameter(description = "Max number of entries to return") Integer limit,
            @RequestParam(name = "sort", required = false, defaultValue = "asc")
            @Parameter(description = "Sort order by timestamp: asc or desc") String sort
    ) {
        // Basic validation of sort parameter
        String sortNormalized = sort == null ? "asc" : sort.trim().toLowerCase(Locale.ROOT);
        if (!Objects.equals(sortNormalized, "asc") && !Objects.equals(sortNormalized, "desc")) {
            Map<String, Object> error = Map.of(
                    "error", "Invalid sort parameter",
                    "detail", "sort must be 'asc' or 'desc'"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Validate date formats if provided (ISO-8601). We do not use them further in this minimal implementation.
        try {
            if (from != null && !from.isBlank()) {
                OffsetDateTime.parse(from);
            }
            if (to != null && !to.isBlank()) {
                OffsetDateTime.parse(to);
            }
        } catch (DateTimeParseException ex) {
            Map<String, Object> error = Map.of(
                    "error", "Invalid timestamp",
                    "detail", "from/to must be ISO-8601 (e.g., 2023-01-01T00:00:00Z)"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Minimal placeholder response: empty history array with the product_id
        Map<String, Object> response = new HashMap<>();
        response.put("product_id", productId);
        response.put("history", Collections.emptyList());
        return ResponseEntity.ok(response);
    }
}
