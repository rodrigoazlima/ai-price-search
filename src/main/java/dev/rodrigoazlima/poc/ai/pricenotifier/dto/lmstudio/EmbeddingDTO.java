package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class EmbeddingDTO {

    private String object;
    private List<EmbeddingItemDTO> data;
    private String model;

}
