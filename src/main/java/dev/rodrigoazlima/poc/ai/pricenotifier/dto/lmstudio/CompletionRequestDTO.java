package dev.rodrigoazlima.poc.ai.pricenotifier.dto.lmstudio;

import lombok.Builder;
import lombok.Data;

import java.util.Map;


@Data
@Builder
public class CompletionRequestDTO {

    private String model;
    private String prompt;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    private String stop;
    private Double presencePenalty;
    private Double frequencyPenalty;
    private Map<String, Integer> logitBias;
    private Double repeatPenalty;
    private Integer seed;
    private Integer topP;
    private Integer topK;

}
