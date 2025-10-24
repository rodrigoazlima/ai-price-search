package dev.rodrigoazlima.poc.ai.pricenotifier.service;

import dev.rodrigoazlima.poc.ai.pricenotifier.dto.ProductTrackDTO;

public interface PriceNotificationService {

    boolean createNewTracker(ProductTrackDTO dto);
}
