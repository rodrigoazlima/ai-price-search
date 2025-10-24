package dev.rodrigoazlima.poc.ai.pricenotifier.repository;

import dev.rodrigoazlima.poc.ai.pricenotifier.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // Find price history for a specific product
    List<PriceHistory> findByProductIdOrderByTimestampDesc(String productId);

    // Find price history for a specific product within a date range
    List<PriceHistory> findByProductIdAndTimestampBetweenOrderByTimestampDesc(
            String productId, LocalDateTime startDate, LocalDateTime endDate);

    // Find the latest price for a specific product
    PriceHistory findTopByProductIdOrderByTimestampDesc(String productId);

    // Find all records with prices higher than a specified amount
    List<PriceHistory> findByPriceGreaterThanOrderByTimestampDesc(BigDecimal price);

    // Find all records for a specific currency
    List<PriceHistory> findByCurrencyOrderByTimestampDesc(String currency);
}
