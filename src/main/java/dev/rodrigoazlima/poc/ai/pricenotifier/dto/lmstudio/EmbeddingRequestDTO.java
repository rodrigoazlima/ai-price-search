package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class EmbeddingRequestDTO {

    private String model;
    private List<String> input;
    private String encodingFormat;

}
