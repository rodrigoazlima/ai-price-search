package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class EmbeddingItemDTO {

    private Integer index;
    private String object;
    private List<Double> embedding;

}
